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
/**
 *
 */
package test;

import com.nissy_ki_chi.picosheet.core.Book;
import com.nissy_ki_chi.picosheet.core.Cell;
import com.nissy_ki_chi.picosheet.core.Sheet;

import junit.framework.TestCase;

/**
 * @author yusuke nishikawa
 *
 */
public class ApplicationTest extends TestCase {


	private Book book;
	private Sheet sheet;
	private final String BOOK_NAME = "myBook";
	private final String INITIAL_SHEET_NAME = "mySheet";


	/**
	 * @param name
	 */
	public ApplicationTest(String name) {
		super(name);
	}

	/* (非 Javadoc)
	 * @see junit.framework.TestCase#setUp()
	 */
	protected void setUp() throws Exception {
		super.setUp();
		book = new Book(BOOK_NAME);
		sheet = book.addSheet(INITIAL_SHEET_NAME);
	}

	/* (非 Javadoc)
	 * @see junit.framework.TestCase#tearDown()
	 */
	protected void tearDown() throws Exception {
		super.tearDown();
	}


	public void testSetNumberValue() throws Exception {
		Cell hogeCell = sheet.addCell("hoge");
		hogeCell.setNumberValue("0");
		hogeCell.setNumberValue("-0");
		hogeCell.setNumberValue("-1");
		hogeCell.setNumberValue("-19238912831923871293817291982379891879");
		hogeCell.setNumberValue("0.0000000000000000000000000000000000000000000000000000000000000000000000000000000000001");
		hogeCell.setNumberValue("31983912839812739182379128739128379123879892384923847923847293847293847923879234872938492384793287923847932487");
		hogeCell.setNumberValue("1234567890123456");
		System.out.println(hogeCell.getValueString());
	}



}
