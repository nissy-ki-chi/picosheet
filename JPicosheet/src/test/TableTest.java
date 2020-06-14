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

import java.math.BigDecimal;

import com.nissy_ki_chi.jpicosheet.core.Book;
import com.nissy_ki_chi.jpicosheet.core.Cell;
import com.nissy_ki_chi.jpicosheet.core.Range;
import com.nissy_ki_chi.jpicosheet.core.ReferenceNotFoundException;
import com.nissy_ki_chi.jpicosheet.core.Sheet;
import com.nissy_ki_chi.jpicosheet.core.Table;
import com.nissy_ki_chi.jpicosheet.core.Cell.CellStatus;
import com.nissy_ki_chi.jpicosheet.core.Element.ElementType;
import com.nissy_ki_chi.jpicosheet.core.Element.ErrorType;

import junit.framework.TestCase;

/**
 * @author yusuke nishikawa
 *
 */
public class TableTest extends TestCase {

	Book book;
	Sheet sheet;
	Table table;

	/* (非 Javadoc)
	 * @see junit.framework.TestCase#setUp()
	 */
	protected void setUp() throws Exception {
		super.setUp();
		book= new Book("myBook");
		sheet = book.addSheet("mySheet");
		table = sheet.addTable("myTable#", 15, 10);

	}

	protected void tearDown() throws Exception {
		super.tearDown();
	}

	public void testCreateTable() {
		// テーブルが作成できること
		sheet.addTable("newTable1#", 1, 1);
		sheet.addTable("newTable2#", 1, 2);
		sheet.addTable("newTable3#", 2, 1);
		sheet.addTable("newTable4#", 10, 10);
		sheet.addTable("newTable5#", 1000, 10);
	}

	public void testCreateTable2() {
		// テーブルのサイズが行、列とも1以上であること
		try {
			sheet.addTable("fugaTable#", 0, 0);
		} catch (IllegalArgumentException e) {
			// OK
			return;
		}
		fail();
	}
	public void testCreateTable3() {
		// テーブルのサイズが行、列とも1以上であること
		try {
			sheet.addTable("fugaTable#", 1, 0);
		} catch (IllegalArgumentException e) {
			// OK
			return;
		}
		fail();
	}
	public void testCreateTable4() {
		// テーブルのサイズが行、列とも1以上であること
		try {
			sheet.addTable("fugaTable#", 0, 1);
		} catch (IllegalArgumentException e) {
			// OK
			return;
		}
		fail();
	}
	public void testCreateTable5() {
		// テーブルのサイズが行、列とも1以上であること
		try {
			sheet.addTable("fugaTable#", -1, 1);
		} catch (IllegalArgumentException e) {
			// OK
			return;
		}
		fail();
	}
	public void testCreateTable6() {
		// テーブルのサイズが行、列とも1以上であること
		try {
			sheet.addTable("fugaTable#", 1, -1);
		} catch (IllegalArgumentException e) {
			// OK
			return;
		}
		fail();
	}

	public void testCreateSameNameTable() throws ReferenceNotFoundException {
		// 同じ名前のテーブルを作成しようとした場合、既存のテーブルが返されること
		Table alreadyTable = sheet.addTable("alreadyCreated#", 3, 4);
		sheet.getCell("alreadyCreated#R1C1").setNumberValue("123");
		assertEquals(new BigDecimal("123"), sheet.getCell("alreadyCreated#R1C1").getValue().getNumber());
		assertEquals(3, alreadyTable.getRowSize());
		assertEquals(4, alreadyTable.getColSize());

		Table renew_alreadyTable = sheet.addTable("alreadyCreated#", 10, 10);
		assertSame(alreadyTable, renew_alreadyTable);
		assertEquals(new BigDecimal("123"), sheet.getCell("alreadyCreated#R1C1").getValue().getNumber());
		assertEquals(3, renew_alreadyTable.getRowSize());
		assertEquals(4, renew_alreadyTable.getColSize());
	}

	public void testReCreateTable() {
		// 同じ名前のテーブルを作成しようとしたとき、すでに作成済みのテーブルが返されること
		Table table1 = sheet.addTable("newTable#", 4, 4);
		Table table2 = sheet.addTable("newTable#", 4, 4);
		Table table3 = sheet.addTable("newTable#", 10, 10);
		assertSame(table1, table2);
		assertSame(table1, table3);
		assertSame(table2, table3);
	}

	public void testDeleteTable(){
		// テーブルを削除できること
		sheet.addTable("newTable#", 2, 2);
		sheet.deleteTable("newTable#");
		try {
			sheet.getTable("newTable#");
		} catch (ReferenceNotFoundException e) {
			// OK
		}
	}

	public void testDeleteTable2(){
		// 存在しないテーブルを削除しようとした場合、何も起こらないこと
		sheet.addTable("newTable#", 2, 2);
		sheet.deleteTable("newTable#");
		try {
			sheet.getTable("newTable#");
		} catch (ReferenceNotFoundException e) {
			// NG
			fail();
		}
	}

	public void testAddRow() {
		// テーブルに行を追加できること
		table.addRow();
		table.addRow(1);
		table.addRow(100);
		try {
			table.addRow(-1);
		} catch (IllegalArgumentException e) {
			// OK
			return;
		}
		fail();
	}

	public void testAddColumn() {
		// テーブルに列を追加できること
		table.addColumn(1);
		table.getCell(1, 10);
		table.getCell(10, 10);
		table.getCell(14, 10);
		table.addColumn(1);
		table.addColumn(100);
		try {
			table.addColumn(-1);
		} catch (IllegalArgumentException e) {
			// OK
			return;
		}
	}

	public void testInsertRow() throws IllegalStateException, ReferenceNotFoundException {
		// テーブルに行を挿入できること
		table.getCell(1, 1).setNumberValue("10");
		table.insertRow(1);
		assertEquals(new BigDecimal("0"), table.getCell(1, 1).getValue().getNumber());
		assertEquals(new BigDecimal("10"), table.getCell(2, 1).getValue().getNumber());
		assertEquals(new BigDecimal("10"), sheet.getCell("myTable#R2C1").getValue().getNumber());
		table.insertRow(1);
		assertEquals(new BigDecimal("10"), table.getCell(3, 1).getValue().getNumber());
		table.insertRow(5);
		assertEquals(new BigDecimal("10"), table.getCell(3, 1).getValue().getNumber());
	}

	public void testInsertRow2() {
		// 不正な挿入位置指定でエラーになること
		try {
			table.insertRow(-1);
		} catch (IllegalArgumentException e) {
			// OK
			return;
		}
		// NG
		fail();
	}

	public void testInsertRow3() {
		// テーブルの行数より大きな挿入位置指定でエラーにならないこと
		try {
			int rowSize = table.getRowSize();
			// 末尾に1行追加
			table.insertRow(rowSize + 10);
		} catch (IllegalArgumentException e) {
			// NG
			fail();
		}
		// OK
		return;
	}


	public void testInsertRow4() throws IllegalStateException, ReferenceNotFoundException {
		// テーブルに行を挿入できること
		table.getCell(1, 1).setNumberValue("10");
		table.insertRow(1, 1);
		assertEquals(new BigDecimal("0"), table.getCell(1, 1).getValue().getNumber());
		assertEquals(new BigDecimal("10"), table.getCell(2, 1).getValue().getNumber());
		assertEquals(new BigDecimal("10"), sheet.getCell("myTable#R2C1").getValue().getNumber());
		table.insertRow(1, 10);
		assertEquals(new BigDecimal("0"), table.getCell(1, 1).getValue().getNumber());
		assertEquals(new BigDecimal("10"), table.getCell(12, 1).getValue().getNumber());
		assertEquals(new BigDecimal("10"), sheet.getCell("myTable#R12C1").getValue().getNumber());
		table.insertRow(5, 3);
		assertEquals(new BigDecimal("0"), table.getCell(1, 1).getValue().getNumber());
		assertEquals(new BigDecimal("10"), table.getCell(15, 1).getValue().getNumber());
		assertEquals(new BigDecimal("10"), sheet.getCell("myTable#R15C1").getValue().getNumber());
	}

	public void testInsertRow5() {
		// 不正な挿入位置指定でエラーになること
		try {
			table.insertRow(-1, 1);
		} catch (IllegalArgumentException e) {
			// OK
			return;
		}
		// NG
		fail();
	}

	public void testInsertRow7() {
		// 不正な挿入行数指定でエラーになること
		try {
			table.insertRow(1, -1);
		} catch (IllegalArgumentException e) {
			// OK
			return;
		}
		// NG
		fail();
	}

	public void testInsertColumn() throws IllegalStateException, ReferenceNotFoundException {
		// テーブルに列を挿入できること
		table.getCell(1, 1).setNumberValue("10");
		table.insertColumn(1);
		assertEquals(new BigDecimal("0"), table.getCell(1, 1).getValue().getNumber());
		assertEquals(new BigDecimal("10"), table.getCell(1, 2).getValue().getNumber());
		assertEquals(new BigDecimal("10"), sheet.getCell("myTable#R1C2").getValue().getNumber());
		table.insertColumn(1);
		assertEquals(new BigDecimal("10"), table.getCell(1, 3).getValue().getNumber());
		table.insertColumn(5);
		assertEquals(new BigDecimal("10"), table.getCell(1, 3).getValue().getNumber());
	}


	public void testInsertColumn2() {
		// 不正な挿入位置指定でエラーになること
		try {
			table.insertColumn(-1);
		} catch (IllegalArgumentException e) {
			// OK
			return;
		}
		// NG
		fail();
	}

	public void testInsertColumn3() {
		// テーブルの列数より大きな挿入位置指定でエラーになること
		try {
			table.insertColumn(10);
		} catch (IllegalArgumentException e) {
			// OK
			return;
		}
		// NG
		fail();
	}


	public void testInsertColumn4() throws IllegalStateException, ReferenceNotFoundException {
		// テーブルに列を挿入できること
		table.getCell(1, 1).setNumberValue("10");
		table.insertColumn(1, 1);
		assertEquals(new BigDecimal("0"), table.getCell(1, 1).getValue().getNumber());
		assertEquals(new BigDecimal("10"), table.getCell(1, 2).getValue().getNumber());
		assertEquals(new BigDecimal("10"), sheet.getCell("myTable#R1C2").getValue().getNumber());
		table.insertColumn(1, 10);
		assertEquals(new BigDecimal("0"), table.getCell(1, 1).getValue().getNumber());
		assertEquals(new BigDecimal("10"), table.getCell(1, 12).getValue().getNumber());
		assertEquals(new BigDecimal("10"), sheet.getCell("myTable#R1C12").getValue().getNumber());
		table.insertColumn(5, 4);
		assertEquals(new BigDecimal("0"), table.getCell(1, 1).getValue().getNumber());
		assertEquals(new BigDecimal("10"), table.getCell(1, 16).getValue().getNumber());
		assertEquals(new BigDecimal("10"), sheet.getCell("myTable#R1C16").getValue().getNumber());
	}

	public void testInsertColumn5() {
		// 不正な挿入位置指定でエラーになること
		try {
			table.insertColumn(-1, 1);
		} catch (IllegalArgumentException e) {
			// OK
			return;
		}
		// NG
		fail();
	}

	public void testInsertColumn6() {
		// テーブルの列数より大きな挿入位置指定でエラーになること
		try {
			table.insertColumn(16, 1);
		} catch (IllegalArgumentException e) {
			// OK
			return;
		}
		// NG
		fail();
	}

	public void testInsertColumn7() {
		// 不正な挿入列数指定でエラーになること
		try {
			table.insertColumn(1, -1);
		} catch (IllegalArgumentException e) {
			// OK
			return;
		}
		// NG
		fail();
	}


	public void testGetTableCell() throws ReferenceNotFoundException {
		// アドレス付きテーブル名でセルが取得できること
		sheet.getCell("myTable#R1C1");
		sheet.getCell("myTable#R1C2");
		sheet.getCell("myTable#R9C9");
	}

	public void testGetTableCell2() throws ReferenceNotFoundException {
		// アドレス付きテーブル名で作成した範囲外のセルが取得できないこと
		try {
			sheet.getCell("myTable#R1C11");
		} catch (ReferenceNotFoundException e) {
			// OK
			return;
		}
		fail();
	}

	public void testGetTableCell3() throws ReferenceNotFoundException {
		// アドレス付きテーブル名で作成した範囲外のセルが取得できないこと
		try {
			sheet.getCell("myTable#R1R1");
		} catch (ReferenceNotFoundException e) {
			// OK
			return;
		}
		fail();
	}

	public void testSetTableCell() throws ReferenceNotFoundException {
		// アドレスつきテーブル名指定で得たセルに値をセットできること
		sheet.getCell("myTable#R1C1").setNumberValue("10");
		sheet.getCell("myTable#R1C2").setNumberValue("20");
	}


	public void testTableCellCalc() throws ReferenceNotFoundException {
		// アドレスつきテーブル名指定でセルの計算ができること
		sheet.getCell("myTable#R1C1").setNumberValue("10");
		sheet.getCell("myTable#R1C2").setNumberValue("20");

		sheet.getCell("myTable#R1C3").setFormula("myTable#R1C1+myTable#R1C2");
		assertEquals(new BigDecimal("30"), sheet.getCell("myTable#R1C3").getValue().getNumber());

		sheet.getCell("myTable#R1C3").setFormula("1 + myTable#R1C1 + myTable#R1C2 + 2");
		assertEquals(new BigDecimal("33"), sheet.getCell("myTable#R1C3").getValue().getNumber());

	}

	public void testTableCellCalc2() throws ReferenceNotFoundException {
		// アドレスつきテーブル名指定でセルの計算ができること
		Cell r1c1 = sheet.getCell("myTable#R1C1").setNumberValue("10");
		Cell r1c2 = sheet.getCell("myTable#R1C2").setNumberValue("20");

		sheet.getCell("myTable#R1C3").setFormula("myTable#R1C1+myTable#R1C2");
		assertEquals(new BigDecimal("30"), sheet.getCell("myTable#R1C3").getValue().getNumber());

		r1c1.setNumberValue("11");
		assertEquals(new BigDecimal("31"), sheet.getCell("myTable#R1C3").getValue().getNumber());
		r1c2.setNumberValue("22");
		assertEquals(new BigDecimal("33"), sheet.getCell("myTable#R1C3").getValue().getNumber());
	}


	public void testFunctionCalc() throws ReferenceNotFoundException {
		// 関数にアドレス付きテーブル名指定が使えること
		Cell r1c1 = sheet.getCell("myTable#R1C1").setNumberValue("10");
		Cell r1c2 = sheet.getCell("myTable#R1C2").setNumberValue("20");

		sheet.getCell("myTable#R1C3").setFormula("SUM(myTable#R1C1,myTable#R1C2)");
		assertEquals(new BigDecimal("30"), sheet.getCell("myTable#R1C3").getValue().getNumber());

		Cell r2c5 = sheet.getCell("myTable#R2C5").setNumberValue("100");
		sheet.getCell("myTable#R1C3").setFormula("SUM(myTable#R1C1,myTable#R1C2, myTable#R2C5)");
		assertEquals(new BigDecimal("130"), sheet.getCell("myTable#R1C3").getValue().getNumber());

		r1c1.setNumberValue("11");
		r1c2.setNumberValue("21");
		r2c5.setNumberValue("101");
		assertEquals(new BigDecimal("133"), sheet.getCell("myTable#R1C3").getValue().getNumber());

	}

	public void testFunctionRangeCalc() throws ReferenceNotFoundException {

		for (int rowPos = 1; rowPos <= table.getRowSize(); rowPos++) {
			for (int colPos = 1; colPos <= table.getColSize(); colPos++) {
				sheet.getTable("myTable#").getCell(rowPos, colPos)
					.setNumberValue("1");
			}
		}

		Cell calcCell = sheet.addCell("calcCell");
		assertEquals(new BigDecimal("150"), calcCell.setFormula("SUM(myTable#)").getValue().getNumber());
		assertEquals(new BigDecimal("150"), calcCell.setFormula("SUM(myTable#R1C1:RxCx)").getValue().getNumber());

		assertEquals(new BigDecimal("10"), calcCell.setFormula("SUM(myTable#R1Cx)").getValue().getNumber());
		assertEquals(new BigDecimal("15"), calcCell.setFormula("SUM(myTable#RxC1)").getValue().getNumber());

		assertEquals(new BigDecimal("1"), calcCell.setFormula("SUM(myTable#R1C1)").getValue().getNumber());
		assertEquals(new BigDecimal("1"), calcCell.setFormula("SUM(myTable#R2C2)").getValue().getNumber());
		assertEquals(new BigDecimal("1"), calcCell.setFormula("SUM(myTable#R9C4)").getValue().getNumber());

		assertEquals(new BigDecimal("20"), calcCell.setFormula("SUM(myTable#R1C1:R2C10)").getValue().getNumber());
		assertEquals(new BigDecimal("9"), calcCell.setFormula("SUM(myTable#R6C6:R8C8)").getValue().getNumber());

	}

	public void testCreateRange() throws ReferenceNotFoundException {
		// テーブルからレンジを作成できること
		Table table = sheet.addTable("hogeTable#", 10,12);

		Range range;

		// すべて
//		range = table.getRange("RC");
//		assertEquals(10, range.getRowSize());
//		assertEquals(12, range.getColSize());
		range = table.getRange("R1C1:RxCx");
		assertEquals(10, range.getRowSize());
		assertEquals(12, range.getColSize());

		// 1行
		range = table.getRange("R3Cx");
		assertEquals(1, range.getRowSize());
		assertEquals(12, range.getColSize());
		// 1列
		range = table.getRange("RxC4");
		assertEquals(10, range.getRowSize());
		assertEquals(1, range.getColSize());

		// 複数行
		range = table.getRange("R3C1:R6Cx");
		assertEquals(4, range.getRowSize());
		assertEquals(12, range.getColSize());
		// 複数列
		range = table.getRange("R1C3:RxC6");
		assertEquals(10, range.getRowSize());
		assertEquals(4, range.getColSize());

		// 指定範囲
		range = table.getRange("R1C1:R1C1");
		assertEquals(1, range.getRowSize());
		assertEquals(1, range.getColSize());
		range = table.getRange("R1C1:R2C2");
		assertEquals(2, range.getRowSize());
		assertEquals(2, range.getColSize());
		range = table.getRange("R3C2:R5C4");
		assertEquals(3, range.getRowSize());
		assertEquals(3, range.getColSize());
		range = table.getRange("R4C3:R6Cx");
		assertEquals(3, range.getRowSize());
		assertEquals(10, range.getColSize());
		range = table.getRange("R4C3:RxC7");
		assertEquals(7, range.getRowSize());
		assertEquals(5, range.getColSize());
		range = table.getRange("R4C3:RxCx");
		assertEquals(7, range.getRowSize());
		assertEquals(10, range.getColSize());

	}

	public void testCreateRange2() {
		// 不正な範囲指定文字列が渡された場合エラーとなること
		Table table = sheet.addTable("hogeTable#", 10,12);

		try {
			table.getRange("asdf");
		} catch (IllegalArgumentException e) {
			// OK
			return;
		}
	}

	public void testCreateRange3() {
		// 不正な範囲指定文字列が渡された場合エラーとなること
		// 今のところ、レンジ指定は大文字のRとCのみ許されている
		Table table = sheet.addTable("hogeTable#", 10,12);

		try {
			table.getRange("r3c2:r5c4");
		} catch (IllegalArgumentException e) {
			// OK
			return;
		}
	}


	public void testBadRange() {
		// 不正なレンジ指定をした場合例外がスローされること
		Table table = sheet.addTable("myTable#", 10, 10);

		try {
			table.getRange("a1");
		} catch (IllegalArgumentException e) {
			// OK
			return;
		}
		fail("不正なレンジ指定なのに例外が発生していない");
	}

	public void testBadRange2() {
		// 不正なレンジ指定をした場合例外がスローされること
		Table table = sheet.addTable("myTable#", 10, 10);

		try {
			table.getRange("r1c1");
		} catch (IllegalArgumentException e) {
			// OK
			return;
		}
		fail("不正なレンジ指定なのに例外が発生していない");
	}

	public void testBadRang3() {
		// 不正なレンジ指定をした場合例外がスローされること
		Table table = sheet.addTable("myTable#", 10, 10);

		try {
			table.getRange("R5C5:R1C1");
		} catch (IllegalArgumentException e) {
			// OK
			return;
		}
		fail("不正なレンジ指定なのに例外が発生していない");
	}


	public void testCollectionCount() {
		// getCellsで正しいセル数が返されること
		assertEquals(150, table.getRange().getCells().size());

		assertEquals(1, table.getRange("R1C1:R1C1").getCells().size());

		assertEquals(10, table.getRange("R1Cx").getCells().size());
		assertEquals(15, table.getRange("RxC1").getCells().size());

		assertEquals(25, table.getRange("R2C2:R6C6").getCells().size());
	}


	public void testRangeCalc() throws ReferenceNotFoundException {

		// 範囲を使った計算式がある場合、範囲内のセルの値が更新されたらその式の結果も更新されること
		for (int rowPos = 1; rowPos <= table.getRowSize(); rowPos++) {
			for (int colPos = 1; colPos <= table.getColSize(); colPos++) {
				sheet.getTable("myTable#").getCell(rowPos, colPos)
					.setNumberValue("1");
			}
		}

		Cell sumTotal = sheet.addCell("sumTotal").setFormula("SUM(myTable#)");
		Cell sumRange1 = sheet.addCell("sumRange1").setFormula("SUM(myTable#R1C1:RxC1)");
		assertEquals(new BigDecimal("150"), sumTotal.getValue().getNumber());
		assertEquals(new BigDecimal("15"), sumRange1.getValue().getNumber());

		table.getCell(1, 1).setNumberValue("10");
		assertEquals(new BigDecimal("24"), sumRange1.getValue().getNumber());

		table.getCell(1, 4).setNumberValue("10");
		assertEquals(new BigDecimal("24"), sumRange1.getValue().getNumber());

		table.getCell(4, 1).setNumberValue("10");
		assertEquals(new BigDecimal("177"), sumTotal.getValue().getNumber());
		assertEquals(new BigDecimal("33"), sumRange1.getValue().getNumber());

		Cell sumRange2 = sheet.addCell("sumRange2").setFormula("SUM(myTable#R6C1:R10Cx)");
		assertEquals(new BigDecimal("50"), sumRange2.getValue().getNumber());

		table.getCell(6, 1).setNumberValue("10");
		assertEquals(new BigDecimal("186"), sumTotal.getValue().getNumber());
		assertEquals(new BigDecimal("42"), sumRange1.getValue().getNumber());
		assertEquals(new BigDecimal("59"), sumRange2.getValue().getNumber());

		table.getCell(7, 9).setNumberValue("10");
		assertEquals(new BigDecimal("195"), sumTotal.getValue().getNumber());
		assertEquals(new BigDecimal("42"), sumRange1.getValue().getNumber());
		assertEquals(new BigDecimal("68"), sumRange2.getValue().getNumber());

		Cell anotherCell = sheet.addCell("anotherCell").setNumberValue("100");
		table.getCell(7, 10).setFormula("anotherCell / 2");
		assertEquals(new BigDecimal("244"), sumTotal.getValue().getNumber());
		assertEquals(new BigDecimal("42"), sumRange1.getValue().getNumber());
		assertEquals(new BigDecimal("117"), sumRange2.getValue().getNumber());
		anotherCell.setNumberValue("200");
		assertEquals(new BigDecimal("294"), sumTotal.getValue().getNumber());
		assertEquals(new BigDecimal("42"), sumRange1.getValue().getNumber());
		assertEquals(new BigDecimal("167"), sumRange2.getValue().getNumber());

	}

	
	public void testCircularReferenceTable1() {
		
		Table t1 = sheet.addTable("circular1#", 2, 2);
		t1.getCell(1, 2).setNumberValue("100");
		t1.getCell(2, 1).setNumberValue("200");
		t1.getCell(2, 2).setNumberValue("300");
		Table t2 = sheet.addTable("circular2#", 2, 2);
		t2.getCell(1, 2).setNumberValue("1");
		t2.getCell(2, 1).setNumberValue("2");
		t2.getCell(2, 2).setNumberValue("3");
		Cell t1r1c1 = sheet.getCell("circular1#R1C1").setFormula("SUM(circular2#)");
		assertTrue(t1r1c1.getStatus() == CellStatus.CALCULATED);
		
		Cell t1r1c2 = sheet.getCell("circular2#R1C2").setFormula("SUM(circular1#)");
		assertTrue((t1r1c2.getStatus() == CellStatus.ERROR) &&
				(t1r1c2.getValueType() == ElementType.ERROR) &&
				(t1r1c2.getValue().getErrorType() == ErrorType.CIRCULAR_REFERENCE));
		
	}
	
	public void testCircularReferenceTable2() {
		
		Table t1 = sheet.addTable("circular1#", 2, 2);
		t1.getCell(1, 2).setNumberValue("100");
		t1.getCell(2, 1).setNumberValue("200");
		t1.getCell(2, 2).setNumberValue("300");
		Table t2 = sheet.addTable("circular2#", 2, 2);
		t2.getCell(1, 2).setNumberValue("1");
		t2.getCell(2, 1).setNumberValue("2");
		t2.getCell(2, 2).setNumberValue("3");
		Cell t1r1c1 = sheet.getCell("circular1#R1C1").setFormula("SUM(circular2#)");
		assertTrue(t1r1c1.getStatus() == CellStatus.CALCULATED);
		
		Cell t2r1c1 = sheet.getCell("circular2#R1C1").setFormula("SUM(circular1#)");
		assertTrue((t2r1c1.getStatus() == CellStatus.ERROR) &&
				(t2r1c1.getValueType() == ElementType.ERROR) &&
				(t2r1c1.getValue().getErrorType() == ErrorType.CIRCULAR_REFERENCE));
		
		assertTrue((t1r1c1.getStatus() == CellStatus.ERROR) &&
				(t1r1c1.getValueType() == ElementType.ERROR) &&
				(t1r1c1.getValue().getErrorType() == ErrorType.CIRCULAR_REFERENCE));
	}
}
