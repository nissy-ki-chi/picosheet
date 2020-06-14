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

import static org.junit.Assert.assertArrayEquals;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;


/**
 * セル同士の参照関係を保持するオブジェクトです。
 * @author yusuke nishikawa
 */
public class Resolver {


	/**
	 * このオブジェクトを持っているブックへの参照
	 */
	private Book _book;
	/**
	 * このブックのカレントシート参照
	 */
	private Sheet _currentSheet;

	/**
	 * あるセルまたはグループを参照しているほかのセルの集合を保持するMap
	 */
	private Map<String, Set<Cell>> _references;


	@SuppressWarnings("unused")
	private Resolver() {
	}

	/**
	 * Bookオブジェクトを指定してこのオブジェクトを作成します
	 * @param book Bookオブジェクト
	 */
	Resolver(Book book) {
		_book = book;
		_references = new HashMap<String, Set<Cell>>();
	}

	/**
	 * このブックで既定で使用するシートオブジェクトを指定します
	 * @param currentSheet 選択するシートオブジェクト
	 */
	public void setCurrentSheet(Sheet currentSheet) {
		this._currentSheet = currentSheet;
	}

	/**
	 * このブックで現在選択されているシートオブジェクトを返します
	 * @return 選択されているシートオブジェクト
	 */
	public Sheet getCurrentSheet() {
		return this._currentSheet;
	}

	/**
	 * セル名またはグループ名の完全修飾名を返します
	 * @param sheet 引数nameの属するSheetオブジェクト
	 * @param name セル名またはグループ名
	 * @return セル名またはグループ名の完全修飾名
	 */
	String getFullyQualifiedName(Sheet sheet, String name) {
		if (isFullyQualifiedName(name)) {
			return name;
		} else {
			return sheet.getName() + "!" + name;
		}
	}

	/**
	 * 渡された名前が完全修飾名か否かを返します
	 * @param name セル名
	 * @return セル名が完全修飾名の場合true、そうでない場合false
	 */
	public boolean isFullyQualifiedName(String name) {
		if (name.contains("!")) {
			return true;
		} else {
			return false;
		}
	}

	/**
	 * 完全修飾名からシート名を抽出します。
	 * @param fullyQualifiedName 完全修飾名
	 * @return シート名
	 */
	public String getSheetNameFromFullyQualifiedName(String fullyQualifiedName) {
		if (isFullyQualifiedName(fullyQualifiedName)) {
			return fullyQualifiedName.substring(0, fullyQualifiedName.indexOf("!"));
		} else {
			throw new IllegalArgumentException(fullyQualifiedName + " is not FullyQualifiedName.");
		}
	}

	/**
	 * 指定したセル名のセルが存在するかどうか確認します。<br>
	 * シート名!セル名 の形式で指定した場合で、指定したシートが存在しない場合、ReferenceNotFoundExceptionがスローされます。
	 * @param cellName セル名。シート名!セル名 の形式でも指定できる。シート名を指定しない場合カレントシートから探す。
	 * @return 指定したセルが存在する場合true、そうでない場合false
	 */
	public boolean cellExists(String cellName) {
		// セル名に"!"が含まれている場合、シート名!セル名 の完全修飾名形式である
		if (cellName.contains("!")) {
			String[] sheetCell = cellName.split("!");
			return this._book.getSheet(sheetCell[0]).cellExists(cellName);
		}
		// そうでない場合、セル名 の形式である
		return this._currentSheet.cellExists(cellName);
	}

	/**
	 * セル名の文字列表現からセルオブジェクトを返します。<br>
	 * 指定されたセルが見つからなかった場合、ReferenceNotFoundExceptionがスローされます
	 * @param cellName セル名
	 * @return セルオブジェクト
	 */
	public Cell getCell(String cellName) {
		return getCell(cellName, null);
	}
	
	/**
	 * セル名の文字列表現からセルオブジェクトを返します。<br>
	 * cellNameがテーブルのセルアドレス形式であり、defaultTableが渡されている場合そのテーブルからセルオブジェクトを返します。<br>
	 * 指定されたセルが見つからなかった場合、ReferenceNotFoundExceptionがスローされます
	 * @param cellName セル名
	 * @return セルオブジェクト
	 */
	public Cell getCell(String cellName, Table defaultTable) {
		// セル名に"!"が含まれている場合、シート名!セル名 の形式である
		if (cellName.contains("!")) {
			String[] sheetCell = cellName.split("!");
			return this._book.getSheet(sheetCell[0]).getCell(sheetCell[1]);
		}
		
		// defaultTableが渡されていてcellNameがセルアドレス形式の場合、このテーブルからセルを得る
		if (defaultTable != null && Table.isValidAddressName(cellName)) {
			return defaultTable.getCell(cellName);
		} else {
			// そうでない場合、セル名 の形式である
			return this._currentSheet.getCell(cellName);
		}
	}

	/**
	 * グループ名の文字列表現からセルオブジェクトのコレクションを返します。<br>
	 * 指定されたグループが見つからなかった場合、ReferenceNotFoundExceptionがスローされます
	 * @param groupName グループ名
	 * @return セルオブジェクトを含むコレクション
	 */
	public Collection<Cell> getCellsFromGroup(String groupName) {
		// 対象とするシートの決定
		Sheet targetSheet;
		String targetGroup;
		if (groupName.contains("!")) {
			String[] sheetGroup = groupName.split("!");
			targetSheet = this._book.getSheet(sheetGroup[0]);
			targetGroup = sheetGroup[1];
		} else {
			targetSheet = this._currentSheet;
			targetGroup = groupName;
		}
		// シートに含まれるグループからCellを得る
		return targetSheet.getGroup(targetGroup).getCells();
	}

	/**
	 * テーブル名もしくはテーブル名＋範囲名の文字列表現からセルオブジェクトのコレクションを返します。
	 * テーブル名が見つからなかった場合、ReferenceNotFoundExceptionがスローされます
	 * @param tableName テーブル名
	 * @return セルオブジェクトを含むコレクション
	 */
	public Collection<Cell> getCellsFromTable(String tableName) {
		// 対象とするシートの決定
		Sheet targetSheet;
		String targetTable;
		if (tableName.contains("!")) {
			String[] sheetTable = tableName.split("!");
			targetSheet = this._book.getSheet(sheetTable[0]);
			targetTable = sheetTable[1];
		} else {
			targetSheet = this._currentSheet;
			targetTable = tableName;
		}
		// シートに含まれるテーブルからCellを得る
		int p = targetTable.indexOf('#');
		if ((p > 0) && (p != targetTable.length() - 1)) {
			// テーブル名に範囲指定が付いている場合、その範囲のRangeオブジェクトからコレクションを得る
			Range range = targetSheet.getTable(targetTable.substring(0, p + 1)).getRange(targetTable.substring(p + 1));
			return range.getCells();
		} else {
			// そうでない場合、テーブル全体のコレクションを得る
			return targetSheet.getTable(targetTable).getCells();
		}
	}

	/**
	 * このセルが他のセルを参照していることを登録します
	 * @param referenceCell 他のセルを参照しているセルオブジェクト
	 * @param referencesCellName 参照先のセル名
	 */
	void registReferences(Cell referenceCell, String referencesCellName) {

		String fqn = getFullyQualifiedName(referenceCell.getSheet(), referencesCellName);
		if (! _references.containsKey(fqn)) {
			_references.put(fqn, new HashSet<Cell>());
		}
		_references.get(fqn).add(referenceCell);
	}

	/**
	 * このセルの、他のセルへの参照の登録を取り消します
	 * @param referenceCell 他のセルを参照していたセルオブジェクト
	 * @param referencesCellName 参照の登録を取り消すセル名
	 */
	void removeReferences(Cell referenceCell, String referencesCellName) {

		String fqn = getFullyQualifiedName(referenceCell.getSheet(), referencesCellName);
		if (_references.containsKey(fqn)) {
			_references.get(fqn).remove(referenceCell);
			//このセルを参照しているセルが1つも無くなった場合、_referencesから削除する
			if (_references.get(fqn).size() == 0) {
				_references.remove(fqn);
			}
		}
	}

	Set<Cell> getReferencingCells(Cell referenceCell) {
		Set<Cell> returnCell = new HashSet<Cell>();
		getReferencingCellsRecursive(referenceCell, returnCell);
		return returnCell;
	}

	/**
	 * 渡されたセルを参照しているセルを再帰的に調査し、引数referencingCellsにセットします。<br>
	 * このメソッドの実行前の時点でreferencingCellsにセルがセットされていた場合、調査したセルは既存のreferencingCellsに追加されていきます。
	 * @param cell 調査対象のセル
	 * @param referencingCells 調査対象のセルを参照しているセル
	 */
	private void getReferencingCellsRecursive(Cell cell, Set<Cell> referencingCells){

		//このセルを参照しているセルを得る
		Set<Cell> set = _references.get(cell.getFullyQualifiedName());
		
		if (set == null) {
			// 参照しているセルが無い場合終了
			return;
		} else {
			// 参照しているセルをreferencingCellsに追加し、再帰する。
			Iterator<Cell> iter = set.iterator();
			while(iter.hasNext()){
				Cell refCell = iter.next();
				if (! referencingCells.contains(refCell)) {
					referencingCells.add(refCell);
					getReferencingCellsRecursive(refCell, referencingCells);
				}
			}
		}
	}

	/**
	 * 渡されたグループを参照しているセルを返します。<br>
	 * そのグループを参照しているセルが1つも無い場合、空のSetが返されます。
	 * @param referenceGroup 調査対象のグループ
	 * @return 調査対象のグループを参照しているセル
	 */
	Set<Cell> getReferencingCells(Group referenceGroup) {

		Set<Cell> returnCell = new HashSet<Cell>();
		String groupFqn = referenceGroup.getFullyQualifiedName();
		if (_references.containsKey(groupFqn)) {
			returnCell.addAll(_references.get(groupFqn));
			return returnCell;
		} else {
			return returnCell;
		}
	}

	/**
	 * 指定したグループ名のグループが存在するかどうか確認します。
	 * シート名!グループ名 の形式で指定した場合で、指定したシートが存在しない場合、ReferenceNotFoundExceptionがスローされます
	 * @param groupName グループ名。シート名!グループ名 の形式でも指定できる。シート名を指定しない場合カレントシートから探す。
	 * @return 指定したグループが存在する場合true、そうでない場合false
	 */
	public boolean groupExists(String groupName) {
		// グループ名に"!"が含まれている場合、シート名!グループ名 の形式である
		if (groupName.contains("!")) {
			String[] sheetCell = groupName.split("!");
			return this._book.getSheet(sheetCell[0]).groupExists(groupName);
		}
		// そうでない場合、グループ名 の形式である
		return this._currentSheet.groupExists(groupName);
	}

	/**
	 * グループ名の文字列表現からグループオブジェクトを返します。
	 * グループ名が見つからなかった場合、ReferenceNotFoundExceptionがスローされます
	 * @param groupName グループ名
	 * @return グループブジェクト
	 * @throws ReferenceNotFoundException グループ名が見つからなかった場合
	 */
	public Group getGroup(String groupName) {
		// グループ名に"!"が含まれている場合、シート名!グループ名 の形式である
		if (groupName.contains("!")) {
			String[] sheetGroup = groupName.split("!");
			return this._book.getSheet(sheetGroup[0]).getGroup(sheetGroup[1]);
		}
		// そうでない場合、グループ名 の形式である
		return this._currentSheet.getGroup(groupName);
	}

	/**
	 * このセルがグループを参照していることを登録します
	 * @param cell グループを参照しているセルオブジェクト
	 * @param referenceGroupName 参照先のグループ名
	 */
	void registGroupReferences(Cell cell, String referenceGroupName) {

		String fqn = getFullyQualifiedName(cell.getSheet(), referenceGroupName);
		// _referencesに登録
		registReferences(cell, fqn);
		// グループは存在するか？
		Group group = null;
		try {
			group = cell.getSheet().getGroup(fqn);
		} catch (ReferenceNotFoundException e) {
			// 存在しない場合、それ以上何もしない
			return;
		}

		// 存在する場合、グループのメンバであるセルたちを得る
		// セルたちを引数cellのセルが参照しているセルである、としてすべて_referencesに登録
		for (Cell memberCell: group.getCells()) {
			registReferences(cell, memberCell.getFullyQualifiedName());
		}
	}

	/**
	 * 渡されたテーブルを参照しているセルを返します。<br>
	 * そのテーブルを参照しているセルが1つも無い場合、空のSetが返されます。
	 * @param referenceTable 調査対象のテーブル
	 * @return 調査対象のテーブルを参照しているセル
	 */
	Set<Cell> getReferencingCells(Table referenceTable) {

		Set<Cell> returnCell = new HashSet<Cell>();
		String tableFql = referenceTable.getFullyQualifiedName();
		if (_references.containsKey(tableFql)) {
			returnCell.addAll(_references.get(tableFql));
			return returnCell;
		} else {
			return returnCell;
		}
	}

	/**
	 * 指定したテーブル名のテーブルが存在するかどうか確認します。<br>
	 * シート名!テーブル名 の形式で指定した場合で、指定したシートが存在しない場合、ReferenceNotFoundExceptionがスローされます
	 * @param tableName テーブル名。シート名!テーブル名 の形式でも指定できる。シート名を指定しない場合カレントシートから探す。
	 * @return 指定したテーブルが存在する場合true、そうでない場合false
	 */
	public boolean tableExists(String tableName) {
		// テーブル名に"!"が含まれている場合、シート名!テーブル名 の形式である
		if (tableName.contains("!")) {
			String[] sheetCell = tableName.split("!");
			return this._book.getSheet(sheetCell[0]).tableExists(tableName);
		}
		// そうでない場合、テーブル名 の形式である
		return this._currentSheet.tableExists(tableName);
	}

	/**
	 * テーブル名の文字列表現からテーブルオブジェクトを返します。<br>
	 * テーブル名が見つからなかった場合、ReferenceNotFoundExceptionがスローされます
	 * @param tableName テーブル名
	 * @return テーブルブジェクト
	 */
	public Table getTable(String tableName)  {
		// テーブル名に"!"が含まれている場合、シート名!テーブル名 の形式である
		if (tableName.contains("!")) {
			String[] sheetTable = tableName.split("!");
			return this._book.getSheet(sheetTable[0]).getTable(sheetTable[1]);
		}
		// そうでない場合、テーブル名 の形式である
		return this._currentSheet.getTable(tableName);
	}

	/**
	 * このセルがテーブルを参照していることを登録します
	 * @param cell テーブルを参照しているセルオブジェクト
	 * @param referenceTableName 参照先のテーブル名
	 */
	void registTableReferences(Cell cell, String referenceTableName) {

		String[] splitFqn  = getFullyQualifiedName(cell.getSheet(), referenceTableName).split("#");
		String fqn = splitFqn[0] + "#";
		String range = null;
		if (splitFqn.length == 2) {
			range = splitFqn[1];
		}
		// _referencesに登録
		registReferences(cell, fqn);
		// テーブルは存在するか？
		Table table = null;
		try {
			table = cell.getSheet().getTable(fqn);
		} catch (ReferenceNotFoundException e) {
			// 存在しない場合、それ以上何もしない
			return;
		}

		// 存在する場合、テーブルのメンバであるセルたちを得る
		// セルたちを引数cellのセルが参照しているセルである、としてすべて_referencesに登録
		Collection<Cell> collection;
		if (range == null) {
			collection = table.getCells();
		} else {
			collection = table.getRange(range).getCells();
		}
		for (Cell memberCell: collection) {
			registReferences(cell, memberCell.getFullyQualifiedName());
		}
	}

}
