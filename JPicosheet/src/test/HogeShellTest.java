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

import com.nissy_ki_chi.jpicosheet.core.Element;
import com.nissy_ki_chi.jpicosheet.core.ReferenceNotFoundException;
import com.nissy_ki_chi.jpicosheet.core.Cell.CellStatus;
import com.nissy_ki_chi.jpicosheet.core.Cell.CellType;
import com.nissy_ki_chi.jpicosheet.util.HogeShell;

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
