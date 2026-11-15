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
import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.math.MathContext;
import java.math.RoundingMode;

import com.nissy_ki_chi.picosheet.core.Book;
import com.nissy_ki_chi.picosheet.core.ConcurrentBookWrapper;
import com.nissy_ki_chi.picosheet.core.Group;
import com.nissy_ki_chi.picosheet.core.Sheet;
import com.nissy_ki_chi.picosheet.util.SimpleReader;
import com.nissy_ki_chi.picosheet.util.SimpleWriter;

import junit.framework.TestCase;

public class SimpleWriterTest extends TestCase {

	protected void setUp() throws Exception {
		super.setUp();
	}


	public void testWrite() throws IOException {

		Book book = new Book("myBook");
		Sheet sheet = book.addSheet("sheet1");
		sheet.setMathContext(new MathContext(12, RoundingMode.CEILING));
		sheet.addCell("cell1").setStringValue("abcde");
		sheet.addCell("cell2").setNumberValue("123");
		sheet.addCell("cell3").setFormula("cell2*3.14");
		sheet.addCell("cell4").setStringValue("string\nwith\nreturn");

		sheet.getCell("cell1").setLabel("ラベルには日本語なども使用可能!?/ <>_+-*/^\"");

		Group group = sheet.addGroup("hogeGroup@");
		String[] cellNames = {"cell1","cell2","cell3"};
		group.addCells(cellNames);

		sheet.addTable("newTable1#", 1, 1);
		sheet.getCell("newTable1#R1C1").setNumberValue("123");

		sheet.addTable("newTable2#", 1, 2);
		sheet.getCell("newTable2#R1C2").setNumberValue("11111");

		sheet.addTable("newTable3#", 2, 1);
		sheet.getCell("newTable3#R2C1").setNumberValue("22222");

		sheet.addTable("newTable4#", 10, 3);
		sheet.getCell("newTable4#R10C3").setNumberValue("33333");


		ConcurrentBookWrapper cbr = new ConcurrentBookWrapper(book);

		BufferedWriter bw = null;
		try {
			bw = new BufferedWriter(new FileWriter("target/writerOut.txt"));
			SimpleWriter sw = new SimpleWriter(cbr);
			sw.write(bw);
		} finally {
			if (bw != null) {
				bw.close();
			}
		}

	}

	public void testRead() throws IOException {
		Book book = new Book("myBook");
		ConcurrentBookWrapper cbr = new ConcurrentBookWrapper(book);

		BufferedReader br = null;;

		try {
			br = new BufferedReader(new InputStreamReader(new FileInputStream("target/writerOut.txt")));
			SimpleReader sr = new SimpleReader(cbr);
			sr.Read(br);
			
			assertEquals(new MathContext(12,RoundingMode.CEILING), cbr.getMathContext("sheet1"));
			
			assertEquals("abcde", cbr.getCellValue("sheet1!cell1").getString());
			assertEquals("ラベルには日本語なども使用可能!?/ <>_+-*/^\"", cbr.getCellLabel("sheet1!cell1"));
			assertEquals("123", cbr.getCellValue("sheet1!cell2").getString());
			assertEquals("=cell2*3.14", cbr.getCellFormula("sheet1!cell3"));
			assertEquals("string\nwith\nreturn", cbr.getCellValue("sheet1!cell4").getString());
			
			assertEquals("123", cbr.getCellValue("sheet1!newTable1#R1C1").getString());
			assertEquals("11111", cbr.getCellValue("sheet1!newTable2#R1C2").getString());
			assertEquals("22222", cbr.getCellValue("sheet1!newTable3#R2C1").getString());
			assertEquals("33333", cbr.getCellValue("sheet1!newTable4#R10C3").getString());
			
			
		} finally {
			if (br != null) {
				br.close();
			}
		}
	}
}
