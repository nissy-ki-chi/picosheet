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
import java.text.SimpleDateFormat;
import java.util.Date;

import com.nissy_ki_chi.jpicosheet.core.Book;
import com.nissy_ki_chi.jpicosheet.core.Cell;
import com.nissy_ki_chi.jpicosheet.core.Element;
import com.nissy_ki_chi.jpicosheet.core.ReferenceNotFoundException;
import com.nissy_ki_chi.jpicosheet.core.Sheet;
import com.nissy_ki_chi.jpicosheet.core.Cell.CellType;
import com.nissy_ki_chi.jpicosheet.core.Element.ElementType;

import junit.framework.TestCase;

public class CellTest extends TestCase {

	Book book;
	Sheet sheet;
	Cell cell;

	public CellTest(String name) {
		super(name);
	}

	protected void setUp() throws Exception {
		super.setUp();
		book= new Book("myBook");
		sheet = book.addSheet("mySheet");
		cell = sheet.addCell("targetCell");

	}

	protected void tearDown() throws Exception {
		super.tearDown();
	}

	/**
	 * セル作成時のテスト(主にセル名のテスト)
	 * @throws Exception
	 */
	public void testCell() throws Exception {
		sheet.addCell("newcell");
		sheet.addCell("NEWCELL");
		sheet.addCell("newCell");
		sheet.addCell("newCell123");
		sheet.addCell("a");
		sheet.addCell("_newcell");
		sheet.addCell("_newcell_");
		sheet.addCell("_________________newcell");
		sheet.addCell("new_cell_name_with_underscore");
		sheet.addCell("very_long_cell_name_aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa");
		try {
			sheet.addCell("");
			fail("空文字のセル名でエラーにならない");
		} catch (Exception e) {
			// OK
		}
		try {
			sheet.addCell("new cell");
			fail("中にスペースを含むセル名でエラーにならない");
		} catch (Exception e) {
			// OK
		}
		try {
			sheet.addCell("new	cell");
			fail("中にタブを含むセル名でエラーにならない");
		} catch (Exception e) {
			// OK
		}
		try {
			sheet.addCell(" newcell ");
			fail("前後にスペースを含むセル名でエラーにならない");
		} catch (Exception e) {
			// OK
		}
		try {
			sheet.addCell("	newcell	");
			fail("前後にタブを含むセル名でエラーにならない");
		} catch (Exception e) {
			// OK
		}
		try {
			sheet.addCell("日本語");
			fail("日本語のセル名でエラーにならない");
		} catch (Exception e) {
			// OK
		}
		try {
			sheet.addCell("1122");
			fail("数字のみのセル名でエラーにならない");
		} catch (Exception e) {
			// OK
		}
		try {
			sheet.addCell("1abc");
			fail("先頭文字が数字のセル名でエラーにならない");
		} catch (Exception e) {
			// OK
		}
		try {
			sheet.addCell("=abc");
			fail("先頭文字が記号のセル名でエラーにならない");
		} catch (Exception e) {
			// OK
		}
	}

	public void testGetFormula() throws Exception {
		cell.setFormula("1+1");
		assertEquals("=1+1", cell.getFormula());

		cell.setValue("=1+1");
		assertEquals("=1+1", cell.getFormula());

		cell.setValue("=1+1*");
		assertEquals(Element.ElementType.ERROR, cell.getValue().getType());
		assertEquals("=1+1*", cell.getFormula());
		cell.setValue("=1+1-nonexistenceref");
		assertEquals(Element.ElementType.ERROR, cell.getValue().getType());
		assertEquals("=1+1-nonexistenceref", cell.getFormula());

		cell.setNumberValue("123");
		assertEquals(null, cell.getFormula());
	}

	public void testGetName() {
		assertEquals("targetCell", cell.getName());
		assertEquals("mySheet!targetCell", cell.getFullyQualifiedName());
	}


	public void testGetSheet() {
		assertEquals(cell.getSheet(), sheet);
	}

	public void testGetDate() {
		cell.setValue("2014/01/01 12:34:56.123");
		assertEquals("2014/01/01 12:34:56.123", new SimpleDateFormat("yyyy/MM/dd HH:mm:ss.SSS").format(cell.getValue().getDate()));
		cell.setValue("2014/01/01 12:34:56");
		assertEquals("2014/01/01 12:34:56", cell.getValueString());
		cell.setValue("2014/01/01 12:34");
		assertEquals("2014/01/01 12:34:00", cell.getValueString());
		cell.setValue("2014/01/01 12");
		assertEquals("2014/01/01 12:00:00", cell.getValueString());
		cell.setValue("2014/01/01");
		assertEquals("2014/01/01 00:00:00", cell.getValueString());
		cell.setValue("12:34:56.123");
		assertEquals("1970/01/01 12:34:56", cell.getValueString());
		cell.setValue("12:34:56");
		assertEquals("1970/01/01 12:34:56", cell.getValueString());
		cell.setValue("12:34");
		assertEquals("1970/01/01 12:34:00", cell.getValueString());
		cell.setValue("1953/01/01 22:32:42.456");
		assertEquals("1953/01/01 22:32:42.456", new SimpleDateFormat("yyyy/MM/dd HH:mm:ss.SSS").format(cell.getValue().getDate()));

		cell.setValue("2014-01-01 12:34:56.123");
		assertEquals("2014-01-01 12:34:56.123", new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS").format(cell.getValue().getDate()));
		cell.setValue("2014-01-01 12:34:56");
		assertEquals("2014/01/01 12:34:56", cell.getValueString());
		cell.setValue("2014-01-01 12:34");
		assertEquals("2014/01/01 12:34:00", cell.getValueString());
		cell.setValue("2014-01-01 12");
		assertEquals("2014/01/01 12:00:00", cell.getValueString());
		cell.setValue("2014-01-01");
		assertEquals("2014/01/01 00:00:00", cell.getValueString());
		cell.setValue("12:34:56.123");
		assertEquals("1970/01/01 12:34:56", cell.getValueString());
		cell.setValue("12:34:56");
		assertEquals("1970/01/01 12:34:56", cell.getValueString());
		cell.setValue("12:34");
		assertEquals("1970/01/01 12:34:00", cell.getValueString());
		cell.setValue("1953-01-01 22:32:42.456");
		assertEquals("1953-01-01 22:32:42.456", new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS").format(cell.getValue().getDate()));

	}

	public void testGetValue() {
		cell.setNumberValue("1234");
		assertEquals(cell.getValue(), Element.newElement(ElementType.NUMBER, new BigDecimal("1234")));
	}

	public void testGetValueString() {
		cell.setNumberValue("1234");
		assertEquals(cell.getValueString(), "1234");
		cell.setNumberValue("1234.567");
		assertEquals(cell.getValueString(), "1234.567");
		cell.setFormula("1+2+3");
		assertEquals(cell.getValueString(), "6");
	}

	public void testCompareEqual() {
		cell.setNumberValue("1");
		assertEquals(cell.getValue(), Element.newElement(ElementType.NUMBER, new BigDecimal("1")));
		cell.setNumberValue("12345677890");
		assertEquals(cell.getValue(), Element.newElement(ElementType.NUMBER, new BigDecimal("12345677890")));
		cell.setNumberValue("1234.56789");
		assertEquals(cell.getValue(), Element.newElement(ElementType.NUMBER, new BigDecimal("1234.56789")));
		cell.setStringValue("abcdefgABCDEFG");
		assertEquals(cell.getValue(), Element.newElement(ElementType.STRING, "abcdefgABCDEFG"));

		sheet.addCell("emptyCell1");
		try {
			assertEquals(sheet.getCell("emptyCell1").getValue(), Element.newElement(ElementType.EMPTY));
		} catch (ReferenceNotFoundException e1) {
			fail();
		}
		sheet.addCell("emptyCell2");
		try {
			assertEquals(sheet.getCell("emptyCell1").getValue(), sheet.getCell("emptyCell2").getValue());
		} catch (ReferenceNotFoundException e) {
			fail();
		}
	}

	public void testSetNumberValue() {
		cell.setNumberValue("10");
		assertEquals(cell.getValue().getNumber(), new BigDecimal("10"));
		cell.setNumberValue("100000000000");
		assertEquals(cell.getValue().getNumber(), new BigDecimal("100000000000"));
		cell.setNumberValue("-10");
		assertEquals(cell.getValue().getNumber(), new BigDecimal("-10"));
		cell.setNumberValue("-100000000000");
		assertEquals(cell.getValue().getNumber(), new BigDecimal("-100000000000"));
		cell.setNumberValue("0.1");
		assertEquals(cell.getValue().getNumber(), new BigDecimal("0.1"));
		cell.setNumberValue("-0.1");
		assertEquals(cell.getValue().getNumber(), new BigDecimal("-0.1"));
		cell.setNumberValue("0.000000000001");
		assertEquals(cell.getValue().getNumber(), new BigDecimal("0.000000000001"));
		cell.setNumberValue("-0.000000000001");
		assertEquals(cell.getValue().getNumber(), new BigDecimal("-0.000000000001"));
	}

	public void testSetNumberValue2() {
		try {
			cell.setNumberValue("1.2.");
		} catch (NumberFormatException e) {
			// OK
			return;
		}
		fail();
	}

	public void testSetNumberValue3() {
		try {
			cell.setNumberValue("1..2");
		} catch (NumberFormatException e) {
			// OK
			return;
		}
		fail();
	}
	public void testSetStringValue() {
		cell.setStringValue("abcde");
		assertEquals(cell.getValue().getString(), "abcde");
	}

	public void testSetBooleanValue() {
		cell.setBooleanValue(true);
		assertEquals(ElementType.BOOLEAN, cell.getValue().getType());
		assertEquals(new Boolean(true), cell.getValue().getBoolean());
		assertEquals("BOOLEAN:true", cell.getValue().toString());
		cell.setBooleanValue(false);
		assertEquals(ElementType.BOOLEAN, cell.getValue().getType());
		assertEquals(new Boolean(false), cell.getValue().getBoolean());
		assertEquals("BOOLEAN:false", cell.getValue().toString());

	}

	public void testSetDateValue() {
		Date date1 = new Date();
		Date date2 = new Date(date1.getTime());
		cell.setDateValue(date1);
		assertEquals(ElementType.DATE, cell.getValue().getType());
		assertEquals(date1, cell.getValue().getDate());
		assertEquals(date2, cell.getValue().getDate());
		assertEquals("DATE:" + new SimpleDateFormat("yyyy/MM/dd HH:mm:ss").format(date1), cell.getValue().toString());

	}

	public void testEquals() {
		Cell c1 = sheet.addCell("compareCell1");
		Cell c2 = sheet.addCell("compareCell2");
		assertEquals(true, c1.equals(c1));
		assertEquals(true, c1.equals(c2));
		assertEquals(true, c2.equals(c1));

		c1.setNumberValue("1");
		c2.setNumberValue("1");
		assertEquals(true, c1.equals(c1));
		assertEquals(true, c1.equals(c2));
		assertEquals(true, c2.equals(c1));

		c1.setNumberValue("123");
		c2.setNumberValue("456");
		assertEquals(true, c1.equals(c1));
		assertEquals(false, c1.equals(c2));
		assertEquals(false, c2.equals(c1));

	}

	public void testCompare() {
		Cell c1 = sheet.addCell("compareCell1");
		Cell c2 = sheet.addCell("compareCell2");

		assertEquals(0, c1.compareTo(c1));
		assertEquals(-1, c1.compareTo(c2));
		assertEquals(1, c2.compareTo(c1));
	}

	public void testSetValue() {

		Cell c1 = sheet.addCell("c1");
		Cell c2 = sheet.addCell("c2");
		c1.setValue("");
		assertEquals(CellType.EMPTY, c1.getCellType());
		c1.setValue("123");
		assertEquals(CellType.NUMBER, c1.getCellType());
		c1.setValue("-123");
		assertEquals(CellType.NUMBER, c1.getCellType());
		c1.setValue("123.456");
		assertEquals(CellType.NUMBER, c1.getCellType());
		c1.setValue(".000234");
		assertEquals(CellType.NUMBER, c1.getCellType());

		c1.setValue("=1+2");
		assertEquals(CellType.FORMULA, c1.getCellType());
		assertEquals(Element.ElementType.NUMBER, c1.getValue().getType());
		c1.setValue("=0+0");
		assertEquals(CellType.FORMULA, c1.getCellType());
		assertEquals(Element.ElementType.NUMBER, c1.getValue().getType());
		c1.setValue("=1+*/1");
		assertEquals(CellType.FORMULA, c1.getCellType());
		assertEquals(Element.ElementType.ERROR, c1.getValue().getType());
		assertEquals(Element.ErrorType.INVALID_FORMULA, c1.getValue().getErrorType());
		c1.setValue("=1+1+");
		assertEquals(CellType.FORMULA, c1.getCellType());
		assertEquals(Element.ElementType.ERROR, c1.getValue().getType());
		assertEquals(Element.ErrorType.INVALID_FORMULA, c1.getValue().getErrorType());
		c1.setValue("=1+1(");
		assertEquals(CellType.FORMULA, c1.getCellType());
		assertEquals(Element.ElementType.ERROR, c1.getValue().getType());
		assertEquals(Element.ErrorType.INVALID_FORMULA, c1.getValue().getErrorType());

		c1.setValue("=c1+1");
		assertEquals(CellType.FORMULA, c1.getCellType());
		assertEquals(Element.ElementType.ERROR, c1.getValue().getType());
		assertEquals(Element.ErrorType.CIRCULAR_REFERENCE, c1.getValue().getErrorType());
		c1.setValue("=c2+1");
		assertEquals(CellType.FORMULA, c1.getCellType());
		assertEquals(Element.ElementType.NUMBER, c1.getValue().getType());
		c1.setValue("=nonexistenceCell+1");
		assertEquals(CellType.FORMULA, c1.getCellType());
		assertEquals(Element.ElementType.ERROR, c1.getValue().getType());
		assertEquals(Element.ErrorType.INVALID_REFERENCES, c1.getValue().getErrorType());

		c1.setValue("abcde");
		assertEquals(CellType.STRING, c1.getCellType());
		c1.setValue("あいうえお");
		assertEquals(CellType.STRING, c1.getCellType());

		c1.setValue("2014/01/01 12:34:56:123");
		assertEquals(CellType.DATE, c1.getCellType());
		c1.setValue("2014-01-01 12:34:56:123");
		assertEquals(CellType.DATE, c1.getCellType());
		c1.setValue("2014/01/01 12:34:56");
		assertEquals(CellType.DATE, c1.getCellType());
		c1.setValue("2014-01-01 12:34:56");
		assertEquals(CellType.DATE, c1.getCellType());
		c1.setValue("2014/01/01 12:34");
		assertEquals(CellType.DATE, c1.getCellType());
		c1.setValue("2014-01-01 12:34");
		assertEquals(CellType.DATE, c1.getCellType());
		c1.setValue("2014/01/01 12");
		assertEquals(CellType.DATE, c1.getCellType());
		c1.setValue("2014-01-01 12");
		assertEquals(CellType.DATE, c1.getCellType());
		c1.setValue("2014/01/01");
		assertEquals(CellType.DATE, c1.getCellType());
		c1.setValue("2014-01-01");
		assertEquals(CellType.DATE, c1.getCellType());
		c1.setValue("12:34:56.123");
		assertEquals(CellType.DATE, c1.getCellType());
		c1.setValue("12:34:56");
		assertEquals(CellType.DATE, c1.getCellType());
		c1.setValue("12:34");
		assertEquals(CellType.DATE, c1.getCellType());

	}

	public void testLabel() {
		Cell c1 = sheet.addCell("c1");
		assertEquals("", c1.getLabel());

		c1.setLabel("");
		assertEquals("", c1.getLabel());
		c1.setLabel("abcde");
		assertEquals("abcde", c1.getLabel());
		c1.setLabel("ラベルには日本語なども使用可能!?/ <>_+-*/^\"");
		assertEquals("ラベルには日本語なども使用可能!?/ <>_+-*/^\"", c1.getLabel());

		Cell c2 = sheet.addCell("c2");
		assertEquals("", c2.getLabel());

		c2.setLabel("c2's new label");
		assertEquals("c2's new label", c2.getLabel());
		assertEquals("ラベルには日本語なども使用可能!?/ <>_+-*/^\"", c1.getLabel());
	}

	public void testCreateUndefinedReferenceCells() {
		Cell cell = sheet.addCell("formulaCell").setFormula("ref1+ref2");
		cell.createUndefinedReferenceCells();
		assertTrue(sheet.cellExists("ref1"));
		assertTrue(sheet.cellExists("ref2"));

		cell.setFormula("ref1+ref2+ref3");
		cell.createUndefinedReferenceCells();
		assertTrue(sheet.cellExists("ref1"));
		assertTrue(sheet.cellExists("ref2"));
		assertTrue(sheet.cellExists("ref3"));

		cell.setFormula("newSheet!refA+newSheet!refB+newSheet!refC");
		Sheet newSheet = book.addSheet("newSheet");
		cell.createUndefinedReferenceCells();
		assertTrue(newSheet.cellExists("refA"));
		assertTrue(newSheet.cellExists("refB"));
		assertTrue(newSheet.cellExists("refC"));

		cell.setFormula("nonExistenceSheet!cell1");
		try {
			cell.createUndefinedReferenceCells();
		} catch (ReferenceNotFoundException e) {
			// OK
			return;
		} catch (Exception e) {
			// NG
			fail();
		}
		// NG
		fail();
	}


}
