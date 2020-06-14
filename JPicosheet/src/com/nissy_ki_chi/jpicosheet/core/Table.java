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
/**
 *
 */
package com.nissy_ki_chi.jpicosheet.core;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * セルの行列を表すオブジェクトです。<br>
 * 1つ1つのセルとして、セルの集合として、また行および列方向に順序を持った行列として扱うことができます。<br>
 *
 * テーブル名には末尾に<code>"#"</code>が付きます。テーブルの名前として<code>hoge</code>を使う場合、<code>"hoge#"</code>と指定します。<br>
 * テーブル名だけを指定した場合、それはテーブル全体を指定したことを意味します。たとえば大きさが3*3のテーブル<code>hoge#</code>を
 * 作成した場合、<code>hoge#</code>は9つのセルを含んでいます。<br>
 * テーブル内の特定のセルを指定する場合、"R1C1形式"のセルアドレス指定を行います。インデックスは0から始まるため、
 * <code>hoge#</code>の左上のセルを指定するには<code>hoge#R1C1</code>と指定します。この方法でテーブル内の単一のセルを指定した場合、
 * その扱いは通常のセルと同じです。
 *
 * @author yusuke nishikawa
 *
 */
public class Table implements CellGroupReference {

	/**
	 * テーブル名
	 */
	private String _name;

	/**
	 * このテーブルを持っているSheetオブジェクトへの参照
	 */
	private Sheet _sheet;
	
	/**
	 * このテーブルのカラム数
	 */
	private int _colSize;

	/**
	 * テーブル名のパターン文字列
	 */
	static final String TABLE_NAME_PATTERN = "[a-zA-Z_][a-zA-Z0-9_]*#";

	/**
	 * テーブル名の正規表現パターン
	 */
	private static Pattern _tableNamePattern = Pattern.compile(TABLE_NAME_PATTERN);

	/**
	 * 完全修飾テーブル名のパターン文字列
	 */
	static final String FULLY_QUALIFIED_TABLE_NAME_PATTERN = Sheet.SHEET_NAME_PATTERN + "!" + TABLE_NAME_PATTERN;

	/**
	 * 完全修飾テーブル名の正規表現パターン
	 */
	private static Pattern _fullyQualifiedTableNamePattern = Pattern.compile(FULLY_QUALIFIED_TABLE_NAME_PATTERN);

	/**
	 * アドレスのパターン文字列
	 */
	static final String ADDRESS_PATTERN = "(R[1-9]?[0-9]*C[1-9]?[0-9]+)";

	/**
	 * アドレスの正規表現パターン
	 */
	private static Pattern _addressPattern = Pattern.compile(ADDRESS_PATTERN);

	/**
	 * アドレス付きテーブル名のパターン文字列
	 */
	static final String TABLE_NAME_WITH_ADDRESS_PATTERN = TABLE_NAME_PATTERN + ADDRESS_PATTERN;

	/**
	 * アドレス付きテーブル名の正規表現パターン
	 */
	private static Pattern _tableNameWithAddressPattern = Pattern.compile(TABLE_NAME_PATTERN + ADDRESS_PATTERN);

	/**
	 * 完全修飾アドレス付きテーブル名のパターン文字列
	 */
	static final String FULLY_QUALIFIED_TABLE_NAME_WITH_ADDRESS_PATTERN =Sheet.SHEET_NAME_PATTERN + "!" +  TABLE_NAME_PATTERN + ADDRESS_PATTERN;

	/**
	 * 完全修飾アドレス付きテーブル名の正規表現パターン
	 */
	private static Pattern _fullyQualifiedtableNameWithAddressPattern = Pattern.compile(FULLY_QUALIFIED_TABLE_NAME_WITH_ADDRESS_PATTERN);




	/**
	 * 範囲のパターン文字列
	 */
	static final String RANGE_PARSE_PATTERN =
		"^(?:" +
		"(?:(R[0-9]+)(C[0-9]+)(:)(R[0-9]+)(C[0-9]+))|(?:(R[0-9]+)(C[0-9]+)(:)(R[0-9]+)(Cx))|(?:(R[0-9]+)(C[0-9]+)(:)(Rx)(C[0-9]+))|(?:(R[0-9]+)(C[0-9]+)(:)(Rx)(Cx))" + "|" +
		"(?:(R[0-9]+)(Cx))|(?:(Rx)(C[0-9]+))" + "|" +
		")$";

	static final String RANGE_PATTERN_BASE =
		"R[0-9]+C[0-9]+:R[0-9]+C[0-9]+|R[0-9]+C[0-9]+:R[0-9]+Cx|R[0-9]+C[0-9]+:RxC[0-9]+|R[0-9]+C[0-9]+:RxCx" + "|" +
		"R[0-9]+Cx|RxC[0-9]+";

	static final String RANGE_PATTERN =
		"^(?:" +
		RANGE_PATTERN_BASE +
		")$";

	/**
	 * 範囲のパース用の正規表現パターン
	 */
	private static Pattern _RangeParsePattern = Pattern.compile(RANGE_PARSE_PATTERN);

	/**
	 * 範囲の正規表現パターン
	 */
	private static Pattern _RangePattern = Pattern.compile(RANGE_PATTERN);

	/**
	 * 範囲付きテーブル名のパターン文字列
	 */
	static final String TABLE_NAME_WITH_RANGE_PATTERN =
		TABLE_NAME_PATTERN + "(?:" + RANGE_PATTERN_BASE + ")";

	/**
	 * 範囲付きテーブル名の正規表現パターン
	 */
	private static Pattern _tableNameWithRangePattern = Pattern.compile(TABLE_NAME_WITH_RANGE_PATTERN);

	/**
	 * 完全修飾範囲付きテーブル名のパターン文字列
	 */
	static final String FULLY_QUALIFIED_TABLE_NAME_WITH_RANGE_PATTERN =Sheet.SHEET_NAME_PATTERN + "!" +  TABLE_NAME_WITH_RANGE_PATTERN;

	/**
	 * 完全修飾範囲付きテーブル名の正規表現パターン
	 */
	private static Pattern _fullyQualifiedtableNameWithRangePattern = Pattern.compile(FULLY_QUALIFIED_TABLE_NAME_WITH_RANGE_PATTERN);

	/**
	 * テーブルの行方向のデータを保持するオブジェクトチェーンの、最初の要素
	 */
	private RowsChunk _rowsChunkHead;
	
	/**
	 * テーブルの行方向のデータを保持するオブジェクトチェーンの、最後の要素 
	 */
	private RowsChunk _rowsChunkTail;
	
	
	/**
	 * テーブル名とシートオブジェクト、カラムサイズを指定してオブジェクトを作成します。
	 * @param tableName テーブル名
	 * @param colSize テーブルの列数
	 * @param sheet このテーブルを持っているSheetオブジェクトへの参照
	 * @throws IllegalArgumentException
	 */
	Table(String tableName, int colSize, Sheet sheet) throws IllegalArgumentException {

		validateTableName(tableName);

		// colSizeは1以上でなければならない
		if (colSize < 1) {
			throw new IllegalArgumentException("invalid col size. " +
					" col=" + Integer.toString(colSize));
		}

		_name = tableName;
		_colSize = colSize;
		_rowsChunkHead = new RowsChunk(this, 1, null);
		_rowsChunkTail = _rowsChunkHead;
		_sheet = sheet;
	}
	
	/**
	 * 渡された文字列がテーブル名として正しいかチェックします。<br>
	 * 正しくない場合、例外がスローされます。
	 * @param tableName チェックするテーブル名
	 * @throws IllegalArgumentException テーブル名として正しくない場合
	 */
	private void validateTableName(String tableName) throws IllegalArgumentException {

		if (!isValidTableName(tableName)) {
			throw new IllegalArgumentException("invalid table name \"" + tableName + "\"");
		}
	}

	/**
	 * 渡された文字列がテーブル名として正しいかチェックします。
	 * @param tableName チェックするテーブル名
	 * @return テーブル名として正しい場合true、そうでない場合false
	 */
	public static boolean isValidTableName(String tableName) {

		return _tableNamePattern.matcher(tableName).matches();

	}

	/**
	 * 渡された文字列が完全修飾テーブル名として正しいかチェックします。
	 * @param cellName チェックするテーブル名
	 * @return テーブル名として正しい場合true、そうでない場合false
	 */
	public static boolean isValidFullyQualifiedTableName(String fullyQualifiedTableName) {

		return _fullyQualifiedTableNamePattern.matcher(fullyQualifiedTableName).matches();

	}
	
	/**
	 * 渡された文字列がアドレス名として正しいかチェックします。
	 * @param addressname チェックするアドレス名
	 * @return アドレス名として正しい場合true、そうでない場合false
	 */
	public static boolean isValidAddressName(String addressname) {
		return _addressPattern.matcher(addressname).matches();
	}

	/** 渡された文字列がアドレス付きテーブル名として正しいかチェックします。<
	 * @param tableNameWithAddress チェックするアドレス付きテーブル名
	 * @return アドレス付きテーブル名として正しい場合true、そうでない場合false
	 */
	public static boolean isValidTableNameWithAddress(String tableNameWithAddress) {

		return _tableNameWithAddressPattern.matcher(tableNameWithAddress).matches();

	}

	/** 渡された文字列が完全修飾アドレス付きテーブル名として正しいかチェックします。
	 * @param tableNameWithAddress チェックするアドレス付きテーブル名
	 * @return アドレス付きテーブル名として正しい場合true、そうでない場合false
	 */
	public static boolean isValidFullyQualifiedTableNameWithAddress(String fullyQualifiedTableNameWithAddress) {

		return _fullyQualifiedtableNameWithAddressPattern.matcher(fullyQualifiedTableNameWithAddress).matches();

	}

	/**
	 * 渡された文字列が範囲名として正しいかチェックします
	 * @param rangeName チェックする範囲名
	 * @return 範囲名として正しい場合true、そうでない場合false
	 */
	public static boolean isValidRangeName(String rangeName) {
		return _RangePattern.matcher(rangeName).matches();
	}

	/**
	 * 渡された文字列が範囲付きテーブル名として正しいかチェックします。
	 * @param tableNameWithRange チェックする範囲付きテーブル名
	 * @return 範囲付きテーブル名として正しい場合true、そうでない場合false
	 */
	public static boolean isValidTableNameWithRange(String tableNameWithRange) {

		return _tableNameWithRangePattern.matcher(tableNameWithRange).matches();

	}

	/**
	 * 渡された文字列が完全修飾範囲付きテーブル名として正しいかチェックします。
	 * @param tableNameWithRange チェックする完全修飾範囲付きテーブル名
	 * @return 完全修飾範囲付きテーブル名として正しい場合true、そうでない場合false
	 */
	public static boolean isValidFullyQualifiedTableNameWithRange(String fullyQualifiedTableNameWithRange) {

		return _fullyQualifiedtableNameWithRangePattern.matcher(fullyQualifiedTableNameWithRange).matches();
	}


	/**
	 * 渡された文字列が範囲名として正しいかチェックします。<br>
	 * 正しくない場合、例外がスローされます。
	 * @param rangeName チェックする範囲名
	 * @throws IllegalArgumentException テーブル名として正しくない場合
	 */
	private void validateRangeName(String rangeName) throws IllegalArgumentException {

		if (!isValidRangeName(rangeName)) {
			throw new IllegalArgumentException("invalid range name \"" + rangeName + "\"");
		}
	}

	/**
	 * このオブジェクトを持つSheetオブジェクトを返します。
	 * @return このオブジェクトを持つSheetオブジェクト
	 */
	Sheet getSheet() {
		return _sheet;
	}
	
	/**
	 * このテーブルの名前を返します
	 * @return テーブルの名前
	 */
	public String getName() {
		return _name;
	}

	/**
	 * このテーブルの完全修飾名を返します
	 * @return このテーブルの完全修飾名
	 */
	public String getFullyQualifiedName() {
		return _sheet.getName() + "!" + _name;
	}

	/**
	 * このテーブルの行方向の末尾に新しい行を1行追加します。
	 * @return このテーブル
	 */
	public Table addRow() {
		return addRow(1);
	}

	/**
	 * このテーブルの行方向の末尾に新しい行を指定した行数だけ追加します。
	 * @param addRowCount 追加する行数
	 * @return このテーブル
	 * @throws IllegalArgumentException 行数の指定が1未満の場合
	 */
	public Table addRow(int addRowCount) {

		// addRowCountが1以下であってはならない
		if (addRowCount < 1) {
			throw new IllegalArgumentException("invalid addRowCount: " + Integer.toString(addRowCount));
		}

		_rowsChunkTail = _rowsChunkTail.addRow(addRowCount);
		return this;
	}

	/**
	 * このテーブルの列方向の末尾に新しい列を1列追加します。
	 * @return このテーブル
	 */
	public Table addColumn() {
		return addColumn(1);
	}

	/**
	 * このテーブルの列方向の末尾に新しい列を指定した列数だけ追加します。
	 * @param addColumnCount 追加する列数
	 * @return このテーブル
	 * @throws IllegalArgumentException 列数の指定が0未満の場合
	 */
	public Table addColumn(int addColumnCount) {

		// addColumnCountが1以下であってはならない
		if (addColumnCount < 1) {
			throw new IllegalArgumentException("ingalid addColumnCount: " + Integer.toString(addColumnCount));
		}
		
		// すべての行に対し、末尾に列を追加する
		_colSize += addColumnCount;
		RowsChunk targetChunk = _rowsChunkHead;
		while (targetChunk != null) {
			Row targetRow;
			for (int i = 0; i < targetChunk.getFilledCount(); i++) {
				targetRow = targetChunk.getRowByIndex(i);
				targetRow.addColumn(addColumnCount);
			}
			targetChunk = targetChunk.getAfter();
		}
		return this;
	}
	
	/**このテーブルの指定した位置に、指定した行数の行を挿入します。
	 * @param rowPos 挿入する位置
	 * @param insertRowCount 挿入する行数
	 * @return このテーブル
	 */
	public Table insertRow(int rowPos, int insertRowCount) {
	
		if (rowPos < 0) throw new IllegalArgumentException("rowPosは0以上でなければなりません。" + rowPos);
		if (insertRowCount < 1) throw new IllegalArgumentException("insertRowCountは1以上でなければなりません。" + insertRowCount);

		RowsChunk targetChunk = this._rowsChunkHead;
		while (targetChunk != null) {
			// 目的の行を含むRowsChunkが見つかるか、最後のRowsChunkまで来たらtargetChunk決定
			if (rowPos <= targetChunk.getLastRowNum()) break;
			if (targetChunk.getAfter() == null) break;
			targetChunk = targetChunk.getAfter();
		}
	
		// このRowsChunkの挿入ポイントより後ろにRowがあれば、それを新しいRowsChunkに移す
		int cutPoint = rowPos - targetChunk.getBeginRowNum();
		RowsChunk devidedChunk = null;
		if (cutPoint < targetChunk.getFilledCount()) {
			devidedChunk = new RowsChunk(this, rowPos, targetChunk, cutPoint);
			targetChunk.eraseRowsAfter(cutPoint);
		}
		
		// 行を挿入…targetChunkに対して行の追加を行う
		RowsChunk addRowLast = targetChunk.addRow(insertRowCount);

		// 挿入ポイント以降の行番号情報をずらす
		RowsChunk renumTarget; 
		if (devidedChunk != null) {
			addRowLast.setAfter(devidedChunk);
			renumTarget = devidedChunk;
		} else {
			addRowLast.setAfter(targetChunk.getAfter());
			renumTarget = addRowLast;
		}
		do {
			renumTarget.setBeginRowNum(renumTarget.getBeginRowNum() + insertRowCount);
			_rowsChunkTail = renumTarget;
		} while ((renumTarget = renumTarget.getAfter()) != null);
		
		return this;
	}
	
	
	
	/**
	 * このテーブルの指定した位置に1行挿入します。挿入位置がテーブルの行数より大きい場合、テーブルの末尾に1行追加されます。
	 * @param rowPos 挿入する位置
	 * @return このテーブル
	 */
	public Table insertRow(int rowPos) {
		if ((0 <= rowPos) && (this.getRowSize() < rowPos)) return addRow();
		return insertRow(rowPos, 1);
	}


	/**このテーブルの指定した位置に1列挿入します。
	 * @param rowPos 挿入する位置
	 * @return このテーブル
	 */
	public Table insertColumn(int columnPos) {

		return insertColumn(columnPos, 1);
	}

	/**このテーブルの指定した位置に、指定した列数の列を挿入します。
	 * @param columnPos 挿入する位置
	 * @param size 挿入する列数
	 * @return このテーブル
	 */
	public Table insertColumn(int columnPos, int size) {
		// columnPosがマイナスであってはならない
		if (columnPos < 0) {
			throw new IllegalArgumentException("ingalid columnPos: " + Integer.toString(columnPos));
		}
		// columnPosがテーブルの最大列以上であってはならない
		if (_colSize <= columnPos) {
			throw new IllegalArgumentException("invalid columnPos: " + Integer.toString(columnPos) + " table columnsize: " + Integer.toString(_colSize));
		}
		// insertColumnCountが1以下であってはならない
		if (size < 1) {
			throw new IllegalArgumentException("ingalid insertColumnCount: " + Integer.toString(size));
		}
		
		// すべての行に対し、指定位置に列を挿入する
		_colSize += size;
		RowsChunk targetChunk = _rowsChunkHead;
		while (targetChunk != null) {
			Row targetRow;
			for (int i = 0; i < targetChunk.getFilledCount(); i++) {
				targetRow = targetChunk.getRowByIndex(i);
				targetRow.insertColumn(columnPos, size);
			}
			targetChunk = targetChunk.getAfter();
		}
		return this;
	}


	/**
	 * このテーブルの行数を返します
	 * @return このテーブルの行数
	 */
	public int getRowSize() {
		return this._rowsChunkTail.getLastRowNum();
	}

	/**
	 * このテーブルの列数を返します
	 * @return このテーブルの列数
	 */
	public int getColSize() {
		return _colSize;
	}

	/**
	 * このテーブル全体を現すRangeオブジェクトを返します。<br>
	 * これは、<code>getRange("R1C1:RxCx")</code>を指定したのと同じです。
	 * @return Rangeオブジェクト
	 */
	public Range getRange() {
		return getRange("R1C1:RxCx");
	}

	/**
	 * このテーブルの指定した範囲を表すRangeオブジェクトを返します。<br>
	 * テーブルの範囲指定として以下のような指定が可能です。<br>
	 * <table border="1">
	 * <tr>
	 * <th>指定</th><th>説明</th>
	 * </tr>
	 * <tr><td>R1C1:RxCx</td><td>このテーブルすべて</td></tr>
	 * <tr><td>R3Cx</td><td>3行目(行インデックス2)のすべての列</td></tr>
	 * <tr><td>RxC4</td><td>4列目(列インデックス3)のすべての行</td></tr>
	 * <tr><td>R3C1:R6Cx</td><td>3行目から6行目のすべての列</td></tr>
	 * <tr><td>R1C3:RxC6</td><td>3列目から6列目のすべての行</td></tr>
	 * <tr><td>R1C1:R1C1</td><td>1行1列から1行1列の1x1の範囲、つまりtableの左上のセル</td></tr>
	 * <tr><td>R4C3:R6C5</td><td>4行3列から6行4列の3x3の範囲</td></tr>
	 * <tr><td>R4C3:R6Cx</td><td>4行3列から5行最大列まで</td></tr>
	 * <tr><td>R4C3:RxC7</td><td>4行3列から最大行7列まで</td></tr>
	 * <tr><td>R4C3:RxCx</td><td>4行3列から最大行最大列まで</td></tr>
	 * </table>
	 * @param range このテーブルの範囲を表す文字列
	 * @return Rangeオブジェクト
	 * @throws IllegalArgumentException rangeに指定されたテーブル範囲指定文字列が正しくない場合
	 */
	public Range getRange(String range) throws IllegalArgumentException {

		validateRangeName(range);

		String rowFromStr = "";
		String colFromStr = "";
		String rowToStr = "";
		String colToStr = "";

		int rowFrom = 0;
		int colFrom = 0;
		int rowTo = 0;
		int colTo = 0;

		// range からセル範囲を得る
		Matcher matcher = _RangeParsePattern.matcher(range);
		List<String> tokens = new ArrayList<String>();

		while (matcher.find()) {
			for (int i = 1; i <= matcher.groupCount(); i++) {
				if (matcher.group(i) != null) {
					tokens.add(matcher.group(i));
				}
			}
		}

		switch (tokens.size()) {
		case 2:
			// テーブル全体、あるいは一行、あるいは1列
			rowFromStr = tokens.get(0).substring(1);
			colFromStr = tokens.get(1).substring(1);

//			// "RxCx"の場合
//			if (rowFromStr.equals("x") && colFromStr.equals("x")) {
//				rowFrom = _rows.size() - 1;
//				rowTo = rowFrom;
//				colFrom = _colSize - 1;
//				colTo = colFrom;
//				return new Range(this, rowFrom, colFrom, rowTo, colTo);
//			}

			// RnCxあるいはRxCnの場合、1行もしくは1列選択
			if (rowFromStr.equals("x")) {
				rowFrom = 1;
				rowTo = _rowsChunkTail.getLastRowNum();
			} else {
				rowFrom = Integer.parseInt(rowFromStr);
				rowTo = rowFrom;
			}
			if (colFromStr.equals("x")) {
				colFrom = 1;
				colTo = _colSize;
			} else {
				colFrom = Integer.parseInt(colFromStr);
				colTo = colFrom;
			}
			break;
		case 5:
			// それ以外の範囲
			rowFromStr = tokens.get(0).substring(1);
			colFromStr = tokens.get(1).substring(1);
			// 3番目の要素は":"なので無視
			rowToStr = tokens.get(3).substring(1);
			colToStr = tokens.get(4).substring(1);

			// FromとToの指定がある場合、From側には必ず数字が入っている
			rowFrom = Integer.parseInt(rowFromStr);
			colFrom = Integer.parseInt(colFromStr);
			// To側は空の指定の場合がある
			if (rowToStr.equals("x")) {
				rowTo = _rowsChunkTail.getLastRowNum();
			} else {
				rowTo = Integer.parseInt(rowToStr);
			}
			if (colToStr.equals("x")) {
				colTo = _colSize;
			} else {
				colTo = Integer.parseInt(colToStr);
			}
			break;
		default:
			// それ以外の場合、validateRangeName()でチェックできていない。RangePatternの正規表現がおかしい。
			assert false: "unknown range " + range;
		}
		// レンジを作成して返す
		return new Range(this, rowFrom, colFrom, rowTo, colTo);
	}

	/**
	 * このテーブルの指定された位置のCellオブジェクトを返します
	 * @param rowPos 行位置
	 * @param colPos 列位置
	 * @return 指定された位置のCellオブジェクト
	 */
	public Cell getCell(int rowPos, int colPos) {
		if (getRowSize() < rowPos) throw new IllegalArgumentException("rowPosが大きすぎます。" + rowPos);
		if (_colSize < colPos) throw new IllegalArgumentException("colPosが大きすぎます。" + colPos);
		
		RowsChunk targetChunk = this._rowsChunkHead;
		while (targetChunk != null) {
			if (rowPos <= targetChunk.getLastRowNum()) {
				Row row = targetChunk.getRowByIndex(rowPos - targetChunk.getBeginRowNum());
				return row.getCellByPosition(colPos);
			}
			targetChunk = targetChunk.getAfter();
		}
		// ここには来ない
		assert(false);
		return null;
	}

	/**
	 * このテーブルの指定された位置のCellオブジェクトを返します
	 * @param cellAddress このテーブルのセルアドレス(R1C1形式)
	 * @return 指定された位置のCellオブジェクト。
	 */
	public Cell getCell(String cellAddress) {
		if (!TableCell.isValidCellName(cellAddress)) {
			throw new IllegalArgumentException("ingalid cellAddress: " + cellAddress);
		}

		String[] address = cellAddress.split("[RrCc]");
		int rowPos = Integer.parseInt(address[1]);
		int colPos = Integer.parseInt(address[2]);
		if (getRowSize() < rowPos) throw new IllegalArgumentException("rowPosが大きすぎます。" + rowPos);
		if (_colSize < colPos) throw new IllegalArgumentException("colPosが大きすぎます。" + colPos);

		if ((colPos <= getColSize()) && (rowPos <= getRowSize())) {
			return getCell(rowPos, colPos);
		}
		return null;
	}

	/* (非 Javadoc)
	 * @see com.nissy_ki_chi.jpicosheet.core.CellGroupReference#getCells()
	 */
	public Collection<Cell> getCells() {
		List<Cell> cells = new ArrayList<Cell>(this._colSize * this._rowsChunkTail.getLastRowNum());
		RowsChunk targetChunk = this._rowsChunkHead;
		while (targetChunk != null) {
			for (int i = 0; i < targetChunk.getFilledCount(); i++) {
				Row row = targetChunk.getRowByIndex(i);
					for (Cell c: row.getCols()) {
						cells.add(c);
				}
			}
			targetChunk = targetChunk.getAfter();
		}
		return cells;
	}

//	/* (非 Javadoc)
//	 * @see java.lang.Object#equals(java.lang.Object)
//	 */
//	@Override
//	public boolean equals(Object obj) {
//		if (obj instanceof Table) {
//			Table t = (Table) obj;
//			if (this._colSize == t._colSize) {
//				if (this._rows.size() == t._rows.size()) {
//					// テーブル内のすべてのセルを比較 サイズが大きいととても効率が悪い
//					for (int rowNum = 0; rowNum < this._rows.size(); rowNum++) {
//						List<Cell> myRow = _rows.get(rowNum);
//						List<Cell> tRow = t._rows.get(rowNum);
//						for (int colNum = 0; colNum < myRow.size(); colNum++) {
//							if (! myRow.get(colNum).equals(tRow.get(colNum))) {
//								return false;
//							}
//						}
//					}
//					return true;
//				}
//			}
//		}
//		return false;
//	}
}
