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

import com.nissy_ki_chi.jpicosheet.core.Book;
import com.nissy_ki_chi.jpicosheet.core.Cell;
import com.nissy_ki_chi.jpicosheet.core.CircularReferenceException;
import com.nissy_ki_chi.jpicosheet.core.Group;
import com.nissy_ki_chi.jpicosheet.core.ReferenceNotFoundException;
import com.nissy_ki_chi.jpicosheet.core.Sheet;
import com.nissy_ki_chi.jpicosheet.core.Cell.CellStatus;
import com.nissy_ki_chi.jpicosheet.core.Element.ElementType;
import com.nissy_ki_chi.jpicosheet.core.Element.ErrorType;

import junit.framework.TestCase;

public class GroupTest extends TestCase {

	Book book;
	Sheet sheet;
	Cell cell;

	public GroupTest(String name) {
		super(name);
	}

	protected void setUp() throws Exception {
		super.setUp();
		book= new Book("myBook");
		sheet = book.addSheet("mySheet");
		cell = sheet.addCell("targetCell");
	}

	protected void tearDown() throws Exception {
		super.tearDown();
	}


	public void testAddGroup() {
		sheet.addGroup("hogeGroup@");
	}

	public void testAddGroupAndCells() {
		Cell c1 = sheet.addCell("cell1").setNumberValue("123");
		Cell c2 = sheet.addCell("cell2").setNumberValue("456");

		Group group = sheet.addGroup("hogeGroup@");
		group.addCell(c1);
		try {
			group.addCell("cell2");
		} catch (ReferenceNotFoundException e) {
			fail("セル名が見つからない");
		}

	}

	public void testGroupsInGroup() throws CircularReferenceException{
		Cell c1 = sheet.addCell("cell1").setNumberValue("123");
		Cell c2 = sheet.addCell("cell2").setNumberValue("456");

		Group hogeGroup = sheet.addGroup("hogeGroup@");
		hogeGroup.addCell(c1);
		try {
			hogeGroup.addCell("cell2");
		} catch (ReferenceNotFoundException e) {
			fail("セル名が見つからない");
		}

		Group fugaGroup = sheet.addGroup("fugaGroup@");
		Cell c3 = sheet.addCell("cell3").setNumberValue("789");
		Cell c4 = sheet.addCell("cell4").setNumberValue("321");
		fugaGroup.addCell(c1);
		fugaGroup.addCell(c3);
		fugaGroup.addCell(c4);
		assertEquals(3, fugaGroup.getCells().size());

		fugaGroup.addCells(hogeGroup.getCells());
		assertEquals(4, fugaGroup.getCells().size());



	}

	public void testFunctionWithGroup() {
		Cell c1 = sheet.addCell("cell1").setNumberValue("123");
		Cell c2 = sheet.addCell("cell2").setNumberValue("456");

		Group hogeGroup = sheet.addGroup("hogeGroup@");
		hogeGroup.addCell(c1);
		try {
			hogeGroup.addCell("cell2");
		} catch (ReferenceNotFoundException e) {
			fail("セル名が見つからない");
		}

		cell.setFormula("SUM(1,2,3)");
		cell.setFormula("SUM(hogeGroup@)");
		assertEquals(cell.getValue().getNumber(), new BigDecimal(123+456));
		cell.setFormula("SUM(hogeGroup@, cell1, 1)");
		assertEquals(cell.getValue().getNumber(), new BigDecimal(123+456+123+1));

	}
	public void testFunctionWithGroup2() throws IllegalStateException, ReferenceNotFoundException {
		sheet.addCell("a").setNumberValue("1");
		sheet.addCell("b").setNumberValue("2");
		sheet.addCell("c").setNumberValue("3");
		sheet.addCell("x").setNumberValue("4");
		sheet.addCell("y").setNumberValue("5");
		sheet.addCell("z").setNumberValue("6");

		try {
			sheet.addGroup("one@").addCell("a").addCell("b").addCell("c");
			sheet.addGroup("two@").addCell("x").addCell("y").addCell("z");
			sheet.addGroup("three@").addCells(sheet.getGroup("one@").getCells()).addCells(sheet.getGroup("two@").getCells());
		} catch(ReferenceNotFoundException e) {
			fail(e.getMessage());
		}

		sheet.addCell("calc1").setFormula("SUM(three@)");
		assertEquals(sheet.getCell("calc1").getValue().getNumber(), new BigDecimal(1+2+3+4+5+6));
	}


	public void testCircularReferenceGroup1() {

		// グループの循環参照はチェックされエラーとならなければならない。
		Cell a = sheet.addCell("a").setNumberValue("100");
		Cell b = sheet.addCell("b").setNumberValue("200");
		Cell c = sheet.addCell("c").setNumberValue("300");
		sheet.addGroup("group1@").addCell(a).addCell(b).addCell(c);

		Cell sum1 = sheet.addCell("sum1").setFormula("SUM(group1@)");
		assertTrue(sum1.getStatus() == CellStatus.CALCULATED);

		// group1@に含まれるセルaに、group1@を使った式をセット
		a.setFormula("SUM(group1@)");
		assertTrue((a.getStatus() == CellStatus.ERROR) &&
				(a.getValueType() == ElementType.ERROR) &&
				(a.getValue().getErrorType() == ErrorType.CIRCULAR_REFERENCE) );

	}

	public void testCircularReferenceGroup2() {

		// グループが他のグループを使った計算を行うことはOKだが、お互いを参照すると循環参照
		Cell a = sheet.addCell("a").setNumberValue("100");
		Cell b = sheet.addCell("b").setNumberValue("200");
		Cell c = sheet.addCell("c").setNumberValue("300");
		Group group1 = sheet.addGroup("group1@").addCell(a).addCell(b).addCell(c);
		Cell x = sheet.addCell("x").setNumberValue("1");
		Cell y = sheet.addCell("y").setNumberValue("2");
		Cell z = sheet.addCell("z").setNumberValue("3");
		Group group2 = sheet.addGroup("group2@").addCell(x).addCell(y).addCell(z);

		Cell d = sheet.addCell("d").setFormula("SUM(group2@)");
		group1.addCell(d);
		assertTrue((d.getStatus() == CellStatus.CALCULATED));

		Cell w = sheet.addCell("w").setFormula("SUM(group1@)");
		group2.addCell(w);
		// d,wともに循環参照エラー
		assertTrue((w.getStatus() == CellStatus.ERROR) &&
				(w.getValueType() == ElementType.ERROR) &&
				(w.getValue().getErrorType() == ErrorType.CIRCULAR_REFERENCE) );
		assertTrue((d.getStatus() == CellStatus.ERROR) &&
				(d.getValueType() == ElementType.ERROR) &&
				(d.getValue().getErrorType() == ErrorType.CIRCULAR_REFERENCE) );
		
	}

	public void testValueChanged() throws IllegalStateException, ReferenceNotFoundException {
		// グループ中の値が変更された場合、そのグループを参照しているセルの内容は再計算されなければならない
		Group group1 = sheet.addGroup("group1@")
			.addCell(sheet.addCell("cell1").setNumberValue("10"))
			.addCell(sheet.addCell("cell2").setNumberValue("20"));
		Cell calcCell = sheet.addCell("calcCell").setFormula("1+SUM(group1@)+2");
		assertEquals(sheet.getCell("calcCell").getValue().getNumber(), new BigDecimal("33"));

		sheet.getCell("cell1").setNumberValue("11");
		assertEquals(sheet.getCell("calcCell").getValue().getNumber(), new BigDecimal("34"));

		sheet.addCell("calcCell2").setFormula("10+calcCell+100");
		assertEquals(sheet.getCell("calcCell2").getValue().getNumber(), new BigDecimal("144"));

		sheet.getCell("cell1").setNumberValue("12");
		assertEquals(sheet.getCell("calcCell").getValue().getNumber(), new BigDecimal("35"));
		assertEquals(sheet.getCell("calcCell2").getValue().getNumber(), new BigDecimal("145"));

	}


	public void testMemberAdd() throws IllegalStateException, ReferenceNotFoundException {
		// グループのメンバが追加された場合、そのグループを参照しているセルの内容は再計算されなければならない
		Group group1 = sheet.addGroup("group1@")
		.addCell(sheet.addCell("cell1").setNumberValue("10"))
		.addCell(sheet.addCell("cell2").setNumberValue("20"));
		Cell calcCell = sheet.addCell("calcCell").setFormula("1+SUM(group1@)+2");
		assertEquals(sheet.getCell("calcCell").getValue().getNumber(), new BigDecimal("33"));

		group1.addCell(sheet.addCell("cell3").setNumberValue("30"));
		assertEquals(new BigDecimal("63"), sheet.getCell("calcCell").getValue().getNumber());
	}

	public void testMemberRemove() throws IllegalStateException, ReferenceNotFoundException {
		// グループのメンバが削除された場合、そのグループを参照しているセルの内容は再計算されなければならない
		Group group1 = sheet.addGroup("group1@")
		.addCell(sheet.addCell("cell1").setNumberValue("10"))
		.addCell(sheet.addCell("cell2").setNumberValue("20"))
		.addCell(sheet.addCell("cell3").setNumberValue("30"));
		Cell calcCell = sheet.addCell("calcCell").setFormula("1+SUM(group1@)+2");
		assertEquals(sheet.getCell("calcCell").getValue().getNumber(), new BigDecimal("63"));

		group1.removeCell("cell1");
		assertEquals(new BigDecimal("53"), sheet.getCell("calcCell").getValue().getNumber());
		group1.removeCell(sheet.getCell("cell2"));
		assertEquals(new BigDecimal("33"), sheet.getCell("calcCell").getValue().getNumber());

	}

	public void testEmptyMember() throws IllegalStateException, ReferenceNotFoundException {
		// グループのメンバが空であった場合でも、そのグループを参照しているセルの内容は再計算されなければならない
		Group group1 = sheet.addGroup("group1@")
		.addCell(sheet.addCell("cell1").setNumberValue("10"))
		.addCell(sheet.addCell("cell2").setNumberValue("20"));
		Cell calcCell = sheet.addCell("calcCell").setFormula("1+SUM(group1@)+2");
		assertEquals(new BigDecimal("33"), sheet.getCell("calcCell").getValue().getNumber());

		group1.removeCell("cell1");
		assertEquals(new BigDecimal("23"), sheet.getCell("calcCell").getValue().getNumber());
		group1.removeCell(sheet.getCell("cell2"));
		assertEquals(new BigDecimal("3"), sheet.getCell("calcCell").getValue().getNumber());

	}

	public void testMemberFirstOne() throws IllegalStateException, ReferenceNotFoundException {
		// グループのメンバが空の状態から最初の1個が追加された場合、そのグループを参照しているセルの内容は再計算されなければならない
		Group group1 = sheet.addGroup("group1@");
		Cell calcCell = sheet.addCell("calcCell").setFormula("1+SUM(group1@)+2");
		assertEquals(new BigDecimal("3"), sheet.getCell("calcCell").getValue().getNumber());

		sheet.getGroup("group1@").addCell(sheet.addCell("cell1").setNumberValue("10"));
		assertEquals(new BigDecimal("13"), sheet.getCell("calcCell").getValue().getNumber());

	}

	public void testMemberLastOneToEmpty() throws IllegalStateException, ReferenceNotFoundException {
		// グループのメンバが最初の1個から空になった場合でも、そのグループを参照しているセルの内容は再計算されなければならない
		sheet.addGroup("group1@").addCell(sheet.addCell("cell1").setNumberValue("10"));
		assertEquals(new BigDecimal("0"), sheet.addCell("calcCell").getValue().getNumber());

		Cell calcCell = sheet.getCell("calcCell").setFormula("1+SUM(group1@)+2");
		assertEquals(new BigDecimal("13"), sheet.addCell("calcCell").getValue().getNumber());
		sheet.getGroup("group1@").removeCell("cell1");
		assertEquals(new BigDecimal("3"), sheet.getCell("calcCell").getValue().getNumber());
		// ちなみに、グループのメンバからはずされたセルは元のまま存在していなければならない
		assertEquals(new BigDecimal("10"), sheet.getCell("cell1").getValue().getNumber());
	}


	public void testNonexistenceGroup() {
		// グループが存在しない場合、そのグループを参照しているセルは参照エラーの状態になる
		Cell calcCell = sheet.addCell("calcCell").setFormula("1+SUM(group1@)+2");
		assertEquals(CellStatus.ERROR, calcCell.getStatus());
		assertEquals(ElementType.ERROR, calcCell.getValueType());
		assertEquals(ErrorType.INVALID_REFERENCES, calcCell.getValue().getErrorType());

	}

	public void testNonexistenceToExistence() throws IllegalStateException, ReferenceNotFoundException {
		// グループが追加された場合、そのグループを参照しているセルは参照エラーの状態から正常になる
		Cell calcCell = sheet.addCell("calcCell").setFormula("1+SUM(group1@)+2");
		assertEquals(CellStatus.ERROR, calcCell.getStatus());
		assertEquals(ElementType.ERROR, calcCell.getValueType());
		assertEquals(ErrorType.INVALID_REFERENCES, calcCell.getValue().getErrorType());

		sheet.addGroup("group1@");
		assertEquals(new BigDecimal("3"), sheet.getCell("calcCell").getValue().getNumber());
	}


	public void testExictenceToNonExistence() throws IllegalStateException, ReferenceNotFoundException {
		// グループが削除された場合、そのグループを参照しているセルは正常から参照エラーになる
		Group group1 = sheet.addGroup("group1@");
		Cell calcCell = sheet.addCell("calcCell").setFormula("1+SUM(group1@)+2");
		assertEquals(sheet.getCell("calcCell").getValue().getNumber(), new BigDecimal("3"));

		sheet.deleteGroup(group1);
		assertEquals(ElementType.ERROR, calcCell.getValueType());
		assertEquals(ErrorType.INVALID_REFERENCES, calcCell.getValue().getErrorType());
	}

	public void testAddAnothorCellToError() {
		// グループに、このグループが属するシートのセル以外のセルを含めようとした場合、エラーとならなければならない
		Cell cell = sheet.addCell("cell1");
		Cell anotherCell = book.addSheet("anotherSheet").addCell("anotherCell");
		Group group1 = sheet.addGroup("group1@");
		group1.addCell(cell);
		try {
			group1.addCell(anotherCell);
		} catch (Exception e) {
			//OK
			return;
		}
		fail("エラーになっていない");

	}


}
