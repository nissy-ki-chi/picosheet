package com.nissy_ki_chi.jpicosheet.core;

import java.util.HashSet;

public class ElasticBookWrapper {


	private final Book _book;
	private final ConcurrentBookWrapper _cbw;
	private final HashSet<ElasticSheetWrapper> _sheets;

	/**
	 * 引数なしのコンストラクタは使用禁止
	 */
	@SuppressWarnings("unused")
	private ElasticBookWrapper() {
		_book = null;
		_cbw = null;
		_sheets = null;
	}

	/**
	 * Bookオブジェクトを利用してオブジェクトを初期化します。
	 * @param book Bookオブジェクト
	 */
	public ElasticBookWrapper(Book book) {
		if (book == null) {
			throw new NullPointerException("book is null");
		}
		_book = book;
		_cbw = new ConcurrentBookWrapper(book);
		_sheets = new HashSet<ElasticSheetWrapper>();
	}



	public ElasticSheetWrapper getSheet(String sheetName) {
		Sheet sheet = _book.getSheet(sheetName);
		if (sheet == null) {
			return null;
		}
		ElasticSheetWrapper esw = new ElasticSheetWrapper(sheet, _cbw);
		_sheets.add(esw);
		return esw;
	}


	public ElasticSheetWrapper addSheet(String sheetName) {
		_cbw.addSheet(sheetName);
		return getSheet(sheetName);
	}


	public void deleteSheet(String sheetName) {
		Sheet sheet = _book.getSheet(sheetName);
		if (sheet == null) {
			return ;
		}


	}






}
