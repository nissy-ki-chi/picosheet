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

import java.math.MathContext;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

import com.nissy_ki_chi.jpicosheet.core.Cell.CellStatus;
import com.nissy_ki_chi.jpicosheet.core.Cell.CellType;

/**
 * 複数のセル、グループを持つことのできる"シート"です。<br>
 *
 * 初期状態のシートにはセルが1つもありません。シートには絶対アドレスが無く、ちょうど真っ白な紙のような状態となっています。
 * この真っ白なシートに対してセルを1つづつ追加していきます。セルにはセル名を指定し、このセル名がセルアドレスの代わりとなります。
 * 1つのシートの中に同じ名前のセルを複数作ることはできません。<br>
 *
 * シートにはセルに加え、グループとテーブルを加えることができます。<br>
 * グループは複数のセルの集合を表すものです。グループもセルと同様ユニークな名前を
 * 付けますが、グループ名には末尾に"@"を加えます。<br>
 * テーブルはグループと同じくセルの集合ですが、セルが行と列に順序を保った状態で保持されているためデータの保持に便利です。
 * テーブル名には末尾に"#"を加えます。テーブル内のセルにアクセスするにはテーブル名の後ろに"R1C1形式"でテーブル内のセルアドレスを指定します。<br>
 *
 * セル、グループともに任意の数をシートに追加することができます。<br>
 * シートごとに保持する数値の精度を指定することができます。<br>
 *
 * @author yusuke nishikawa
 *
 */
public class Sheet implements Comparable<Sheet> {

	public static final MathContext DEFAULT_MATH_CONTEXT = MathContext.DECIMAL64;

	/**
	 * シート名
	 */
	private String _name;
	/**
	 * このシートの計算精度
	 */
	private MathContext _mc = DEFAULT_MATH_CONTEXT;

	/**
	 * このシートが持つセルのMap
	 */
	private HashMap<String, Cell> _cells;
	private HashMap<String,Group> _groups;
	private HashMap<String,Table> _tables;

	/**
	 * このシートのセルの中で、ステータスがErrorであるセル
	 */
	private HashMap<String, Cell> _errorCells;

	/**
	 * このシートを持っているBookオブジェクトへの参照
	 */
	private Book _book;
	/**
	 * シート名のパターン文字列
	 */
	static final String SHEET_NAME_PATTERN = "[a-zA-Z_][a-zA-Z0-9_]*";
	/**
	 * シート名の正規表現パターン
	 */
	private static Pattern _sheetNamePattern = Pattern.compile(SHEET_NAME_PATTERN);

	@SuppressWarnings("unused")
	private Sheet() {}

	/**
	 * シート名およびブックを指定してシートを作成します。
	 * @param sheetName シート名
	 * @param _book このブックが属するBookオブジェクト
	 * @throws IllegalArgumentException シート名が正しくない場合
	 */
	public Sheet(String sheetName, Book book) throws IllegalArgumentException{
		super();

		// 正しいシート名かチェック
		validateSheetName(sheetName);

		this._name = sheetName;
		this._cells = new HashMap<String, Cell>();
		this._groups = new HashMap<String, Group>();
		this._tables = new HashMap<String, Table>();
		this._errorCells = new HashMap<String, Cell>();
		this._book = book;
	}

	/**
	 * シート名およびブック、MathContextオブジェクトを指定してシートを作成します。
	 * @param sheetName シート名
	 * @param _book このブックが属するBookオブジェクト
	 * @param _mc このシートの計算時にデフォルトで使用するMathContextオブジェクト
	 * @throws IllegalArgumentException シート名が正しくない場合
	 */
	public Sheet (String sheetName, Book book, MathContext mc) throws IllegalArgumentException  {
		this(sheetName, book);
		this._mc = mc;
	}


	/**
	 * シート名を返します。
	 * @return シート名
	 */
	public String getName() {
		return _name;
	}

	/**
	 * シート名をセットします
	 * @param cellName 新しいシート名
	 * @throws IllegalArgumentException シート名が正しくない場合
	 */
	public void setName(String sheetName) throws IllegalArgumentException  {
		// 正しいシート名かチェック
		validateSheetName(sheetName);
		// TODO:シート名を変えたらResolverの中のセル参照も変更する必要がある
		this._name = sheetName;
	}

	/**
	 * このシートが属するBookオブジェクトを返します
	 * @return このシートが属するBookオブジェクト
	 */
	public Book getBook() {
		return this._book;
	}

	/**
	 * このシートのデフォルトMathContextオブジェクトを返します
	 * @return このシートのデフォルトMathContextオブジェクト
	 */
	public MathContext getMathContext() {
		return this._mc;
	}

	/**
	 * このシートのデフォルトMathContextオブジェクトを指定します
	 * @param _mc このシートのデフォルトMathContextオブジェクト
	 */
	public void setMathContext(MathContext mc) {
		this._mc = mc;

		// 再計算が有効である場合、このシートのすべての式セルを再計算する
		Calculator calculator = _book.getCalculator();
		if (calculator.isRecalcEnable()) {
			calculator.recalcDisable();
			for (Cell cell: getCells()) {
				if (cell.getCellType() == CellType.FORMULA) {
					cell.setStatus(CellStatus.REQUIRE_CALCULATION);
				}
			}
			calculator.recalcEnable();
		}
	}

	/**
	 * 指定した名前のセルがシートに存在する場合、Trueを返します
	 * @param cellName 存在を確認するセル名
	 * @return 存在する場合true、そうでない場合false
	 * @throws IllegalArgumentException セル名が完全修飾セル名であり、そのシート名部分がこのシートの名前と異なる場合
	 */
	public boolean cellExists(String cellName) throws IllegalArgumentException {
		return this._cells.containsKey(getShortName(cellName));
	}

	/**
	 * 指定したセルオブジェクトがセルに存在する場合、Trueを返します
	 * @param cell 存在を確認するセルオブジェクト
	 * @return 存在する場合true、そうでない場合false
	 */
	public boolean cellExists(Cell cell) {
		// Cell#equalsは内容が同じである異なるオブジェクトに対しtrueを返すため、equalsは使わない
		for (Cell c: this._cells.values()) {
			if (c == cell) {
				return true;
			}
		}
		return false;
	}

	/**
	 * シートにセルを追加します。<br>
	 * すでに存在するセル名が指定された場合、既存のセルオブジェクトを返します。
	 * @param cellName セル名
	 * @return セルオブジェクト
	 * @throws IllegalArgumentException セル名が完全修飾セル名であり、そのシート名部分がこのシートの名前と異なる場合
	 */
	public Cell addCell(String cellName) {
		return addCell(cellName, false);
	}


	/**
	 * シートにセルを追加します。forTableCellがtrueの場合、テーブル作成用のセル名が使えます<br>
	 * すでに存在するセル名が指定された場合、既存のセルオブジェクトを返します。
	 * @param cellName セル名
	 * @param forTableCell テーブル作成のためのセル作成の場合true、そうでない場合false
	 * @return セルオブジェクト
	 * @throws IllegalArgumentException セル名が完全修飾セル名であり、そのシート名部分がこのシートの名前と異なる場合
	 */
	Cell addCell(String cellName, boolean forTableCell) {
		String shortName = getShortName(cellName);
		// セルを追加
		if (this._cells.containsKey(shortName)) {
			// すでに存在するセル名が指定された場合、既存のセルオブジェクトを返す
			return this._cells.get(shortName);
		} else {
			// そうでない場合、新しいセルを作成し、返す
			Cell cell = new PlainCell(shortName, this, forTableCell);
			this._cells.put(shortName, cell);
			return cell;
		}

	}

	/**
	 * 引数で指定したセル名を持つセルオブジェクトを返します。
	 * @param cellName セル名
	 * @return セルオブジェクト
	 */
	public Cell getCell(String cellName) {

		String shortName = getShortName(cellName);
		// 普通のセルから探す
		if (this._cells.containsKey(shortName)) {
			return this._cells.get(shortName);
		}
		// 見つからなかったらテーブルから探す
		Cell tableCell = getCellFromTablesCell(cellName);
		if(tableCell != null) {
			return tableCell;
		}
		// セルが存在しなかった場合、例外をスロー
		throw new ReferenceNotFoundException("cellname " + cellName + " is not found");
	}

	private Cell getCellFromTablesCell(String tableCellName) {
		if (!Table.isValidTableNameWithAddress(tableCellName)) {
			return null;
		}
		int sharpIndex = tableCellName.lastIndexOf('#');
		String tableName = tableCellName.substring(0, sharpIndex + 1);
		String[] address = tableCellName.substring(sharpIndex + 2).split("[RrCc]"); 
		int rowPos = Integer.parseInt(address[0]);
		int colPos = Integer.parseInt(address[1]);
		Table targetTable = _tables.get(tableName);
		if (targetTable != null) {
			if ((colPos <= targetTable.getColSize()) && (rowPos <= targetTable.getRowSize())) {
				return _tables.get(tableName).getCell(rowPos, colPos);
			}
		}
		return null;
	}
	
	
	/**
	 * このシートが保持しているすべてのセルへの参照を返します
	 * @return シートが保持しているすべてのセルへの参照
	 */
	public Set<Cell> getCells() {
		return new HashSet<Cell>(_cells.values());
	}

	/**
	 * このシートが保持しているすべてのセルの名前(完全修飾セル名ではないセル名)を返します
	 * @return シートが保持しているすべてのセルの名前
	 */
	public Set<String> getCellNames() {
		return new HashSet<String>(_cells.keySet());
	}

	/**
	 * このシートが保持しているセルの中にエラーのセルがあるかを返します
	 * @return エラーセルが存在する場合true、なければfalse
	 */
	public boolean containsErrorCell() {
		return !this._errorCells.isEmpty();
	}

	/**
	 * このシートが保持しているエラーセルのSetを返します
	 * @return このシートが保持しているエラーセルのSet
	 */
	public Map<String, Cell> getErrorCells() {
		// このクラスのメンバである_errorCellsを書き換えられないよう、_errorCellsのシャローコピーを返す
		return new HashMap<String, Cell>(this._errorCells);
	}

	/**
	 * エラーステータスのセルを_errorCellsメンバに追加する
	 * @param cell エラーとなったセル
	 */
	void addErrorCell(Cell cell) {
		// 渡されたセルがこのシートに属するものであれば追加する
		if (this._cells.containsKey(cell.getName())) {
			this._errorCells.put(cell.getName(), cell);
		}
	}

	/**
	 * エラーステータスのセルを_errorCellsメンバから削除する
	 * @param cell エラーでなくなった、あるいは削除されたセル
	 */
	void removeErrorCell(Cell cell) {
		this._errorCells.remove(cell.getName());
	}

	/**
	 * _errorCellsメンバからすべてのセルの参照を削除します
	 */
	void removeAllErrorCell() {
		this._errorCells.clear();
	}

	/**
	 * セル名を変更します<br>
	 * 変更対象のセルが存在しない場合、ReferenceNotFoundExceptionが発生します。<br>
	 * 変更後のセル名を持つシートがすでに存在していた場合、そのセルオブジェクトは削除されます。
	 * @param cellName 変更対象のセル名
	 * @param newCellName 変更後のセル名
	 */
	public void renameCell(String cellName, String newCellName) {

		String shortCellName, shortNewCellName;
		shortCellName = getShortName(cellName);
		shortNewCellName = getShortName(newCellName);

		// セルが存在しなかったらエラー
		if (! this._cells.containsKey(shortCellName)) {
			throw new ReferenceNotFoundException("cellname " + cellName + "is not found");
		}

		// 変更前と変更後の名前が同じだったら何もしない
		if (cellName.equals(newCellName)) {
			return;
		}

		// Cellオブジェクトの名前を変更
		Cell cell = this._cells.get(shortCellName);
		Set<Cell> oldReferenceCells = _book.getResolver().getReferencingCells(cell);
		boolean isTableCell = Table.isValidTableNameWithAddress(shortCellName);
		cell.setName(shortNewCellName, isTableCell);

		// _cells上の管理をnewCellNameに変える
		this._cells.put(shortNewCellName, this._cells.get(shortCellName));
		this._cells.remove(shortCellName);
		// このセルがエラーセルであった場合、_errorCellsも更新が必要
		if (this._errorCells.containsKey(shortCellName)) {
			this._errorCells.put(shortNewCellName, this._errorCells.get(shortCellName));
			this._errorCells.remove(shortCellName);
		}

		// リネーム前のセルを参照していたセル(参照が無くなる)およびリネーム後のセルを参照しているセル(参照エラーだったのが直る)を再計算
		Calculator calculator = _book.getCalculator();
		calculator.recalcReferencingCells(oldReferenceCells);
		calculator.recalcReferencingCells(cell);
	}

	/**
	 * セルを削除します<br>
	 * 指定したセルがシートに存在しない場合、何もしません。
	 * @param cell 削除するセルオブジェクト
	 */
	public void deleteCell(Cell cell) {
		deleteCell(cell.getName());
	}

	/**
	 * セルを削除します<br>
	 * 指定したセルがシートに存在しない場合、何もしません。
	 * @param cellName 削除するセルのセル名
	 * @throws IllegalArgumentException 完全修飾セル名を指定した際に、シート名がこのシートの名前と異なる場合
	 */
	public void deleteCell(String cellName) throws IllegalArgumentException {

		// 完全修飾セル名が渡された場合、シート名がこのシートと異なるならエラー
		String shortName = getShortName(cellName);

		if (this._cells.containsKey(shortName)) {
			this._cells.remove(shortName);
			// _errorCellsからも削除する
			this._errorCells.remove(shortName);
		}
	}

	/**
	 * （完全修飾セル名でない）セル名を得ます。
	 * @param cellName 確認するセル名
	 * @throws IllegalArgumentException セル名が完全修飾セル名であり、そのシート名部分がこのシートの名前と異なる場合
	 */
	private String getShortName(String cellName) throws IllegalArgumentException {
		if (cellName.contains("!")) {
			// cellNameが完全修飾セル名である場合、シート名をチェック
			int delimiterIndex = cellName.indexOf("!");
			String argSheetName = cellName.substring(0, delimiterIndex);
			if (! argSheetName.equals(_name)) {
				throw new IllegalArgumentException("Invalid Sheetname " + argSheetName);
			}
			// シート名がこのシートと同じならセル名部分を返す
			return cellName.substring(delimiterIndex + 1);
		} else {
			// 完全修飾セル名で無いならそのまま返す
			return cellName;
		}

	}
	/**
	 * 指定した名前のグループがシートに存在する場合、Trueを返します
	 * @param groupName 存在を確認するグループ名
	 * @return 存在する場合true、そうでない場合false
	 * @throws IllegalArgumentException グループ名が完全修飾グループ名であり、そのシート名部分がこのシートの名前と異なる場合
	 */
	public boolean groupExists(String groupName) {
		return this._groups.containsKey(getShortName(groupName));
	}

	/**
	 * 指定したグループオブジェクトがシートに存在する場合、Trueを返します
	 * @param group 存在を確認するグループオブジェクト
	 * @return 存在する場合true、そうでない場合false
	 */
	public boolean groupExists(Group group) {
		// Group#equalsは内容が同じである異なるオブジェクトに対しtrueを返すため、equalsは使わない
		for (Group g: this._groups.values()) {
			if (g == group) {
				return true;
			}
		}
		return false;
	}

	/**
	 * シートにグループを追加します
	 * @param groupName グループ名
	 * @return グループオブジェクト
	 * @throws IllegalArgumentException グループ名が完全修飾セル名であり、そのシート名部分がこのシートの名前と異なる場合
	 */
	public Group addGroup(String groupName) {

		String shortGroupName = getShortName(groupName);
		// すでに存在するグループ名が指定された場合、既存のグループオブジェクトを返す
		if (this._groups.containsKey(shortGroupName)) {
			return this._groups.get(shortGroupName);
		} else {
			// そうでない場合、新しいグループを作成
			Group group = new Group(shortGroupName, this);
			this._groups.put(shortGroupName, group);

			// このグループを参照している(そして参照できていなかった)セルがある場合、それらのセルを再計算する
			recalcReferencingCell(group);
			return group;
		}

	}

	/**
	 * 引数で指定したグループ名を持つグループオブジェクトを返します。
	 * @param groupName グループ名
	 * @return グループオブジェクト
	 * @throws ReferenceNotFoundException 指定した名前のグループが存在しない場合
	 * @throws IllegalArgumentException グループ名が完全修飾セル名であり、そのシート名部分がこのシートの名前と異なる場合
	 */
	public Group getGroup(String groupName) throws ReferenceNotFoundException {

		String shortGroupName = getShortName(groupName);
		// 指定されたグループを返す
		if (this._groups.containsKey(shortGroupName)) {
			return this._groups.get(shortGroupName);
		} else {
			// グループが存在しなかった場合、例外をスロー
			throw new ReferenceNotFoundException("groupname " + groupName + " is not found");
		}
	}

	/**
	 * このシートが保持しているすべてのグループへの参照を返します
	 * @return シートが保持しているすべてのグループへの参照
	 */
	public Set<Group> getGroups() {

		return new HashSet<Group>(_groups.values());

	}

	/**
	 * グループを削除します<br>
	 * 指定したグループが存在しない場合、何もしません。
	 * @param group 削除するグループ
	 */
	public void deleteGroup(Group group) {
		this._groups.remove(group.getName());
		// このグループを参照していた(そして参照できなくなる)セルがある場合、それらのセルを再計算する
		recalcReferencingCell(group);
	}

	/**
	 * グループを削除します<br>
	 * 指定したグループがシートに存在しない場合、何もしません。
	 * @param groupName 削除するグループのグループ名
	 * @throws IllegalArgumentException グループ名が完全修飾セル名であり、そのシート名部分がこのシートの名前と異なる場合
	 */
	public void deleteGroup(String groupName) {

		String shortGroupName = getShortName(groupName);
		if (this._groups.containsKey(shortGroupName)) {
			deleteGroup(_groups.get(shortGroupName));
		}

	}



	/**
	 * 指定した名前のテーブルがシートに存在する場合、Trueを返します
	 * @param tableName テーブル名
	 * @return 存在する場合true、そうでない場合false
	 * @throws IllegalArgumentException テーブル名が完全修飾テーブル名であり、そのシート名部分がこのシートの名前と異なる場合
	 */
	public boolean tableExists(String tableName) {
		return this._tables.containsKey(getShortName(tableName));
	}

	/**
	 * 指定したテーブルオブジェクトがシートに存在する場合、trueを返します
	 * @param table 存在を確認するテーブルオブジェクト
	 * @return 存在する場合true、そうでない場合false
	 */
	public boolean tableExists(Table table) {
		// Table#equalsは内容が同じである異なるオブジェクトに対しtrueを返すため、equalsは使わない
		for (Table t: this._tables.values()) {
			if (t == table) {
				return true;
			}
		}
		return false;
	}

	/**
	 * シートにテーブルを追加します
	 * @param tableName テーブル名
	 * @param rowSize このテーブルの行数
	 * @param colSize このテーブルの列数
	 * @return テーブルオブジェクト
	 */
	public Table addTable(String tableName, int rowSize, int colSize) {

		String shortTableName = getShortName(tableName);
		// すでに存在するテーブル名が指定された場合、既存のテーブルオブジェクトを返す
		if (this._tables.containsKey(shortTableName)) {
			return this._tables.get(shortTableName);
		} else {
			// そうでない場合、新しいテーブルを作成
			Table table = new Table(shortTableName, colSize, this);
			table.addRow(rowSize);
			this._tables.put(shortTableName, table);

//			// このテーブルを参照している(そして参照できていなかった)セルがある場合、それらのセルを再計算する
			// TODO: テーブルでのrecalcReferencingCell 実装する
//			recalcReferencingCell(group);
			return table;
		}
	}

	/**
	 * 指定したテーブル名を持つテーブルオブジェクトを返します。
	 * @param tableName テーブル名
	 * @return テーブルオブジェクト
	 * @throws ReferenceNotFoundException 指定した名前のテーブルが存在しない場合
	 * @throws IllegalArgumentException テーブル名が完全修飾セル名であり、そのシート名部分がこのシートの名前と異なる場合
	 */
	public Table getTable(String tableName) throws ReferenceNotFoundException {

		String shortTableName = getShortName(tableName);
		// 指定されたグループを返す
		if (this._tables.containsKey(shortTableName)) {
			return this._tables.get(shortTableName);
		} else {
			// グループが存在しなかった場合、例外をスロー
			throw new ReferenceNotFoundException("tablename " + tableName + " is not found");
		}

	}

	/**
	 * このシートが保持しているすべてのテーブルへの参照を返します
	 * @return シートが保持しているすべてのテーブルへの参照
	 */
	public Set<Table> getTables() {
		return new HashSet<Table>(_tables.values());
	}


	/**
	 * テーブルを削除します。<br>
	 * 指定したテーブルが存在しない場合、何もしません。
	 * @param table テーブルオブジェクト
	 */
	public void deleteTable(Table table) {
		this._tables.remove(table.getName());
		// このグループを参照していた(そして参照できなくなる)セルがある場合、それらのセルを再計算する
		// TODO: テーブルでのrecalcReferencingCell 実装する
		//recalcReferencingCell(table);

	}


	/**
	 * テーブルを削除します。<br>
	 * 指定したテーブルが存在しない場合、何もしません。
	 * @param tableName テーブル名
	 */
	public void deleteTable(String tableName) {

		String shortTableName = getShortName(tableName);
		if (this._groups.containsKey(shortTableName)) {
			deleteGroup(_groups.get(shortTableName));
		}

	}



	/**
	 * 渡された文字列がシート名として正しいかチェックします。<br>
	 * 正しくない場合、例外がスローされます。
	 * @param sheetName チェックするシート名
	 * @throws IllegalArgumentException シート名として正しくない場合
	 */
	private void validateSheetName(String sheetName) throws IllegalArgumentException {
		if (! isValidSheetName(sheetName)) {
			throw new IllegalArgumentException("invalid sheet _name \"" + sheetName + "\"");
		}
	}

	/**
	 * 渡された文字列がシート名として正しいかチェックします。
	 * @param sheetName チェックするシート名
	 * @return 渡された文字列がシート名として正しい場合true、そうでない場合false
	 */
	public static boolean isValidSheetName(String sheetName) {
		return _sheetNamePattern.matcher(sheetName).matches();
	}

	/**
	 * 指定したグループを参照しているセルを再計算します
	 * @param group チェックするグループ
	 */
	private void recalcReferencingCell(Group group) {
		Resolver resolver = this.getBook().getResolver();
		Calculator calcurator = this.getBook().getCalculator();

		Set<Cell> referencingCells = resolver.getReferencingCells(group);
		for (Cell cell: referencingCells) {
			cell.setStatus(CellStatus.REQUIRE_CALCULATION);
			calcurator.calc(cell);
		}
	}

	/* (非 Javadoc)
	 * @see java.lang.Comparable#compareTo(java.lang.Object)
	 */
	public int compareTo(Sheet o) {
		return this.getName().compareTo(o.getName());
	}

	/* (非 Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		if (obj instanceof Sheet) {
			Sheet s = (Sheet)obj;
			
			// このシートのデフォルトMathContextオブジェクトが等価であること
			if (! this.getMathContext().equals(s.getMathContext())) {
				return false;
			}
			
			// シート内のセル数が同じで、それぞれのシートに同名のセルがあり、同名セル同士が等価であること
			if (this.getCells().size() != s.getCells().size()) {
				return false;
			}
			for (Cell myCell: this.getCells()) {
				if (s.cellExists(myCell.getName())) {
					if (! myCell.equals(s.getCell(myCell.getName()))) {
						return false;
					}
				} else {
					return false;
				}
			}
			// シート内のグループ数が同じで、それぞれのグループに属するセルが同じであること
			if (this.getGroups().size() != s.getGroups().size()) {
				return false;
			}
			for (Group myGroup: this.getGroups()) {
				if (s.groupExists(myGroup.getName())) {
					if (! myGroup.equals(s.getGroup(myGroup.getName()))) {
						return false;
					}
				}
			}
			// シート内のテーブル数が同じで、それぞれのテーブルに属するセルが同じであること
			if (this.getTables().size() != s.getTables().size()) {
				return false;
			}
			for (Table myTable: this.getTables()) {
				if (s.tableExists(myTable.getName())) {
					if (! myTable.equals(s.getTable(myTable.getName()))) {
						return false;
					}
				}
			}
		}
		return true;
	}

	/* (非 Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		return this.getName().hashCode();
	}

	/* (非 Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		// TODO 自動生成されたメソッド・スタブ
		return super.toString();
	}

}
