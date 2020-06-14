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

import java.math.MathContext;
import java.math.RoundingMode;

import com.nissy_ki_chi.jpicosheet.core.Book;
import com.nissy_ki_chi.jpicosheet.core.ConcurrentBookWrapper;
import com.nissy_ki_chi.jpicosheet.core.ReferenceNotFoundException;
import com.nissy_ki_chi.jpicosheet.core.Element.ElementType;

import junit.framework.TestCase;

public class ConcurrentBookWrapperTest extends TestCase {

	protected void setUp() throws Exception {
		super.setUp();
	}

	public void testConcurrentBookWrapper() {
		ConcurrentBookWrapper cbw = new ConcurrentBookWrapper(new Book("concurrentTestBook"));
	}

	public void testSetBookName() {
		ConcurrentBookWrapper cbw = new ConcurrentBookWrapper(new Book("concurrentTestBook"));
		cbw.setBookName("newBookName");
		assertEquals("newBookName", cbw.getBookName());
	}

	public void testGetBookName() {
		ConcurrentBookWrapper cbw = new ConcurrentBookWrapper(new Book("concurrentTestBook"));
		assertEquals("concurrentTestBook", cbw.getBookName());
	}

	public void testAddSheet() {
		ConcurrentBookWrapper cbw = new ConcurrentBookWrapper(new Book("concurrentTestBook"));
		cbw.addSheet("newSheet1");
		cbw.addSheet("newSheet2");
		cbw.addSheet("newSheet3");
	}

	public void testDeleteSheet() {
		ConcurrentBookWrapper cbw = new ConcurrentBookWrapper(new Book("concurrentTestBook"));
		cbw.addSheet("newSheet1");
		cbw.addSheet("newSheet2");
		cbw.addSheet("newSheet3");
		cbw.deleteSheet("newSheet1");
		cbw.deleteSheet("newSheet3");
		cbw.deleteSheet("newSheet2");

		try {
			cbw.deleteSheet("newSheet1");
		} catch (ReferenceNotFoundException e) {
			// OK
			return;
		}
		fail();
	}

	public void testRenameSheet() {
		ConcurrentBookWrapper cbw = new ConcurrentBookWrapper(new Book("concurrentTestBook"));
		cbw.addSheet("newSheet1");
		cbw.addSheet("newSheet2");
		cbw.renameSheet("newSheet1", "superNewSheetName1");

		try {
			cbw.renameSheet("newSheet3", "anotherSheet3");
		} catch (ReferenceNotFoundException e) {
			// OK
			return;
		}
		fail();
	}

	public void testSetCellValue() {
		ConcurrentBookWrapper cbw = new ConcurrentBookWrapper(new Book("concurrentTestBook"));
		cbw.setCellValue("cell1", "abc");
		cbw.setCellValue("newSheet2!cell1", "xyz");

		assertEquals("xyz", cbw.getCellValue("newSheet2!cell1").getString());
		assertEquals("abc", cbw.getCellValue("cell1").getString());
		assertEquals("abc", cbw.getCellValue("Sheet1!cell1").getString());
	}

	public void testGetCellValue() {
		ConcurrentBookWrapper cbw = new ConcurrentBookWrapper(new Book("concurrentTestBook"));
		cbw.setCellValue("cell1", "abc");
		cbw.setCellValue("SheetA!cell1", "SheetA's abc");
		cbw.setCellValue("SheetB!cell1", "SheetB's abc");
		assertEquals("abc", cbw.getCellValue("cell1").getString());
		assertEquals("abc", cbw.getCellValue("Sheet1!cell1").getString());
		assertEquals("SheetA's abc", cbw.getCellValue("SheetA!cell1").getString());
		assertEquals("SheetB's abc", cbw.getCellValue("SheetB!cell1").getString());
		try {
			assertEquals("abc", cbw.getCellValue("SheetX!cell1").getString());
		} catch (ReferenceNotFoundException e) {
			// OK
			return;
		}
		fail();
	}

	public void testAddCell() {
		ConcurrentBookWrapper cbw = new ConcurrentBookWrapper(new Book("concurrentTestBook"));
		cbw.addCell("newBlankCell");
		cbw.addCell("Sheet2!newBlankCell");
		assertEquals(ElementType.EMPTY, cbw.getCellValue("newBlankCell").getType());
		assertEquals(ElementType.EMPTY, cbw.getCellValue("Sheet1!newBlankCell").getType());
		assertEquals(ElementType.EMPTY, cbw.getCellValue("Sheet2!newBlankCell").getType());
	}

	public void testDeleteCell() {
		ConcurrentBookWrapper cbw = new ConcurrentBookWrapper(new Book("concurrentTestBook"));
		cbw.addCell("newBlankCell");
		cbw.deleteCell("newBlankCell");
		try {
			cbw.getCellValue("newBlankCell");
		} catch (ReferenceNotFoundException e) {
			// OK
			return;
		}
		fail();
	}

	public void testRenameCell() {
		ConcurrentBookWrapper cbw = new ConcurrentBookWrapper(new Book("concurrentTestBook"));
		cbw.setCellValue("cell1", "abc");
		cbw.renameCell("cell1", "renamedCell1");
		assertEquals("abc", cbw.getCellValue("renamedCell1").getString());

		cbw.renameCell("Sheet1!renamedCell1", "re_renamedCell1");
		assertEquals("abc", cbw.getCellValue("re_renamedCell1").getString());

		try {
			cbw.renameCell("Sheet1!re_renamedCell1", "Sheet2!superCell1");
		} catch (Exception e) {
			// OK;
			return;
		}
		fail();
	}

	public void testSetMathContext() {
		ConcurrentBookWrapper cbw = new ConcurrentBookWrapper(new Book("concurrentTestBook"));
		cbw.setMathContext("Sheet1", new MathContext(10, RoundingMode.CEILING));
		cbw.addCell("Sheet1!cell1");
		cbw.setCellValue("Sheet1!cell1", "=10/3");
		assertEquals("3.333333334", cbw.getCellValue("Sheet1!cell1").getNumber().toString());
		cbw.setMathContext("Sheet1", new MathContext(11, RoundingMode.CEILING));
		assertEquals("3.3333333334", cbw.getCellValue("Sheet1!cell1").getNumber().toString());
	}
	
	public void testgetCellFormula() {
		ConcurrentBookWrapper cbw = new ConcurrentBookWrapper(new Book("concurrentTestBook"));
		cbw.addCell("Sheet1!cell1");
		cbw.setCellValue("Sheet1!cell1", "=10/3");
		assertEquals("=10/3", cbw.getCellFormula("Sheet1!cell1"));
	}
	
	public void testSetGetCellLabel() {
		ConcurrentBookWrapper cbw = new ConcurrentBookWrapper(new Book("concurrentTestBook"));
		cbw.addCell("Sheet1!cell1");
		cbw.setCellLabel("Sheet1!cell1", "new label");
		assertEquals("new label", cbw.getCellLabel("Sheet1!cell1"));
	}
	
	
}
