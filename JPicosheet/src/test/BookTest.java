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

import java.util.Arrays;

import com.nissy_ki_chi.jpicosheet.core.Book;
import com.nissy_ki_chi.jpicosheet.core.BuiltinFunctionsConstants;
import com.nissy_ki_chi.jpicosheet.core.ReferenceNotFoundException;
import com.nissy_ki_chi.jpicosheet.core.Sheet;

import junit.framework.TestCase;

/**
 * @author yusuke nishikawa
 *
 */
public class BookTest extends TestCase {


	private Book book;
	private final String BOOK_NAME = "myBook";
	private final String INITIAL_SHEET_NAME = "mySheet";
	private final String NEW_SHEET_NAME = "newMySheet";

	/**
	 * @param name
	 */
	public BookTest(String name) {
		super(name);
	}

	/* (非 Javadoc)
	 * @see junit.framework.TestCase#setUp()
	 */
	protected void setUp() throws Exception {
		super.setUp();
		this.book = new Book(BOOK_NAME);
		this.book.addSheet(INITIAL_SHEET_NAME);
	}

	/* (非 Javadoc)
	 * @see junit.framework.TestCase#tearDown()
	 */
	protected void tearDown() throws Exception {
		super.tearDown();
	}

	/**
	 * {@link com.nissy_ki_chi.jpicosheet.core.Book#Book(java.lang.String)} のためのテスト・メソッド。
	 */
	public void testBook() {

		try {
			new Book("newBook");
		} catch (Exception e) {
			fail("Bookのnewでエラー");
		}
	}

	/**
	 * {@link com.nissy_ki_chi.jpicosheet.core.Book#getName()} のためのテスト・メソッド。
	 */
	public void testGetName() {
		assertEquals(BOOK_NAME, this.book.getName());
	}

	/**
	 * {@link com.nissy_ki_chi.jpicosheet.core.Book#setName(java.lang.String)} のためのテスト・メソッド。
	 */
	public void testSetName() {
		this.book.setName(NEW_SHEET_NAME);
		assertEquals(NEW_SHEET_NAME, this.book.getName());
	}

	/**
	 * {@link com.nissy_ki_chi.jpicosheet.core.Book#addSheet(java.lang.String)} のためのテスト・メソッド。
	 */
	public void testAddSheet() {

		Sheet newSheet;
		Sheet alreadyCreatedSheet;
		try {
			newSheet = this.book.addSheet(NEW_SHEET_NAME);
			// すでに存在するシート名でシートを作成した場合、既存のSheetオブジェクトが返される
			alreadyCreatedSheet = this.book.addSheet(NEW_SHEET_NAME);
			assertSame(newSheet, alreadyCreatedSheet);
		} catch (Exception e) {
			fail("シート作成時にエラー");
		}
	}

	public void testSheetExists() {
		
		String sheetName = "sheetExistTestSheet";
		this.book.addSheet(sheetName);
		
		assertTrue(this.book.sheetExists(sheetName));
		assertFalse(this.book.sheetExists(sheetName + "xxxxxx"));
		
	}

	public void testSheetExists2() {
		
		String sheetName = "sheetExistTestSheet";
		Sheet mySheet = this.book.addSheet(sheetName);
		Sheet anotherSheet = new Book("AnotherBook").addSheet("anotherSheet");
		
		assertTrue(this.book.sheetExists(mySheet));
		assertFalse(this.book.sheetExists(anotherSheet));
		
	}
	
	/**
	 * {@link com.nissy_ki_chi.jpicosheet.core.Book#renameSheet(java.lang.String, java.lang.String)} のためのテスト・メソッド。
	 * @throws Exception
	 */
	public void testRenameSheet() throws Exception {
		this.book.renameSheet(INITIAL_SHEET_NAME, NEW_SHEET_NAME);
		this.book.renameSheet(NEW_SHEET_NAME, INITIAL_SHEET_NAME);
	}

	/**
	 * {@link com.nissy_ki_chi.jpicosheet.core.Book#deleteSheet(java.lang.String)} のためのテスト・メソッド。
	 * @throws Exception
	 */
	public void testDeleteSheet() throws Exception {
		String sheetName = "sheetForDelete";
		this.book.addSheet(sheetName);
		try {
			this.book.deleteSheet(sheetName);
		} catch (Exception e) {
			fail("シート削除でエラー");
		}
		try {
			this.book.getSheet(sheetName);
		} catch (ReferenceNotFoundException e) {
			// OK
			return;
		}
		fail("シートが削除されていない");
	}

	/**
	 * {@link com.nissy_ki_chi.jpicosheet.core.Book#getSheet(java.lang.String)} のためのテスト・メソッド。
	 */
	public void testSheet() {
		Sheet sheet = null;
		try {
			this.book.addSheet(INITIAL_SHEET_NAME);
			sheet = this.book.getSheet(INITIAL_SHEET_NAME);
			assertNotNull(sheet);
		} catch (Exception e) {
			fail("シート取得でエラー");
		}

		try {
			// 存在しないシート名を指定した場合、例外が発生する
			this.book.getSheet("asdfadsfasdf");
			fail("存在しないシート名を指定したのに例外が発生しなかった");
		} catch (Exception e) {
			// OK
		}


	}

	/**
	 * {@link com.nissy_ki_chi.jpicosheet.core.Book#recalcEnable()} のためのテスト・メソッド。
	 */
	public void testRecalcEnable() {
		try {
			this.book.recalcEnable();
		} catch (Exception e) {
			fail("再計算の有効化でエラー");
		}

	}

	/**
	 * {@link com.nissy_ki_chi.jpicosheet.core.Book#recalcDisable()} のためのテスト・メソッド。
	 */
	public void testRecalcDisable() {
		try {
			this.book.recalcDisable();
		} catch (Exception e) {
			fail("再計算の無効化でエラー");
		}
	}

	public void testFunctionNames() {

		// BuiltinFunctionsConstants.BUILTIN_FUNCTIONSで定義した関数が利用可能であること
		String[] funcNameFromConst = new String[BuiltinFunctionsConstants.BUILTIN_FUNCTIONS.length];
		for (int i = 0; i < BuiltinFunctionsConstants.BUILTIN_FUNCTIONS.length; i++) {
			funcNameFromConst[i] = BuiltinFunctionsConstants.BUILTIN_FUNCTIONS[i][0];
		}
		String[] funcNameFromBook = this.book.getFunctionNames();

		Arrays.sort(funcNameFromConst);
		Arrays.sort(funcNameFromBook);
		assertTrue(Arrays.equals(funcNameFromConst, funcNameFromBook));
	}


}
