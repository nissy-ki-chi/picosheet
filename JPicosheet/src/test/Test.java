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

import com.nissy_ki_chi.jpicosheet.core.Book;
import com.nissy_ki_chi.jpicosheet.core.Sheet;

public class Test {



	Test() {

	}


	public static void main(String[] args) {
		Test test = new Test();
		test.hoge();

	}


	public void hoge() {

		try {


			Book book= new Book("myBook");
			Sheet sheet = book.addSheet("mySheet");

			sheet.addCell("first").setNumberValue("12340");
			sheet.addCell("second").setNumberValue("56780");
			sheet.addCell("answer").setFormula("=first+second");
			sheet.addCell("average").setFormula("=answer/2");

			System.out.println(sheet.getCell("answer").getValueString());
			System.out.println(sheet.getCell("average").getValueString());

			book.recalcDisable();
			sheet.addCell("calc it").setFormula("=1+2+3-4");
			sheet.addCell("calc2").setFormula("=131+123123+312-2");
			sheet.addCell("calc3").setFormula("=1+2*3+4");
			sheet.addCell("calc3").setFormula("=1");
			sheet.addCell("calc4").setFormula("=0.3+0.3+0.3+0.1");
			sheet.addCell("calc5").setFormula("=10/3*3");
			sheet.addCell("calc6").setFormula("=calc5");
			sheet.addCell("calc7").setFormula("=calc4 + mySheet!calc6");
			book.recalcEnable();
			System.out.println(sheet.getCell("calc it").getValueString());
			System.out.println(sheet.getCell("calc2").getValueString());
			System.out.println(sheet.getCell("calc3").getValueString());
			System.out.println(sheet.getCell("calc4").getValueString());
			System.out.println(sheet.getCell("calc5").getValueString());
			System.out.println(sheet.getCell("calc6").getValueString());
			System.out.println(sheet.getCell("calc7").getValueString());
			System.out.println(sheet.addCell("empty").getValueString());

		} catch (Exception e) {
			// TODO 自動生成された catch ブロック
			e.printStackTrace();
		}

	}

}
