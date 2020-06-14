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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;

import com.nissy_ki_chi.jpicosheet.core.Book;
import com.nissy_ki_chi.jpicosheet.core.PicosheetException;
import com.nissy_ki_chi.jpicosheet.util.TextLoader;

import junit.framework.TestCase;

public class TextLoaderTest extends TestCase {

	protected void setUp() throws Exception {
		super.setUp();
	}

	public void testLoad() {
		//文字列をロードできること
		TextLoader pl = new TextLoader();

		String loadString = "# comment" + "\n" +
							"beginBook name=hogeBook" + "\n" +
							"#comment" + "\n" +
							"BEGINSHEET name=hogeSheet" + "\n" +
							"BeginCell" + "\n" +
							" cell1=123.456" + "\n" +
							" cell2=2233444" + "\n" +
							" cell3==cell1 + cell2" + "\n" +
							" cell4=123321" + "\n" +
							" cell5=12" + "\n" +
							"endcell" + "\n" +
							"begintable name=myTable# rowsize=10 colsize=10" + "\n" +
							" R1C1=123.999" + "\n" +
							" R10C10=999.9999" + "\n" +
							"endtable" + "\n" +
							"endsheet" + "\n" +
							"endbook";

		Book book = null;
		try {
			book = pl.load(new BufferedReader(new StringReader(loadString)));
		} catch (IOException e) {
			e.printStackTrace();
			fail();
		} catch (PicosheetException e) {
			e.printStackTrace();
			fail();
		}

		assertEquals("123.456", book.getResolver().getCell("cell1").getValue().getNumber().toString());
		assertEquals("2233444", book.getResolver().getCell("cell2").getValue().getNumber().toString());
		assertEquals("2233567.456", book.getResolver().getCell("cell3").getValue().getNumber().toString());
		assertEquals("123.999", book.getResolver().getCell("myTable#R1C1").getValue().getNumber().toString());
		assertEquals("999.9999", book.getResolver().getCell("myTable#R10C10").getValue().getNumber().toString());
	}


	public void testLoad2() throws IOException, PicosheetException {

		// 既存のBookに対し、さらにデータをロードできること

		// 最初にBook作成
		TextLoader pl = new TextLoader();

		String loadString = "beginbook name=myBook\n" +
							"beginsheet name=mySheet\n" +
							"endsheet\nendbook\n";

		Book book = null;
		book = pl.load(new BufferedReader(new StringReader(loadString)));

		// 続けて作成したBookに対しデータをロード
		String nextLoad = "beginbook name=myBook\n"+
		"beginsheet name=mySheet\n" +
		"begincell\n" +
		"fuga=12345\n"+
		"endcell\n"+
		"endsheet\n" +
		"endbook\n";

		TextLoader pl2 = new TextLoader(book);
		pl2.load(new BufferedReader(new StringReader(nextLoad)));

		assertEquals("12345", book.getResolver().getCell("fuga").getValue().getNumber().toString());
	}

}
