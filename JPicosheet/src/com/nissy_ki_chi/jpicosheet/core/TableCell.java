package com.nissy_ki_chi.jpicosheet.core;

public class TableCell extends Cell {

	/**
	 * このセルを保持しているRowオブジェクト 
	 */
	Row _row; 
	
	/**
	 * 行オブジェクトを指定してオブジェクトを作成します
	 * @param row このセルオブジェクトが属する行を表すオブジェクト
	 */
	TableCell(Row row) throws IllegalArgumentException {

		this._row = row;

		// メンバに値をセット
		this._formula = null;
		this._value = emptyElement;
	}
	
	@Override
	public String getName() {
		StringBuilder sb = new StringBuilder(_row.getChunk().getTable().getName());
		sb.append("R").append(getRowNumber()).append("C").append(getColNumber());
		return sb.toString();
	}

	@Override
	public String getLabel() {
		// ラベルは付けられない
		return null;
	}

	@Override
	public String getFullyQualifiedName() {
		return getSheet().getName() + "!" + getName();
	}

	@Override
	public Sheet getSheet() {
		return this._row.getChunk().getTable().getSheet();
	}

	@Override
	void setName(String cellName) {
		// セル名は変更不可。何もしない
	}

	@Override
	void setName(String cellName, boolean createForTableCell) throws IllegalArgumentException {
		// セル名は変更不可。何もしない
	}

	@Override
	public Cell setLabel(String newLabel) {
		// ラベルは指定不可。何もしない
		return null;
	}
	
	public static boolean isValidCellName(String cellName) {
		return true;
	}

	public static boolean isValidFullyQualifiedCellName(String fullyQualifiedCellName) {
		return true;
	}

	/**
	 * このセルが属するテーブルオブジェクトを返します。
	 * @return テーブルオブジェクト
	 */
	public Table getTable() {
		return _row.getChunk().getTable();
	}
	/**
	 * このセルが、このテーブルの何列目にあるかを返します。
	 * @return テーブルの列番号
	 */
	private int getColNumber() {
		for (int i = 0; i < _row.getColSize(); i++) {
			if (_row.getCols()[i] == this) return i + 1;
		}
		// ここには来ないはず
		assert(false);
		return -1;
	}
	
	private int getRowNumber() {
		return _row.getChunk().getRowNumber(_row);
	}

}
