package com.nissy_ki_chi.jpicosheet.core;

public class ElasticSheetWrapper {

	private final Sheet _sheet;
	private final ConcurrentBookWrapper _cbw;

	/**
	 * 引数なしのコンストラクタは使用禁止
	 */
	@SuppressWarnings("unused")
	private ElasticSheetWrapper() {
		_sheet = null;
		_cbw = null;
	}

	/**
	 * Sheetオブジェクトを利用してオブジェクトを初期化します。
	 * @param sheet Sheetオブジェクト
	 */
	ElasticSheetWrapper(Sheet sheet, ConcurrentBookWrapper cbw) {
		if (sheet == null) {
			throw new NullPointerException("sheet is null");
		}
		_sheet = sheet;
		_cbw = cbw;
	}


	public ElasticCellWrapper getCell(String cellName) {
		Cell cell = _sheet.getCell(cellName);
		if (cell == null) {
			return null;
		}
		return new ElasticCellWrapper(cell, _cbw);
	}

	public ElasticCellWrapper addCell(String cellname) {
		_cbw.addCell(_sheet.getName() + "!" + cellname);
		return getCell(cellname);
	}

}
