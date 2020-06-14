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
import com.nissy_ki_chi.jpicosheet.core.Element;
import com.nissy_ki_chi.jpicosheet.core.Sheet;
import com.nissy_ki_chi.jpicosheet.core.Element.ElementType;
import com.nissy_ki_chi.jpicosheet.core.Element.ErrorType;

import junit.framework.TestCase;

public class FunctionTest extends TestCase {

	Book book;
	Sheet sheet;
	Cell cell;

	protected void setUp() throws Exception {
		super.setUp();
		book= new Book("myBook");
		sheet = book.addSheet("mySheet");
		cell = sheet.addCell("targetCell");
	}

	public void testNonexistenceFunction() {
		cell.setFormula("nonexistence(1,2,3");
		assertEquals(Cell.CellStatus.ERROR, cell.getStatus());
		assertEquals(Element.ElementType.ERROR, cell.getValue().getType());
		assertEquals(Element.ErrorType.INVALID_FORMULA, cell.getValue().getErrorType());
	}

	public void testFunctionNameCharacterCase() {
		cell.setFormula("SUM(1, 2, 3)");
		assertEquals(new BigDecimal(1 + 2 + 3), cell.getValue().getNumber());
		cell.setFormula("SUM(1, 2, 3)");
		assertEquals(new BigDecimal(1 + 2 + 3), cell.getValue().getNumber());
		cell.setFormula("SUM(1, 2, 3)");
		assertEquals(new BigDecimal(1 + 2 + 3), cell.getValue().getNumber());

	}

	public void testFunctionInFunction() {
		cell.setFormula("SUM(SUM(1,1,1), 2, 3)");
		assertEquals(new BigDecimal("8"), cell.getValue().getNumber());
		cell.setFormula("SUM(2,SUM(1,1,1), 3)");
		assertEquals(new BigDecimal("8"), cell.getValue().getNumber());
		cell.setFormula("SUM(2, 3, SUM(1,1,1))");
		assertEquals(new BigDecimal("8"), cell.getValue().getNumber());

		cell.setFormula("SUM(SUM(), 2, 3)");
		assertEquals(new BigDecimal("5"), cell.getValue().getNumber());
		cell.setFormula("SUM(2, SUM(), 3)");
		assertEquals(new BigDecimal("5"), cell.getValue().getNumber());
		cell.setFormula("SUM(2, 3, SUM())");
		assertEquals(new BigDecimal("5"), cell.getValue().getNumber());

		cell.setFormula("SUM(SUM(SUM(1),SUM(1,2,3),SUM(1,2,3)), 2, 3)");
		assertEquals(new BigDecimal("18"), cell.getValue().getNumber());
		cell.setFormula("(SUM(SUM((SUM(1)),((SUM((1),2,3))),SUM(1,2,3)), 2, 3))");
		assertEquals(new BigDecimal("18"), cell.getValue().getNumber());
		cell.setFormula("( SUM(SUM(  ( SUM( 1	 )	 ) ,(	  ( 	SUM( ( 	1),	2 	 ,3))	),SUM( 1 , 2  , 3 ) 	) , 2 , 3) )    ");
		assertEquals(new BigDecimal("18"), cell.getValue().getNumber());

	}

	public void testBadArgument() {
		cell.setFormula("SUM(,)");
		assertEquals(ElementType.ERROR, cell.getValueType());
		assertEquals(ErrorType.INVALID_FORMULA, cell.getValue().getErrorType());
		cell.setFormula("SUM(,,,)");
		assertEquals(ElementType.ERROR, cell.getValueType());
		assertEquals(ErrorType.INVALID_FORMULA, cell.getValue().getErrorType());
		cell.setFormula("SUM(,");
		assertEquals(ElementType.ERROR, cell.getValueType());
		assertEquals(ErrorType.INVALID_FORMULA, cell.getValue().getErrorType());
		cell.setFormula("SUM(,()");
		assertEquals(ElementType.ERROR, cell.getValueType());
		assertEquals(ErrorType.INVALID_FORMULA, cell.getValue().getErrorType());
		cell.setFormula("SUM(,3)");
		assertEquals(ElementType.ERROR, cell.getValueType());
		assertEquals(ErrorType.INVALID_FORMULA, cell.getValue().getErrorType());
		cell.setFormula("SUM(+,+)");
		assertEquals(ElementType.ERROR, cell.getValueType());
		assertEquals(ErrorType.INVALID_FORMULA, cell.getValue().getErrorType());

	}

	public void testSum() {
		cell.setFormula("SUM(1, 2, 3)");
		assertEquals(new BigDecimal(1 + 2 + 3), cell.getValue().getNumber());
	}

	public void testSumByReference() {
		sheet.addCell("ref1").setNumberValue("100");
		sheet.addCell("ref2").setNumberValue("200");
		sheet.addCell("ref3").setFormula("ref2");
		sheet.addCell("formula").setFormula("SUM(ref1, ref3)");
		assertEquals(new BigDecimal(300), sheet.getCell("formula").getValue().getNumber());
	}


	public void testAverage() {
		cell.setFormula("AVERAGE(2, 4, 10, 4)");
		assertEquals(new BigDecimal(5), cell.getValue().getNumber());
	}

	public void testAverageByReference() {
		sheet.addCell("ref1").setNumberValue("100");
		sheet.addCell("ref2").setNumberValue("200");
		sheet.addCell("ref3").setFormula("ref2");
		sheet.addCell("formula").setFormula("AVERAGE(ref1, ref3)");
		assertEquals(new BigDecimal(150), sheet.getCell("formula").getValue().getNumber());
	}

	public void testABS() {
		cell.setFormula("ABS(10)");
		assertEquals(new BigDecimal("10"), cell.getValue().getNumber());
		cell.setFormula("ABS(-10)");
		assertEquals(new BigDecimal("10"), cell.getValue().getNumber());
		cell.setFormula("ABS(0.00001)");
		assertEquals(new BigDecimal("0.00001"), cell.getValue().getNumber());
		cell.setFormula("ABS(-0.00001)");
		assertEquals(new BigDecimal("0.00001"), cell.getValue().getNumber());
		cell.setFormula("ABS(10-100)");
		assertEquals(new BigDecimal("90"), cell.getValue().getNumber());
	}

	public void testMOD() {
		cell.setFormula("MOD(10, 3)");
		assertEquals(new BigDecimal("1"), cell.getValue().getNumber());
		cell.setFormula("MOD(10, -3)");
		assertEquals(new BigDecimal("-2"), cell.getValue().getNumber());
	}


	public void testINT() {
		cell.setFormula("INT(1.3)");
		assertEquals(new BigDecimal("1"), cell.getValue().getNumber());
		cell.setFormula("INT(-1.3)");
		assertEquals(new BigDecimal("-2"), cell.getValue().getNumber());
	}

	public void testIF() {
		cell.setFormula("IF(true, 10, 20)");
		assertEquals(new BigDecimal("10"), cell.getValue().getNumber());
		cell.setFormula("IF(false, 10, 20)");
		assertEquals(new BigDecimal("20"), cell.getValue().getNumber());

		cell.setFormula("IF(true = true, 10, 20)");
		assertEquals(new BigDecimal("10"), cell.getValue().getNumber());
		cell.setFormula("IF(true <> true, 10, 20)");
		assertEquals(new BigDecimal("20"), cell.getValue().getNumber());

		Cell c1 = sheet.addCell("cell1").setNumberValue("100");
		Cell c2 = sheet.addCell("cell2").setFormula("cell1 < 150");
		cell.setFormula("IF(cell2, 10, 20)");
		assertEquals(new BigDecimal("10"), cell.getValue().getNumber());
		c1.setNumberValue("200");
		cell.setFormula("IF(cell2, 10, 20)");
		assertEquals(new BigDecimal("20"), cell.getValue().getNumber());

		cell.setFormula("IF(cell2, \"small\", \"large\")");
		assertEquals("large", cell.getValueString());

	}

	public void testAND() {
		cell.setFormula("AND(true)");
		assertEquals(true, cell.getValue().getBoolean().booleanValue());
		cell.setFormula("AND(true,true)");
		assertEquals(true, cell.getValue().getBoolean().booleanValue());
		cell.setFormula("AND(true,true,true,true,true,true)");
		assertEquals(true, cell.getValue().getBoolean().booleanValue());
		cell.setFormula("AND(false)");
		assertEquals(false, cell.getValue().getBoolean().booleanValue());
		cell.setFormula("AND(false,false)");
		assertEquals(false, cell.getValue().getBoolean().booleanValue());
		cell.setFormula("AND(false,false,false,false,false)");
		assertEquals(false, cell.getValue().getBoolean().booleanValue());
		cell.setFormula("AND(true,false)");
		assertEquals(false, cell.getValue().getBoolean().booleanValue());
		cell.setFormula("AND(false,true)");
		assertEquals(false, cell.getValue().getBoolean().booleanValue());
		cell.setFormula("AND(true,false,true)");
		assertEquals(false, cell.getValue().getBoolean().booleanValue());

		cell.setFormula("AND(10=10,1<5,-4<>4)");
		assertEquals(true, cell.getValue().getBoolean().booleanValue());

		cell.setFormula("AND(123)");
		assertEquals(ElementType.ERROR, cell.getValue().getType());
		assertEquals(ErrorType.INVALID_PARAMETER, cell.getValue().getErrorType());
		cell.setFormula("AND(1)");
		assertEquals(ElementType.ERROR, cell.getValue().getType());
		assertEquals(ErrorType.INVALID_PARAMETER, cell.getValue().getErrorType());
	}

	public void testOR() {
		cell.setFormula("OR(true)");
		assertEquals(true, cell.getValue().getBoolean().booleanValue());
		cell.setFormula("OR(true,true)");
		assertEquals(true, cell.getValue().getBoolean().booleanValue());
		cell.setFormula("OR(true,true,true,true,true,true)");
		assertEquals(true, cell.getValue().getBoolean().booleanValue());
		cell.setFormula("OR(false)");
		assertEquals(false, cell.getValue().getBoolean().booleanValue());
		cell.setFormula("OR(false,false)");
		assertEquals(false, cell.getValue().getBoolean().booleanValue());
		cell.setFormula("OR(false,false,false,false,false)");
		assertEquals(false, cell.getValue().getBoolean().booleanValue());
		cell.setFormula("OR(true,false)");
		assertEquals(true, cell.getValue().getBoolean().booleanValue());
		cell.setFormula("OR(false,true)");
		assertEquals(true, cell.getValue().getBoolean().booleanValue());
		cell.setFormula("OR(true,false,true)");
		assertEquals(true, cell.getValue().getBoolean().booleanValue());

		cell.setFormula("OR(10=10,1>5,-4=4)");
		assertEquals(true, cell.getValue().getBoolean().booleanValue());

		cell.setFormula("OR(123)");
		assertEquals(ElementType.ERROR, cell.getValue().getType());
		assertEquals(ErrorType.INVALID_PARAMETER, cell.getValue().getErrorType());
		cell.setFormula("OR(1)");
		assertEquals(ElementType.ERROR, cell.getValue().getType());
		assertEquals(ErrorType.INVALID_PARAMETER, cell.getValue().getErrorType());
	}

	public void testNOT() {
		cell.setFormula("NOT(true)");
		assertEquals(false, cell.getValue().getBoolean().booleanValue());
		cell.setFormula("NOT(false)");
		assertEquals(true, cell.getValue().getBoolean().booleanValue());
	}

}
