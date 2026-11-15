/*
 * Picosheet: Spreadsheet engine for Java Applications
 * Copyright (C) 2011-2026 yusuke nishikawa
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.nissy_ki_chi.picosheet.core;

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
