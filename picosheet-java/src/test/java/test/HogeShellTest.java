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

import java.math.BigDecimal;

import com.nissy_ki_chi.picosheet.core.Element;
import com.nissy_ki_chi.picosheet.core.ReferenceNotFoundException;
import com.nissy_ki_chi.picosheet.core.Cell.CellStatus;
import com.nissy_ki_chi.picosheet.core.Cell.CellType;
import com.nissy_ki_chi.picosheet.util.HogeShell;

import junit.framework.TestCase;

public class HogeShellTest extends TestCase {

	public void testHogeShell() {
		HogeShell hogeShell = new HogeShell();
	}

	public void testSetCellFromText() {
		HogeShell hogeShell = new HogeShell();
		String[] textArray = "n	hoge	1".split("\n");
		hogeShell.setCellFromText(textArray);
		textArray = "n	hoge	1\nn	fuga	10\n".split("\n");
		hogeShell.setCellFromText(textArray);
		textArray = "n	hoge	1\nn	fuga	10\nf	plusit	hoge+fuga".split("\n");
		hogeShell.setCellFromText(textArray);
	}

	public void testGetCellValue() {
		HogeShell hogeShell = new HogeShell();
		String[] textArray = "n	hoge	1".split("\n");
		hogeShell.setCellFromText(textArray);
		try {
			Element elem = hogeShell.getCellValue("hoge");
		} catch (ReferenceNotFoundException e) {
			fail();
		}
	}

	public void testGetCellStatus() {
		HogeShell hogeShell = new HogeShell();
		String[] textArray = "n	hoge	1".split("\n");
		hogeShell.setCellFromText(textArray);
		try {
			CellStatus cellStat = hogeShell.getCellStatus("hoge");
		} catch (ReferenceNotFoundException e) {
			fail();
		}
	}

	public void testGetCellType() {
		HogeShell hogeShell = new HogeShell();
		String[] textArray = "n	hoge	1".split("\n");
		hogeShell.setCellFromText(textArray);
		try {
			CellType cellType = hogeShell.getCellType("hoge");
		} catch (ReferenceNotFoundException e) {
			fail();
		}
	}


	public void testCalc() {
		HogeShell hogeShell = new HogeShell();

		String[] textArray = "n	hoge	1\nn	fuga	10\nf	plusit	hoge+fuga\nf	minusit	hoge-fuga\nf	multiit	hoge*fuga\nf	divit	hoge/fuga\n".split("\n");
		hogeShell.setCellFromText(textArray);

		try {
			Element elem = hogeShell.getCellValue("plusit");
			assertEquals(elem.getNumber(), new BigDecimal(1+10));
		} catch (ReferenceNotFoundException e) {
			fail();
		}


		String text ="n	A1	1\n" +
			"n	A1	1\n" +
			"n	A2	2\n" +
			"n	A3	3\n" +
			"n	A4	4\n" +
			"n	A5	5\n" +
			"f	A6	SUM(A1,A2,A3,A4,A5)\n";
		textArray = text.split("\\n");

		hogeShell.setCellFromText(textArray);

		try {
			Element elem = hogeShell.getCellValue("A6");
			assertEquals(elem.getNumber(), new BigDecimal(15));
		} catch (ReferenceNotFoundException e) {
			fail();
		}

	}

}
