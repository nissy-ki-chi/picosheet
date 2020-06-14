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
package test;

import java.math.BigDecimal;
import java.math.MathContext;
import java.util.Map;

import com.nissy_ki_chi.jpicosheet.core.Book;
import com.nissy_ki_chi.jpicosheet.core.Cell;
import com.nissy_ki_chi.jpicosheet.core.Element;
import com.nissy_ki_chi.jpicosheet.core.ReferenceNotFoundException;
import com.nissy_ki_chi.jpicosheet.core.Sheet;

import junit.framework.TestCase;

/**
 * @author yusuke nishikawa
 *
 */
/**
 * @author yusuke
 *
 */
public class SheetTest extends TestCase {

	private Book book;
	private Sheet sheet;
	private final String INITIAL_SHEET_NAME = "mySheet";

	/**
	 * @param name
	 * @throws Exception
	 */
	public SheetTest(String name) throws Exception {
		super(name);
	}

	/* (非 Javadoc)
	 * @see junit.framework.TestCase#setUp()
	 */
	protected void setUp() throws Exception {
		super.setUp();
		this.book = new Book("myBook");
		this.sheet = this.book.addSheet(INITIAL_SHEET_NAME);
	}

	/* (非 Javadoc)
	 * @see junit.framework.TestCase#tearDown()
	 */
	protected void tearDown() throws Exception {
		super.tearDown();
	}


	/**
	 * {@link com.nissy_ki_chi.jpicosheet.core.Sheet#Sheet()} のためのテスト・メソッド。
	 */
	public void testSheet() throws Exception {
		book.addSheet("newsheet");
		book.addSheet("NEWSHEET");
		book.addSheet("newSheet");
		book.addSheet("new_sheet");
		book.addSheet("a");
		book.addSheet("sheet1");
		book.addSheet("_new_sheet");
		book.addSheet("_______________new_sheet");
		book.addSheet("_new_sheet_with_underscore_");
		book.addSheet("longsheetname_aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa");

		try {
			book.addSheet("");
			fail("空文字のシート名でエラーにならない");
		} catch (Exception e) {
			// OK
		}
		try {
			book.addSheet("new cell");
			fail("中にスペースを含むシート名でエラーにならない");
		} catch (Exception e) {
			// OK
		}
		try {
			book.addSheet("new	cell");
			fail("中にタブを含むシート名でエラーにならない");
		} catch (Exception e) {
			// OK
		}
		try {
			book.addSheet(" newcell ");
			fail("前後にスペースを含むシート名でエラーにならない");
		} catch (Exception e) {
			// OK
		}
		try {
			book.addSheet("	newcell	");
			fail("前後にタブを含むシート名でエラーにならない");
		} catch (Exception e) {
			// OK
		}
		try {
			book.addSheet("日本語");
			fail("日本語のシート名でエラーにならない");
		} catch (Exception e) {
			// OK
		}
		try {
			book.addSheet("1122");
			fail("数字のみのシート名でエラーにならない");
		} catch (Exception e) {
			// OK
		}
		try {
			book.addSheet("1abc");
			fail("先頭文字が数字のシート名でエラーにならない");
		} catch (Exception e) {
			// OK
		}
		try {
			book.addSheet("=abc");
			fail("先頭文字が記号のシート名でエラーにならない");
		} catch (Exception e) {
			// OK
		}

	}
	/**
	 * {@link com.nissy_ki_chi.jpicosheet.core.Sheet#getName()} のためのテスト・メソッド。
	 */
	public void testGetName() {
		Sheet sheet;
		try {
			sheet = this.book.getSheet(INITIAL_SHEET_NAME);
			assertEquals(sheet.getName(), INITIAL_SHEET_NAME);
		} catch (Exception e) {
			fail("シート取得時にエラー");
		}
	}

	/**
	 * {@link com.nissy_ki_chi.jpicosheet.core.Sheet#getBook()} のためのテスト・メソッド。
	 */
	public void testGetBook() {
		try {
			Book tmpBook = this.book.getSheet(INITIAL_SHEET_NAME).getBook();
			assertSame(tmpBook, this.book);
		} catch (Exception e) {
			fail("getBook()でエラー");
		}
	}

	/**
	 * {@link com.nissy_ki_chi.jpicosheet.core.Sheet#addCell(java.lang.String)} のためのテスト・メソッド。
	 */
	public void testAddCell() {
		try {
			this.sheet.addCell("a");
			this.sheet.addCell("asldfkasldfkasdlfksalfkasflkasdjlaksdfjlaksflaskfjlaskfjlaskdfjlasdkfalsdkfalskfjalskfjlaskdfj");
			this.sheet.addCell("a1");
			this.sheet.addCell("cell_name_with_underscore");
			this.sheet.addCell("A");
		} catch (Exception e) {
			fail("addCellでエラー");
		}
	}


	/**
	 * 
	 */
	public void testCellExists() {
		String testCellName = "cellExistTestCell";
		this.sheet.addCell(testCellName);
		assertTrue(this.sheet.cellExists(testCellName));
		assertFalse(this.sheet.cellExists(testCellName + "xxxxx"));
	}
	
	/**
	 * 
	 */
	public void testCellExists2() {
		String testCellName = "cellExistTestCell";
		Cell mySheetCell = this.sheet.addCell(testCellName);
		Cell anotherCell = this.book.addSheet("anotherSheet").addCell("anotherSheetCell");
		
		assertTrue(this.sheet.cellExists(mySheetCell));
		assertFalse(this.sheet.cellExists(anotherCell));
}
	
	/**
	 * {@link com.nissy_ki_chi.jpicosheet.core.Sheet#getCell(java.lang.String)} のためのテスト・メソッド。
	 * @throws Exception
	 */
	public void testGetCell() throws Exception {
		this.sheet.addCell("a");
		this.sheet.addCell("asldfkasldfkasdlfksalfkasflkasdjlaksdfjlaksflaskfjlaskfjlaskdfjlasdkfalsdkfalskfjalskfjlaskdfj");
		this.sheet.addCell("a1");
		this.sheet.addCell("cell_name_with_underscore");
		this.sheet.addCell("A");

		try {
			this.sheet.getCell("a");
			this.sheet.getCell("asldfkasldfkasdlfksalfkasflkasdjlaksdfjlaksflaskfjlaskfjlaskdfjlasdkfalsdkfalskfjalskfjlaskdfj");
			this.sheet.getCell("a1");
			this.sheet.getCell("cell_name_with_underscore");
			this.sheet.getCell("A");
		} catch (Exception e) {
			fail("getCellでエラー");
		}

	}

	/**
	 * {@link com.nissy_ki_chi.jpicosheet.core.Sheet#renameCell(java.lang.String, java.lang.String)} のためのテスト・メソッド。
	 */
	public void testRenameCell() {
		try {
			this.sheet.addCell("renameCellTarget");
			this.sheet.renameCell("renameCellTarget", "renameCellTarget");
			this.sheet.getCell("renameCellTarget");
		} catch (Exception e) {
			fail("renameCellでエラー");
		}
		try {
			this.sheet.renameCell("renameCellTarget", "renamedCell");
			this.sheet.getCell("renamedCell");
		} catch (Exception e) {
			fail("renameCellでエラー");
		}
		try {
			this.sheet.getCell("renameCellTarget");
		} catch (ReferenceNotFoundException e) {
			// OK
			return;
		}
		fail("リネーム前のセルが残っている");
	}

	public void testRenameCell2() {
		Cell c1 = sheet.addCell("c1").setNumberValue("1");
		Cell c2 = sheet.addCell("c2").setFormula("c1 + 100");
		Cell c3 = sheet.addCell("c3").setFormula("c10+1000");

		// セル名が変更された場合、それを参照しているセルの結果も変わらなければならない
		assertEquals(Cell.CellStatus.CALCULATED, c2.getStatus());
		assertEquals(new BigDecimal("101"), c2.getValue().getNumber());
		assertEquals(Cell.CellStatus.ERROR, c3.getStatus());
		assertEquals(Element.ElementType.ERROR, c3.getValue().getType());
		assertEquals(Element.ErrorType.INVALID_REFERENCES, c3.getValue().getErrorType());

		sheet.renameCell("c1", "c10");
		assertEquals(Cell.CellStatus.ERROR, c2.getStatus());
		assertEquals(Element.ElementType.ERROR, c2.getValue().getType());
		assertEquals(Element.ErrorType.INVALID_REFERENCES, c2.getValue().getErrorType());
		assertEquals(Cell.CellStatus.CALCULATED, c3.getStatus());
		assertEquals(new BigDecimal("1001"), c3.getValue().getNumber());

		sheet.renameCell("c10", "c1");
		assertEquals(Cell.CellStatus.CALCULATED, c2.getStatus());
		assertEquals(new BigDecimal("101"), c2.getValue().getNumber());
		assertEquals(Cell.CellStatus.ERROR, c3.getStatus());
		assertEquals(Element.ElementType.ERROR, c3.getValue().getType());
		assertEquals(Element.ErrorType.INVALID_REFERENCES, c3.getValue().getErrorType());

	}

	/**
	 * {@link com.nissy_ki_chi.jpicosheet.core.Sheet#deleteCell(java.lang.String)} のためのテスト・メソッド。
	 */
	public void testDeleteCell() {
		try {
			this.sheet.addCell("deleteCellTarget");
			this.sheet.deleteCell("deleteCellTarget");
			try {
				this.sheet.getCell("deleteCellTarget");
			} catch (Exception e) {
				// 成功
				return;
			}
			fail("セルが削除されていない");
		} catch (Exception e) {
			fail("renameCellでエラー");
		}
	}

	public void testErrorCellsInfo() {

		// エラーセルがあることを知ることができること
		sheet.addCell("cell1").setFormula("1+*1");
		assertEquals(true, sheet.containsErrorCell());
		try {
			sheet.getCell("cell1").setFormula("1+1");
		} catch (ReferenceNotFoundException e) {
			fail("セル参照がおかしい");
		}
		assertEquals(false, sheet.containsErrorCell());

		// エラーセルの数を正しく得られること
		sheet.addCell("cell1").setFormula("1+*1");
		sheet.addCell("cell2").setFormula("1+/2");
		sheet.addCell("cell3").setFormula("1+*3");
		Map<String, Cell> errorCells = sheet.getErrorCells();
		assertEquals(3, errorCells.size());

		// getErrorCells()で取得したMapを変更しても影響がないこと
		errorCells.clear();
		assertEquals(3, sheet.getErrorCells().size());

	}

	public void testCompare() {
		Sheet s1 = book.addSheet("compareSheet1");
		Sheet s2 = book.addSheet("compareSheet2");

		assertEquals(0, s1.compareTo(s1));
		assertEquals(-1, s1.compareTo(s2));
		assertEquals(1, s2.compareTo(s1));

	}

	public void testEquals() {
		Sheet s1 = book.addSheet("compareSheet1");
		Sheet s2 = book.addSheet("compareSheet2");
		assertEquals(true, s1.equals(s1));
		assertEquals(true, s1.equals(s2));
		assertEquals(true, s2.equals(s1));

		s1.addCell("cell1").setNumberValue("123");
		s2.addCell("cell1").setNumberValue("123");
		assertEquals(true, s1.equals(s1));
		assertEquals(true, s1.equals(s2));
		assertEquals(true, s2.equals(s1));

		s1.addCell("cellabc").setNumberValue("234");
		s2.addCell("cellxyz").setNumberValue("987");
		assertEquals(true, s1.equals(s1));
		assertEquals(false, s1.equals(s2));
		assertEquals(false, s2.equals(s1));
	}
	
	public void testEquals2() {
		String sheetName = "sameSheetName";
		Sheet s1 = book.addSheet(sheetName);
		Sheet s2 = new Book("anotherBook").addSheet(sheetName);
		
		assertNotSame(s1, s2);
		assertTrue(s1.equals(s2));
	}
	
	public void testEquals3() {
		String sheetName = "sameSheetName";
		Sheet s1 = book.addSheet(sheetName);
		s1.addCell("cell1").setNumberValue("123");
		book.deleteSheet(sheetName);
		Sheet s2 = book.addSheet(sheetName);
		
		assertNotSame(s1, s2);
		assertFalse(s1.equals(s2));

		s2.addCell("cell1").setNumberValue("123");

		assertNotSame(s1, s2);
		assertTrue(s1.equals(s2));
		
		s1.addCell("cell2").setFormula("SUM(1,2,3)");
		s2.addCell("cell2").setFormula("SUM(1,2,3)");
		
		assertNotSame(s1, s2);
		assertTrue(s1.equals(s2));
		
	}
	
	public void testMathContext() {
		sheet.setMathContext(new MathContext(10));
		assertEquals(new MathContext(10), sheet.getMathContext());
	}
}
