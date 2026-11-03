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
package test;

import java.math.BigDecimal;

import com.nissy_ki_chi.jpicosheet.core.Book;
import com.nissy_ki_chi.jpicosheet.core.Cell;
import com.nissy_ki_chi.jpicosheet.core.Range;
import com.nissy_ki_chi.jpicosheet.core.Sheet;
import com.nissy_ki_chi.jpicosheet.core.Table;

import junit.framework.TestCase;



public class RangeTest extends TestCase {

Book book;
Sheet sheet;
Table table;


	protected void setUp() throws Exception {
		super.setUp();
		book= new Book("myBook");
		sheet = book.addSheet("mySheet");
		table = sheet.addTable("myTable#", 10, 10);

		for (int rowPos = 1; rowPos <= table.getRowSize(); rowPos++) {
			for (int colPos = 1; colPos <= table.getColSize(); colPos++) {
				sheet.getTable("myTable#").getCell(rowPos, colPos)
					.setStringValue("R" + Integer.toString(rowPos) + "C" + Integer.toString(colPos));
			}
		}
	}

	protected void tearDown() throws Exception {
		super.tearDown();
	}



	public void testGetCell() {
		Range range = table.getRange("R1C1:R5C5");

		assertEquals("R1C1", range.getCell(1,1).getValueString());
		assertEquals("R2C2", range.getCell(2,2).getValueString());
		assertEquals("R5C5", range.getCell(5,5).getValueString());
		
	}

	public void testIllegalRangeGetCell() {
		// Rangeの範囲外を指定した場合エラーになること
		Range range = table.getRange("R1C1:R5C5");

		try {
			range.getCell(0,0);
		} catch (IllegalArgumentException e) {
			// OK
		}
		try {
			range.getCell(-1,-1);
		} catch (IllegalArgumentException e) {
			// OK
		}
		try {
			range.getCell(range.getRowSize() + 1, range.getColSize() + 1);
		} catch (IllegalArgumentException e) {
			// OK
		}
	}

	public void testGetRowArray() {
		// Rangeから行の配列を得られること
		Range range = table.getRange("R1C1:R5C6");

		Cell[] rowArray = range.getRowArray(1);
		assertEquals(5, rowArray.length);
	}

	public void testGetColArray() {
		// Rangeから列の配列を得られること
		Range range = table.getRange("R1C1:R5C6");

		Cell[] colArray = range.getColArray(1);
		assertEquals(6, colArray.length);
	}

	public void testGetRowArrayValue() {
		// Rangeを通して得るセルが、対応するTableのセルと同一であること
		Range range = table.getRange("R2C2:R6C6");

		assertEquals("R2C2", range.getCell(1, 1).getValueString());
		assertTrue(range.getCell(1, 1) == table.getCell(2,2));
		
		assertEquals("R3C3", range.getCell(2, 2).getValueString());
		assertTrue(range.getCell(2, 2) == table.getCell(3,3));
		
		assertEquals("R6C6", range.getCell(5, 5).getValueString());
		assertTrue(range.getCell(5, 5) == table.getCell(6,6));
		
	}

	public void testFormulaCellReference() {
		// Rangeを通した場合でも、セルアドレスはTableを基準としていること
		Range range = table.getRange("R2C2:R6C6");

		table.getCell(1, 1).setNumberValue("1");
		table.getCell(1, 2).setNumberValue("2");
		range.getCell(1, 1).setFormula("R1C1+R1C2");
		assertEquals(new BigDecimal(3), range.getCell(1, 1).getValue().getNumber());
		
		
	}

}
