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
package com.nissy_ki_chi.jpicosheet.util;

import com.nissy_ki_chi.jpicosheet.core.Book;
import com.nissy_ki_chi.jpicosheet.core.Element;
import com.nissy_ki_chi.jpicosheet.core.ReferenceNotFoundException;
import com.nissy_ki_chi.jpicosheet.core.Sheet;
import com.nissy_ki_chi.jpicosheet.core.Cell.CellStatus;
import com.nissy_ki_chi.jpicosheet.core.Cell.CellType;


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
