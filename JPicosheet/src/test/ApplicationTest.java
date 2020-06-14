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

import com.nissy_ki_chi.jpicosheet.core.Book;
import com.nissy_ki_chi.jpicosheet.core.Cell;
import com.nissy_ki_chi.jpicosheet.core.Sheet;

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
