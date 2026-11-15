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
package test;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;

import com.nissy_ki_chi.picosheet.core.Book;
import com.nissy_ki_chi.picosheet.core.PicosheetException;
import com.nissy_ki_chi.picosheet.util.TextLoader;

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
