/**
 * JPicosheet: Spreadsheet engine for Java Applications
 * Copyright (C) 2011 yusuke nishikawa
 * 
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 * 
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
 */
package com.nissy_ki_chi.jpicosheet.core;

import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

import com.nissy_ki_chi.jpicosheet.core.Cell.CellStatus;


/**
 * セルの集合を表すオブジェクトです。<br>
 * グループの中には同じシートに属する任意の数のセルを含めることができますが、グループ内のセルの順序は保障されません。
 * @author yusuke nishikawa
 *
 */
public class Group implements CellGroupReference {


	/**
	 * グループ名
	 */
	private String _name;

	/**
	 * このグループに属するセル
	 */
	private Map<String, Cell> _cells;

// Groups in Group はしばらく実装中止
//	/**
//	 * このグループに属するグループ
//	 */
//	private Map<STRING, Group> _groups;

	/**
	 * このグループを持っているSheetオブジェクトへの参照
	 */
	private Sheet _sheet;

	/**
	 * グループ名のパターン文字列
	 */
	static final String GROUP_NAME_PATTERN = "[a-zA-Z_][a-zA-Z0-9_]*@";

	/**
	 * グループ名の正規表現パターン
	 */
	private static Pattern _groupNamePattern = Pattern.compile(GROUP_NAME_PATTERN);

	/**
	 * 完全修飾グループ名のパターン文字列
	 */
	static final String FULLY_QUALIFIED_GROUP_NAME_PATTERN = Sheet.SHEET_NAME_PATTERN + "!" + GROUP_NAME_PATTERN;

	/**
	 * 完全修飾グループ名の正規表現パターン
	 */
	private static Pattern _fullyQualifiedGroupNamePattern = Pattern.compile(FULLY_QUALIFIED_GROUP_NAME_PATTERN);

	Group(String groupName, Sheet sheet) {

		validateGroupName(groupName);

		_name = groupName;
		_sheet = sheet;
		_cells = new HashMap<String, Cell>();
// Groups in Group はしばらく実装中止
//		_groups = new HashMap<STRING, Group>();
	}

	/**
	 * このグループの名前を返します
	 * @return グループの名前
	 */
	public String getName() {
		return _name;
	}

	/**
	 * このグループの完全修飾名を返します
	 * @return グループの完全修飾名
	 */
	public String getFullyQualifiedName() {
		return _sheet.getName() + "!" + _name;
	}

	/**
	 * グループにセルを追加します。
	 * @param cell 追加するセルオブジェクト
	 */
	public Group addCell(Cell cell) {
		// 追加しようとするセルを持つシートが、このグループを持つシートと同じでなければならない
		if (this._sheet != cell.getSheet()) {
			// 異なる場合、エラー
			throw new IllegalArgumentException("グループ" + this.getName() + "に異なるシート" +
					cell.getSheet() + "に属するセル" + cell.getName() + "を追加しようとしました");
		}
		// 同じ場合は追加する
		_cells.put(cell.getName(), cell);
		// このグループを参照しているセルがある場合、それらのセルが追加したセルを参照していることを登録する
		Resolver resolver = _sheet.getBook().getResolver();
		Collection<Cell> groupReferencesCells = resolver.getReferencingCells(this);
		for (Cell referencesCell: groupReferencesCells) {
			resolver.registGroupReferences(referencesCell, this.getName());
		}
		// このグループを参照しているセルを再計算
		recalcReferencingCell(this);
		return this;
	}

	/**
	 * グループにセルを追加します。
	 * @param cells 追加するセルオブジェクトを格納したCollection
	 * @return このグループ
	 */
	public Group addCells(Collection<Cell> cells) {

		Iterator<Cell> iter = cells.iterator();
		while(iter.hasNext()) {
			addCell(iter.next());
		}
		return this;
	}

	/**
	 * セル名を指定してグループにセルを追加します。
	 * @param cellName 追加するセル名
	 * @throws ReferenceNotFoundException セル名が見つからなかった場合
	 */
	public Group addCell(String cellName) throws ReferenceNotFoundException {
		Resolver resolver = _sheet.getBook().getResolver();
		addCell(resolver.getCell(cellName));
		return this;
	}

	/**
	 * セル名を指定してグループにセルを追加します。
	 * @param cellNames 追加するセル名
	 * @return このグループ
	 * @throws ReferenceNotFoundException セル名が見つからなかった場合
	 */
	public Group addCells(String[] cellNames) throws ReferenceNotFoundException {
		for (String cellName: cellNames) {
			addCell(cellName);
		}
		return this;
	}


	/**
	 * グループからセルを削除します<br>
	 * 指定したセルがグループに存在しない場合は何もしません
	 * @param cell 削除するセル
	 * @return このグループ
	 */
	public Group removeCell(Cell cell) {
		_cells.remove(cell.getName());
		// このグループを参照しているセルがある場合、それらのセルを再計算する
		recalcReferencingCell(this);
		return this;
	}

	/**
	 * グループからセルを削除します<br>
	 * 指定したセルがグループに存在しない場合は何もしません
	 * @param cellName 削除するセル名
	 * @return このグループ
	 */
	public Group removeCell(String cellName) {
		try {
			Cell removeCell = _sheet.getCell(cellName);
			removeCell(removeCell);
		} catch (ReferenceNotFoundException e) {
			// 指定したセル名がシートに存在しない場合、何もしない
		}
		return this;
	}

	/**
	 * グループからセルを削除します<br>
	 * 指定したセルがグループに存在しない場合は何もしません
	 * @param cellNames 削除するセル名の配列
	 * @return このグループ
	 */
	public Group removeCells(String[] cellNames ) {
		for (String cellName: cellNames) {
			removeCell(cellName);
		}
		return this;
	}
	
	/* (非 Javadoc)
	 * @see com.nissy_ki_chi.jpicosheet.core.CellGroupReference#getCells()
	 */
	public Collection<Cell> getCells(){
		return _cells.values();
		// Groups in Group はしばらく実装中止
		//return getCellsImpl(new HashMap<STRING, Cell>(), new HashSet<STRING>());
	}

	/**
	 * 渡された文字列がグループ名として正しいかチェックします。<br>
	 * 正しくない場合、例外がスローされます。
	 * @param groupName チェックするグループ名
	 * @throws IllegalArgumentException グループ名として正しくない場合
	 */
	private void validateGroupName(String groupName) throws IllegalArgumentException {

		if (! _groupNamePattern.matcher(groupName).matches()) {
			throw new IllegalArgumentException("invalid group name \"" + groupName + "\"");
		}
	}


	/**
	 * 渡された文字列がグループ名として正しいかチェックします。<br>
	 * 正しくない場合、例外がスローされます。
	 * @param groupName チェックするグループ名
	 * @return グループ名として正しい場合true、そうでない場合false
	 */
	static boolean isValidGroupName(String groupName) {

		return _groupNamePattern.matcher(groupName).matches();

	}

	/**
	 * 渡された文字列が完全修飾グループ名として正しいかチェックします。<br>
	 * 正しくない場合、例外がスローされます。
	 * @param cellName チェックするグループ名
	 * @return グループ名として正しい場合true、そうでない場合false
	 */
	static boolean isValidFullyQualifiedGroupName(String fullyQualifiedGroupName) {

		return _fullyQualifiedGroupNamePattern.matcher(fullyQualifiedGroupName).matches();

	}

	/**
	 * 指定したグループを参照しているセルを再計算します
	 * @param group チェックするグループ
	 */
	private void recalcReferencingCell(Group group) {
		Resolver resolver = group._sheet.getBook().getResolver();
		Calculator calcurator = group._sheet.getBook().getCalculator();

		Set<Cell> referencingCells = resolver.getReferencingCells(group);
		for (Cell cell: referencingCells) {
			cell.setStatus(CellStatus.REQUIRE_CALCULATION);
			calcurator.calc(cell);
		}
	}

	/* (非 Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		// Groupクラスのインスタンスで、保持するセル名がすべて同じ場合trueを返す
		if (obj instanceof Group) {
			Group g = (Group) obj;
			Set<String> myCells = _cells.keySet();
			Set<String> gCells = g._cells.keySet();
			if (myCells.equals(gCells)) {
				return true;
			}
		}
		return false;
	}

}
