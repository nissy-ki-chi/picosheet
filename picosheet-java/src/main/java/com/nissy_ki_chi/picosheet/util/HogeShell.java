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
package com.nissy_ki_chi.picosheet.util;

import com.nissy_ki_chi.picosheet.core.Book;
import com.nissy_ki_chi.picosheet.core.Element;
import com.nissy_ki_chi.picosheet.core.ReferenceNotFoundException;
import com.nissy_ki_chi.picosheet.core.Sheet;
import com.nissy_ki_chi.picosheet.core.Cell.CellStatus;
import com.nissy_ki_chi.picosheet.core.Cell.CellType;


/**
 * シェルという考え方を検証するために作成した、テスト用シェル
 * @author yusuke nishikawa
 *
 */
public class HogeShell {

	Book _book;
	Sheet _sheet;



	public HogeShell() {
		_book = new Book("HogeShellBook");
		_sheet = _book.addSheet("HogeSheet");

	}


	public void setCellFromText(String[] dataText) {

		for (String line: dataText) {
			String[] parsedLine = line.split("\\s");
			if (parsedLine[0].toUpperCase().equals("N")) {
				_sheet.addCell(parsedLine[1]).setNumberValue(parsedLine[2]);
			} else if (parsedLine[0].toUpperCase().equals("F")) {
				_sheet.addCell(parsedLine[1]).setFormula(parsedLine[2]);
			} else {
				System.out.println("invalid line: " + line);
			}
		}
	}


	public Element getCellValue(String cellName) throws ReferenceNotFoundException {
		return _sheet.getCell(cellName).getValue();
	}

	public CellStatus getCellStatus(String cellName) throws ReferenceNotFoundException {
		return _sheet.getCell(cellName).getStatus();
	}

	public CellType getCellType(String cellName) throws ReferenceNotFoundException {
		return _sheet.getCell(cellName).getCellType();
	}

}
