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

import com.nissy_ki_chi.picosheet.core.Book;
import com.nissy_ki_chi.picosheet.core.Sheet;

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
