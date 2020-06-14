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
import java.math.MathContext;
import java.math.RoundingMode;
import java.text.SimpleDateFormat;

import com.nissy_ki_chi.jpicosheet.core.Book;
import com.nissy_ki_chi.jpicosheet.core.Cell;
import com.nissy_ki_chi.jpicosheet.core.Element;
import com.nissy_ki_chi.jpicosheet.core.FormulaException;
import com.nissy_ki_chi.jpicosheet.core.ReferenceNotFoundException;
import com.nissy_ki_chi.jpicosheet.core.Sheet;
import com.nissy_ki_chi.jpicosheet.core.Cell.CellStatus;
import com.nissy_ki_chi.jpicosheet.core.Element.ElementType;
import com.nissy_ki_chi.jpicosheet.core.Element.ErrorType;

import junit.framework.TestCase;

public class CalculationTest extends TestCase {


	Book book;
	Sheet sheet;
	Cell cell;

	public CalculationTest(String name) {
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

	public void testBadFormula() throws FormulaException {

		// 正しい式…値のタイプはErrorではない
		cell.setFormula("1");
		assertNotSame(cell.getValue(), ElementType.ERROR);
		cell.setFormula("1.234");
		assertNotSame(cell.getValue(), ElementType.ERROR);
		cell.setFormula(".00000001");
		assertNotSame(cell.getValue(), ElementType.ERROR);
		cell.setFormula(" 1   +   2    ");
		assertNotSame(cell.getValue(), ElementType.ERROR);
		cell.setFormula("1*+2");
		assertNotSame(cell.getValue(), ElementType.ERROR);
		cell.setFormula("1/-2");
		assertNotSame(cell.getValue(), ElementType.ERROR);
		cell.setFormula("1+(2*3)+4");
		assertNotSame(cell.getValue(), ElementType.ERROR);
		cell.setFormula("1+(2*(3+4))*5");
		assertNotSame(cell.getValue(), ElementType.ERROR);

		// 不正な式…値のタイプはError、エラーのタイプはINVALID_FORMULA
		cell.setFormula("1 2");
		assertEquals(cell.getValue().getType(), ElementType.ERROR);
		assertEquals(cell.getValue().getErrorType(), ErrorType.INVALID_FORMULA);
		cell.setFormula("1.1 2.2");
		assertEquals(cell.getValue().getType(), ElementType.ERROR);
		assertEquals(cell.getValue().getErrorType(), ErrorType.INVALID_FORMULA);
		cell.setFormula(".1 .2");
		assertEquals(cell.getValue().getType(), ElementType.ERROR);
		assertEquals(cell.getValue().getErrorType(), ErrorType.INVALID_FORMULA);

		cell.setFormula("1+*2");
		assertEquals(cell.getValue().getType(), ElementType.ERROR);
		assertEquals(cell.getValue().getErrorType(), ErrorType.INVALID_FORMULA);
		cell.setFormula("1+/2");
		assertEquals(cell.getValue().getType(), ElementType.ERROR);
		assertEquals(cell.getValue().getErrorType(), ErrorType.INVALID_FORMULA);
		cell.setFormula("1**2");
		assertEquals(cell.getValue().getType(), ElementType.ERROR);
		assertEquals(cell.getValue().getErrorType(), ErrorType.INVALID_FORMULA);
		cell.setFormula("1+2,");
		assertEquals(cell.getValue().getType(), ElementType.ERROR);
		assertEquals(cell.getValue().getErrorType(), ErrorType.INVALID_FORMULA);
		cell.setFormula("(1+1");
		assertEquals(cell.getValue().getType(), ElementType.ERROR);
		assertEquals(cell.getValue().getErrorType(), ErrorType.INVALID_FORMULA);
		cell.setFormula("1+1)");
		assertEquals(cell.getValue().getType(), ElementType.ERROR);
		assertEquals(cell.getValue().getErrorType(), ErrorType.INVALID_FORMULA);
		cell.setFormula("1+1(");
		assertEquals(cell.getValue().getType(), ElementType.ERROR);
		assertEquals(cell.getValue().getErrorType(), ErrorType.INVALID_FORMULA);
		cell.setFormula(")1+1");
		assertEquals(cell.getValue().getType(), ElementType.ERROR);
		assertEquals(cell.getValue().getErrorType(), ErrorType.INVALID_FORMULA);

		cell.setFormula("1たす1");
		assertEquals(cell.getValue().getType(), ElementType.ERROR);
		assertEquals(cell.getValue().getErrorType(), ErrorType.INVALID_FORMULA);
		cell.setFormula("1plus1)");
		assertEquals(cell.getValue().getType(), ElementType.ERROR);
		assertEquals(cell.getValue().getErrorType(), ErrorType.INVALID_FORMULA);

		cell.setFormula("+");
		assertEquals(cell.getValue().getType(), ElementType.ERROR);
		assertEquals(cell.getValue().getErrorType(), ErrorType.INVALID_FORMULA);
		cell.setFormula("-");
		assertEquals(cell.getValue().getType(), ElementType.ERROR);
		assertEquals(cell.getValue().getErrorType(), ErrorType.INVALID_FORMULA);
		cell.setFormula("*");
		assertEquals(cell.getValue().getType(), ElementType.ERROR);
		assertEquals(cell.getValue().getErrorType(), ErrorType.INVALID_FORMULA);
		cell.setFormula("/");
		assertEquals(cell.getValue().getType(), ElementType.ERROR);
		assertEquals(cell.getValue().getErrorType(), ErrorType.INVALID_FORMULA);
		cell.setFormula("^");
		assertEquals(cell.getValue().getType(), ElementType.ERROR);
		assertEquals(cell.getValue().getErrorType(), ErrorType.INVALID_FORMULA);
		cell.setFormula("++");
		assertEquals(cell.getValue().getType(), ElementType.ERROR);
		assertEquals(cell.getValue().getErrorType(), ErrorType.INVALID_FORMULA);
		cell.setFormula("--");
		assertEquals(cell.getValue().getType(), ElementType.ERROR);
		assertEquals(cell.getValue().getErrorType(), ErrorType.INVALID_FORMULA);
		cell.setFormula("**");
		assertEquals(cell.getValue().getType(), ElementType.ERROR);
		assertEquals(cell.getValue().getErrorType(), ErrorType.INVALID_FORMULA);
		cell.setFormula("//");
		assertEquals(cell.getValue().getType(), ElementType.ERROR);
		assertEquals(cell.getValue().getErrorType(), ErrorType.INVALID_FORMULA);

		cell.setFormula("1+");
		assertEquals(cell.getValue().getType(), ElementType.ERROR);
		assertEquals(cell.getValue().getErrorType(), ErrorType.INVALID_FORMULA);
		cell.setFormula("1-");
		assertEquals(cell.getValue().getType(), ElementType.ERROR);
		assertEquals(cell.getValue().getErrorType(), ErrorType.INVALID_FORMULA);
		cell.setFormula("1*");
		assertEquals(cell.getValue().getType(), ElementType.ERROR);
		assertEquals(cell.getValue().getErrorType(), ErrorType.INVALID_FORMULA);
		cell.setFormula("1/");
		assertEquals(cell.getValue().getType(), ElementType.ERROR);
		assertEquals(cell.getValue().getErrorType(), ErrorType.INVALID_FORMULA);
		cell.setFormula("1+1+");
		assertEquals(cell.getValue().getType(), ElementType.ERROR);
		assertEquals(cell.getValue().getErrorType(), ErrorType.INVALID_FORMULA);
		cell.setFormula("1+1-");
		assertEquals(cell.getValue().getType(), ElementType.ERROR);
		assertEquals(cell.getValue().getErrorType(), ErrorType.INVALID_FORMULA);
		cell.setFormula("1+1*");
		assertEquals(cell.getValue().getType(), ElementType.ERROR);
		assertEquals(cell.getValue().getErrorType(), ErrorType.INVALID_FORMULA);
		cell.setFormula("1+1/");
		assertEquals(cell.getValue().getType(), ElementType.ERROR);
		assertEquals(cell.getValue().getErrorType(), ErrorType.INVALID_FORMULA);

	}

	public void testBadFormula2() {
		// 括弧の中に値や式が無い、空の状態の場合はエラーとなること
		cell.setFormula("()+3");
		assertEquals(ElementType.ERROR, cell.getValue().getType());
		assertEquals(ErrorType.INVALID_FORMULA, cell.getValue().getErrorType());
		cell.setFormula("()-3");
		assertEquals(ElementType.ERROR, cell.getValue().getType());
		assertEquals(ErrorType.INVALID_FORMULA, cell.getValue().getErrorType());
		cell.setFormula("()*3");
		assertEquals(ElementType.ERROR, cell.getValue().getType());
		assertEquals(ErrorType.INVALID_FORMULA, cell.getValue().getErrorType());
		cell.setFormula("()/3");
		assertEquals(ElementType.ERROR, cell.getValue().getType());
		assertEquals(ErrorType.INVALID_FORMULA, cell.getValue().getErrorType());
		cell.setFormula("()/3");
		assertEquals(ElementType.ERROR, cell.getValue().getType());
		assertEquals(ErrorType.INVALID_FORMULA, cell.getValue().getErrorType());
	}

	public void testBadFormula3() {
		// 式の末尾に不正な文字が付いている場合はエラーとなること
		cell.setFormula("1+1!");
		assertEquals(ElementType.ERROR, cell.getValue().getType());
		cell.setFormula("1+1$");
		assertEquals(ElementType.ERROR, cell.getValue().getType());
		cell.setFormula("1+1@");
		assertEquals(ElementType.ERROR, cell.getValue().getType());
		cell.setFormula("1+1..");
		assertEquals(ElementType.ERROR, cell.getValue().getType());
		cell.setFormula("1+1\\");
		assertEquals(ElementType.ERROR, cell.getValue().getType());
		cell.setFormula("1+1,");
		assertEquals(ElementType.ERROR, cell.getValue().getType());
		cell.setFormula("1+1'");
		assertEquals(ElementType.ERROR, cell.getValue().getType());

	}

	public void testBadFormula4() {
		// 式の中の数値のフォーマットが不正の場合エラーとなること(例外のスローではない)
		cell.setFormula("1..1");
		assertEquals(ElementType.ERROR, cell.getValue().getType());
		assertEquals(ErrorType.INVALID_FORMULA, cell.getValue().getErrorType());

	}

	public void testCalculation() throws Exception {

//		sheet.setMathContext(new MathContext(3, RoundingMode.HALF_UP));
		sheet.getCell("targetCell").setFormula("1");

		cell.setFormula("1+1");
		assertEquals(cell.getValue().getNumber(), new BigDecimal(1+1));
		cell.setFormula("1-1");
		assertEquals(cell.getValue().getNumber(), new BigDecimal(1-1));
		cell.setFormula("1*1");
		assertEquals(cell.getValue().getNumber(), new BigDecimal(1*1));
		cell.setFormula("1/1");
		assertEquals(cell.getValue().getNumber(), new BigDecimal(1/1));

		cell.setFormula("(2+3)*2");
		assertEquals(cell.getValue().getNumber(), new BigDecimal((2+3)*2));
		cell.setFormula("(1+(2+3))*2");
		assertEquals(cell.getValue().getNumber(), new BigDecimal((1.0+(2.0+3.0))*2.0));
		cell.setFormula("3-2-1");
		assertEquals(cell.getValue().getNumber(), new BigDecimal(3-2-1));
		cell.setFormula("4-5-3-4-5-1-6-4-5-1-2-3-4");
		assertEquals(cell.getValue().getNumber(), new BigDecimal(4-5-3-4-5-1-6-4-5-1-2-3-4));
		cell.setFormula("+2+4");
		assertEquals(cell.getValue().getNumber(), new BigDecimal(+2+4));
		cell.setFormula("+2+4-4+5+3-4-2-9+4+4+4");
		assertEquals(cell.getValue().getNumber(), new BigDecimal(+2+4-4+5+3-4-2-9+4+4+4));
		cell.setFormula("-1");
		assertEquals(cell.getValue().getNumber(), new BigDecimal(-1));
		cell.setFormula("-(1+1)");
		assertEquals(cell.getValue().getNumber(), new BigDecimal(-(1+1)));
		cell.setFormula("-4--1");
		assertEquals(cell.getValue().getNumber(), new BigDecimal(-4-(-1)));
		cell.setFormula("-4--1-2");
		assertEquals(cell.getValue().getNumber(), new BigDecimal(-4-(-1)-2));
		cell.setFormula("-4--1-2-3-4-2--4--5");
		assertEquals(cell.getValue().getNumber(), new BigDecimal(-4-(-1)-2-3-4-2-(-4)-(-5)));
		cell.setFormula("-4--1-2-3-4-2--4------4++++---++--4");
		assertEquals(cell.getValue().getNumber(), new BigDecimal(-4-(-1)-2-3-4-2-(-4)-(-(-(-(-(-4)))))+(+(+(+(-(-(-(+(+(-(-4))))))))))));
		cell.setFormula("-(-4)-(-(-(-(-(-4)))))+(+(+(+(-(-(-(+(+(-(-4))))))))))");
		assertEquals(cell.getValue().getNumber(), new BigDecimal(-(-4)-(-(-(-(-(-4)))))+(+(+(+(-(-(-(+(+(-(-4))))))))))));

		cell.setFormula("4+--4--1");
		assertEquals(cell.getValue().getNumber(), new BigDecimal(4+-(-4)-(-1)));
		cell.setFormula("+2+4-4+5+3-4-2-9+4+4++4+--4--1-2-3-4-2--4------4++++---++--4");
		assertEquals(cell.getValue().getNumber(), new BigDecimal(+2+4-4+5+3-4-2-9+4+4+(+4)+-(-4)-(-1)-2-3-4-2-(-4)-(-(-(-(-(-4)))))+(+(+(+(-(-(-(+(+(-(-4))))))))))));
		cell.setFormula("+2+4-4+5+3-4-2-9+4+4+(+4)+-(-4)-(-1)-2-3-4-2-(-4)-(-(-(-(-(-4)))))+(+(+(+(-(-(-(+(+(-(-4))))))))))");
		assertEquals(cell.getValue().getNumber(), new BigDecimal(+2+4-4+5+3-4-2-9+4+4+(+4)+-(-4)-(-1)-2-3-4-2-(-4)-(-(-(-(-(-4)))))+(+(+(+(-(-(-(+(+(-(-4))))))))))));
		cell.setFormula("(1+2*3)*(4/2+3)-(9-8-6-(-1))");
		assertEquals(cell.getValue().getNumber(), new BigDecimal((1+2*3)*(4/2+3)-(9-8-6-(-1))));

		cell.setFormula("-1");
		assertEquals(cell.getValue().getNumber(), new BigDecimal("-1"));
		cell.setFormula("--1");
		assertEquals(cell.getValue().getNumber(), new BigDecimal("1"));
		cell.setFormula("---1");
		assertEquals(cell.getValue().getNumber(), new BigDecimal("-1"));
		cell.setFormula("+1");
		assertEquals(cell.getValue().getNumber(), new BigDecimal("1"));
		cell.setFormula("-+1");
		assertEquals(cell.getValue().getNumber(), new BigDecimal("-1"));
		cell.setFormula("--++-+-+1");
		assertEquals(cell.getValue().getNumber(), new BigDecimal("1"));


	}

	public void testCalculation2() {
		cell.setFormula("1.1+2.2");
		assertEquals(cell.getValue().getNumber(), new BigDecimal("1.1").add(new BigDecimal("2.2")));
		cell.setFormula("-1.1+2.2");
		assertEquals(cell.getValue().getNumber(), new BigDecimal("-1.1").add(new BigDecimal("2.2")));
		cell.setFormula("-1.1+-2.2");
		assertEquals(cell.getValue().getNumber(), new BigDecimal("-1.1").add(new BigDecimal("-2.2")));
		cell.setFormula("-1.1+(-2.2)");
		assertEquals(cell.getValue().getNumber(), new BigDecimal("-1.1").add(new BigDecimal("-2.2")));
		cell.setFormula("-(1.1)+(-2.2)");
		assertEquals(cell.getValue().getNumber(), new BigDecimal("-1").multiply(new BigDecimal("1.1")).add(new BigDecimal("-2.2")));

		MathContext mc = sheet.getMathContext();

		cell.setFormula("1.1/6.6");
		assertEquals(cell.getValue().getNumber(), new BigDecimal("1.1").divide(new BigDecimal("6.6"), mc));

		cell.setFormula("1.1+(2.2+3.3)*(4.4-5.5)/6.6");
		assertEquals(cell.getValue().getNumber(), new BigDecimal("2.2").add(new BigDecimal("3.3")).multiply(new BigDecimal("4.4").subtract(new BigDecimal("5.5"))).divide(new BigDecimal("6.6"), mc).add(new BigDecimal("1.1")));

	}

	public void testCalculation3() {
		cell.setFormula("1/0");
		assertEquals(cell.getValue(), Element.newElement(ElementType.ERROR, ErrorType.DIVIDE_BY_ZERO));

	}

	public void testCalculation4() {
		cell.setBooleanValue(true);
		assertEquals(ElementType.BOOLEAN, cell.getValue().getType());
		assertEquals(new Boolean(true), cell.getValue().getBoolean());
		cell.setBooleanValue(false);
		assertEquals(ElementType.BOOLEAN, cell.getValue().getType());
		assertEquals(new Boolean(false), cell.getValue().getBoolean());

		cell.setFormula("true=true");
		assertEquals(ElementType.BOOLEAN, cell.getValue().getType());
		assertEquals(new Boolean(true), cell.getValue().getBoolean());
		cell.setFormula("true=true=true");
		assertEquals(ElementType.BOOLEAN, cell.getValue().getType());
		assertEquals(new Boolean(true), cell.getValue().getBoolean());
		cell.setFormula("true=false");
		assertEquals(ElementType.BOOLEAN, cell.getValue().getType());
		assertEquals(new Boolean(false), cell.getValue().getBoolean());
		cell.setFormula("true=false=true");
		assertEquals(ElementType.BOOLEAN, cell.getValue().getType());
		assertEquals(new Boolean(false), cell.getValue().getBoolean());

		cell.setFormula("1=1");
		assertEquals(ElementType.BOOLEAN, cell.getValue().getType());
		assertEquals(new Boolean(true), cell.getValue().getBoolean());
		cell.setFormula("SUM(1,2,3)=6");
		assertEquals(ElementType.BOOLEAN, cell.getValue().getType());
		assertEquals(new Boolean(true), cell.getValue().getBoolean());
		cell.setFormula("1=1=1");
		assertEquals(ElementType.BOOLEAN, cell.getValue().getType());
		assertEquals(new Boolean(true), cell.getValue().getBoolean());
		cell.setFormula("1=2=3");
		assertEquals(ElementType.BOOLEAN, cell.getValue().getType());
		assertEquals(new Boolean(false), cell.getValue().getBoolean());
		cell.setFormula("SUM(1,2,3)=6=(10-4)");
		assertEquals(ElementType.BOOLEAN, cell.getValue().getType());
		assertEquals(new Boolean(true), cell.getValue().getBoolean());
		cell.setFormula("1=1=true");
		assertEquals(ElementType.BOOLEAN, cell.getValue().getType());
		assertEquals(new Boolean(false), cell.getValue().getBoolean());
		cell.setFormula("1+1/2=1.5");
		assertEquals(ElementType.BOOLEAN, cell.getValue().getType());
		assertEquals(new Boolean(true), cell.getValue().getBoolean());
		cell.setFormula("-1+(-1)/2=-1.5");
		assertEquals(ElementType.BOOLEAN, cell.getValue().getType());
		assertEquals(new Boolean(true), cell.getValue().getBoolean());
		cell.setFormula("(1=1)=true");
		assertEquals(ElementType.BOOLEAN, cell.getValue().getType());
		assertEquals(new Boolean(true), cell.getValue().getBoolean());

		Cell cell1 = sheet.addCell("cell1").setNumberValue("1");
		Cell cell2 = sheet.addCell("cell2").setNumberValue("1");
		Cell compareCell = sheet.addCell("compare").setFormula("cell1=cell2");
		assertEquals(ElementType.BOOLEAN, compareCell.getValue().getType());
		assertEquals(new Boolean(true), compareCell.getValue().getBoolean());
		cell1.setNumberValue("2");
		assertEquals(ElementType.BOOLEAN, compareCell.getValue().getType());
		assertEquals(new Boolean(false), compareCell.getValue().getBoolean());
		cell1.setNumberValue("-1");
		assertEquals(ElementType.BOOLEAN, compareCell.getValue().getType());
		assertEquals(new Boolean(false), compareCell.getValue().getBoolean());
		cell1.setBooleanValue(true);
		assertEquals(ElementType.BOOLEAN, compareCell.getValue().getType());
		assertEquals(new Boolean(false), compareCell.getValue().getBoolean());
		cell1.setBooleanValue(false);
		assertEquals(ElementType.BOOLEAN, compareCell.getValue().getType());
		assertEquals(new Boolean(false), compareCell.getValue().getBoolean());
		cell1.setStringValue("abcde");
		assertEquals(ElementType.BOOLEAN, compareCell.getValue().getType());
		assertEquals(new Boolean(false), compareCell.getValue().getBoolean());
		cell1.setNumberValue("1");
		cell2.setNumberValue("2");
		compareCell.setFormula("SUM(cell1, cell2) = 3");
		assertEquals(ElementType.BOOLEAN, compareCell.getValue().getType());
		assertEquals(new Boolean(true), compareCell.getValue().getBoolean());
	}

	public void testCalculation5() {
		cell.setFormula("1<>2");
		assertEquals(ElementType.BOOLEAN, cell.getValue().getType());
		assertEquals(new Boolean(true), cell.getValue().getBoolean());
		cell.setFormula("1<2");
		assertEquals(ElementType.BOOLEAN, cell.getValue().getType());
		assertEquals(new Boolean(true), cell.getValue().getBoolean());
		cell.setFormula("1<=2");
		assertEquals(ElementType.BOOLEAN, cell.getValue().getType());
		assertEquals(new Boolean(true), cell.getValue().getBoolean());
		cell.setFormula("1>2");
		assertEquals(ElementType.BOOLEAN, cell.getValue().getType());
		assertEquals(new Boolean(false), cell.getValue().getBoolean());
		cell.setFormula("1>=2");
		assertEquals(ElementType.BOOLEAN, cell.getValue().getType());
		assertEquals(new Boolean(false), cell.getValue().getBoolean());

		cell.setFormula("-2<>-1");
		assertEquals(ElementType.BOOLEAN, cell.getValue().getType());
		assertEquals(new Boolean(true), cell.getValue().getBoolean());
		cell.setFormula("-2<-1");
		assertEquals(ElementType.BOOLEAN, cell.getValue().getType());
		assertEquals(new Boolean(true), cell.getValue().getBoolean());
		cell.setFormula("-2<=-1");
		assertEquals(ElementType.BOOLEAN, cell.getValue().getType());
		assertEquals(new Boolean(true), cell.getValue().getBoolean());
		cell.setFormula("-2>-1");
		assertEquals(ElementType.BOOLEAN, cell.getValue().getType());
		assertEquals(new Boolean(false), cell.getValue().getBoolean());
		cell.setFormula("-2>=-1");
		assertEquals(ElementType.BOOLEAN, cell.getValue().getType());
		assertEquals(new Boolean(false), cell.getValue().getBoolean());

		cell.setFormula("1+1<>2+2");
		assertEquals(ElementType.BOOLEAN, cell.getValue().getType());
		assertEquals(new Boolean(true), cell.getValue().getBoolean());
		cell.setFormula("1/1.234<2/2.345");
		assertEquals(ElementType.BOOLEAN, cell.getValue().getType());
		assertEquals(new Boolean(true), cell.getValue().getBoolean());
		cell.setFormula("1*2<=2*2");
		assertEquals(ElementType.BOOLEAN, cell.getValue().getType());
		assertEquals(new Boolean(true), cell.getValue().getBoolean());
		cell.setFormula("SUM(1,2,3)>SUM(2,3,4)");
		assertEquals(ElementType.BOOLEAN, cell.getValue().getType());
		assertEquals(new Boolean(false), cell.getValue().getBoolean());
		cell.setFormula("(1+2-3)>=(3+4+5)");
		assertEquals(ElementType.BOOLEAN, cell.getValue().getType());
		assertEquals(new Boolean(false), cell.getValue().getBoolean());

		cell.setFormula("true <> true");
		assertEquals(ElementType.BOOLEAN, cell.getValue().getType());
		assertEquals(new Boolean(false), cell.getValue().getBoolean());
		cell.setFormula("true < false");
		assertEquals(ElementType.BOOLEAN, cell.getValue().getType());
		assertEquals(new Boolean(false), cell.getValue().getBoolean());
		cell.setFormula("true <= true");
		assertEquals(ElementType.BOOLEAN, cell.getValue().getType());
		assertEquals(new Boolean(false), cell.getValue().getBoolean());
		cell.setFormula("true > true");
		assertEquals(ElementType.BOOLEAN, cell.getValue().getType());
		assertEquals(new Boolean(false), cell.getValue().getBoolean());
		cell.setFormula("true >= true");
		assertEquals(ElementType.BOOLEAN, cell.getValue().getType());
		assertEquals(new Boolean(false), cell.getValue().getBoolean());

	}

	public void testCalculation6() {
		cell.setFormula("true+true");
		assertEquals(ElementType.NUMBER, cell.getValue().getType());
		assertEquals(new BigDecimal("2"), cell.getValue().getNumber());
		cell.setFormula("true+false");
		assertEquals(ElementType.NUMBER, cell.getValue().getType());
		assertEquals(new BigDecimal("1"), cell.getValue().getNumber());
		cell.setFormula("true-true");
		assertEquals(ElementType.NUMBER, cell.getValue().getType());
		assertEquals(new BigDecimal("0"), cell.getValue().getNumber());
		cell.setFormula("SUM(true,true,true,false,true)");
		assertEquals(ElementType.NUMBER, cell.getValue().getType());
		assertEquals(new BigDecimal("4"), cell.getValue().getNumber());
	}

	public void testCalculation7() {
		cell.setFormula("\"abcd\"&\"efg\"");
		assertEquals("abcdefg", cell.getValue().getString());
		cell.setFormula("\"abcd\"  &  \"efg\"");
		assertEquals("abcdefg", cell.getValue().getString());

		cell.setFormula("&\"efg\"");
		assertEquals(ElementType.ERROR, cell.getValue().getType());
		assertEquals(ErrorType.INVALID_FORMULA, cell.getValue().getErrorType());
		cell.setFormula("\"efg\"&");
		assertEquals(ElementType.ERROR, cell.getValue().getType());
		assertEquals(ErrorType.INVALID_FORMULA, cell.getValue().getErrorType());
		cell.setFormula("\"abcd\"  && \"efg\"");
		assertEquals(ElementType.ERROR, cell.getValue().getType());
		assertEquals(ErrorType.INVALID_FORMULA, cell.getValue().getErrorType());

		cell.setFormula("\"abcd&\"&\"efg\"");
		assertEquals("abcd&efg", cell.getValue().getString());
		cell.setFormula("\"&abcd&\"&\"&efg&\"");
		assertEquals("&abcd&&efg&", cell.getValue().getString());

		cell.setFormula("123&456");
		assertEquals("123456", cell.getValue().getString());
		cell.setFormula("123.321&456.654");
		assertEquals("123.321456.654", cell.getValue().getString());
		cell.setFormula("-123&-456");
		assertEquals("-123-456", cell.getValue().getString());

		cell.setFormula("\"abc\" & 123");
		assertEquals("abc123", cell.getValue().getString());
		cell.setFormula("\"abc\" & -123.456");
		assertEquals("abc-123.456", cell.getValue().getString());
		cell.setFormula("\"abc\" & 123.456+100");
		assertEquals("abc223.456", cell.getValue().getString());
		cell.setFormula("33/3&\"abc\" & 123.456+100");
		assertEquals("11abc223.456", cell.getValue().getString());
		cell.setFormula("\"abc\" & 123.456+100-(0.456+3)");
		assertEquals("abc220.000", cell.getValue().getString());
	}

	public void testCalculation8() {
		cell.setFormula("");
		assertEquals(ElementType.EMPTY, cell.getValue().getType());
		// TODO: nullセット時の動作はnullpointerでいい？他も同様。
	}

	public void testCalculation9() {
		cell.setFormula("2^2");
		assertEquals(ElementType.NUMBER, cell.getValue().getType());
		assertEquals("4", cell.getValueString());
		cell.setFormula("4^(1/2)");
		assertEquals(ElementType.NUMBER, cell.getValue().getType());
		assertEquals("2", cell.getValueString());
	}

	public void testDateCalculation() {
		sheet.addCell("dateCell1").setValue("2014/05/25 12:00:00");
		sheet.addCell("dateCell2").setValue("1:23:45");

		cell.setFormula("dateCell1 + dateCell2");
		assertEquals(ElementType.DATE, cell.getValue().getType());
		assertEquals("2014/05/25 13:23:45", cell.getValueString());
		cell.setFormula("dateCell1 - dateCell2");
		assertEquals(ElementType.DATE, cell.getValue().getType());
		assertEquals("2014/05/25 10:36:15", cell.getValueString());

		sheet.getCell("dateCell1").setValue("2014/05/25 12:00:00");
		sheet.getCell("dateCell2").setFormula("2*3600*1000");
		cell.setFormula("dateCell1 + dateCell2");
		assertEquals(ElementType.DATE, cell.getValue().getType());
		assertEquals("2014/05/25 14:00:00", cell.getValueString());

		sheet.getCell("dateCell2").setFormula("3*365*24*3600*1000");
		assertEquals(ElementType.DATE, cell.getValue().getType());
		assertEquals("2017/05/24 12:00:00", cell.getValueString());

		sheet.addCell("dateCell3").setValue("00:15:00.123");
		sheet.getCell("dateCell2").setFormula("dateCell3 * 3");
		assertEquals(ElementType.DATE, cell.getValue().getType());
		assertEquals("2014/05/25 12:45:00.369", new SimpleDateFormat("yyyy/MM/dd HH:mm:ss.SSS").format(cell.getValue().getDate()));

		sheet.addCell("dateCell3").setValue("00:40:00.040");
		sheet.getCell("dateCell2").setFormula("dateCell3 / -2");
		assertEquals(ElementType.DATE, cell.getValue().getType());
		assertEquals("2014/05/25 11:39:59.980", new SimpleDateFormat("yyyy/MM/dd HH:mm:ss.SSS").format(cell.getValue().getDate()));
	}

	public void testFunctionCalculation() throws Exception {

//		sheet.setMathContext(new MathContext(3, RoundingMode.HALF_UP));
		sheet.getCell("targetCell").setFormula("1");

		cell.setFormula("SUM()");
		assertEquals(cell.getValue().getNumber(), new BigDecimal(0));
		cell.setFormula("SUM(1)");
		assertEquals(cell.getValue().getNumber(), new BigDecimal(1));
		cell.setFormula("SUM(1,2,3)");
		assertEquals(cell.getValue().getNumber(), new BigDecimal(6));
		cell.setFormula("SUM(1+1,2+2,3+3)");
		assertEquals(cell.getValue().getNumber(), new BigDecimal(12));
		cell.setFormula("SUM(1+1.1,2+2.2,3+3.3)");
		assertEquals(cell.getValue().getNumber(), new BigDecimal("1").add(new BigDecimal("1.1")).add(new BigDecimal("2")).add(new BigDecimal("2.2")).add(new BigDecimal("3")).add(new BigDecimal("3.3")));

		cell.setFormula("SUM(1+-1,-2+2,3+-3)");
		assertEquals(cell.getValue().getNumber(), new BigDecimal(0));
		cell.setFormula("SUM(1+(2+3),(1+(2+3)),(1+(2+(3+4))))");
		assertEquals(cell.getValue().getNumber(), new BigDecimal(22));

	}

	public void testFunctionCalculation2() {

		// エラー
		cell.setFormula("SUM(1,2,3)SUM(8)");
		assertEquals(CellStatus.ERROR, cell.getStatus());
		cell.setFormula("SUM(1,2,3)(SUM(8))");
		assertEquals(CellStatus.ERROR, cell.getStatus());

		// 左端に関数
		cell.setFormula("SUM(8)");
		assertEquals(new BigDecimal("8"), cell.getValue().getNumber());

		// 関数前の単項演算子
		cell.setFormula("+SUM(8)");
		assertEquals(new BigDecimal("+8"), cell.getValue().getNumber());
		cell.setFormula("-SUM(8)");
		assertEquals(new BigDecimal("-8"), cell.getValue().getNumber());

		// 関数前の四則演算子
		cell.setFormula("1+SUM(8)");
		assertEquals(new BigDecimal("9"), cell.getValue().getNumber());
		cell.setFormula("1-SUM(8)");
		assertEquals(new BigDecimal("-7"), cell.getValue().getNumber());
		cell.setFormula("1*SUM(8)");
		assertEquals(new BigDecimal("8"), cell.getValue().getNumber());
		cell.setFormula("1/-SUM(8)");
		assertEquals(new BigDecimal("-0.125"), cell.getValue().getNumber());

		// 関数前の論理演算子
		cell.setFormula("8=SUM(8)");
		assertTrue(cell.getValue().getBoolean());
		cell.setFormula("7<SUM(8)");
		assertTrue(cell.getValue().getBoolean());
		cell.setFormula("8<=SUM(8)");
		assertTrue(cell.getValue().getBoolean());
		cell.setFormula("9>SUM(8)");
		assertTrue(cell.getValue().getBoolean());
		cell.setFormula("8>=SUM(8)");
		assertTrue(cell.getValue().getBoolean());
		cell.setFormula("2<>SUM(8)");
		assertTrue(cell.getValue().getBoolean());

		// 関数前の文字列演算子
		cell.setFormula("\"abc\"&SUM(8)");
		assertEquals("abc8", cell.getValue().getString());

		// 関数前のカンマ
		cell.setFormula("SUM(8,SUM(10,SUM(20)))");
		assertEquals(new BigDecimal("38"), cell.getValue().getNumber());
	}

	public void testRecalcOrder() throws Exception {

		sheet.getCell("targetCell").setFormula("2");

		book.recalcDisable();
		cell.setFormula("1+1");
		book.recalcEnable();
		assertEquals(cell.getValue().getNumber(), new BigDecimal(1+1));

	}

	/**
	 * セル参照を行っている計算式セルの、参照先セルの値が変わった場合に計算式セルが再計算されること
	 * @throws Exception
	 */
	public void testRefer() throws Exception {

		sheet.getCell("targetCell").setFormula("3");

		Cell cell1 = sheet.addCell("Cell1").setNumberValue("1");
		Cell cell2 = sheet.addCell("Cell2").setNumberValue("2");
		Cell calcCell = sheet.addCell("calcCell").setFormula("Cell1+Cell2");
		assertEquals("3", calcCell.getValueString());
		cell2.setNumberValue("3");
		assertEquals("4", calcCell.getValueString());

		Cell calcCell2 = sheet.addCell("calcCell2").setFormula("calcCell + 3");
		assertEquals("7", calcCell2.getValueString());
		cell1.setNumberValue("2");
		assertEquals("8", calcCell2.getValueString());

		Cell calcCell3 = sheet.addCell("calcCell3").setFormula("calcCell2 + 4");
		assertEquals("12", calcCell3.getValueString());
		cell1.setNumberValue("3");
		assertEquals("13", calcCell3.getValueString());

		cell1.setNumberValue("4");
		assertEquals("14", calcCell3.getValueString());

		calcCell.setFormula("Cell1+Cell2+1");
		assertEquals("8", calcCell.getValueString());
		assertEquals("11", calcCell2.getValueString());
		assertEquals("15", calcCell3.getValueString());

		calcCell2.setFormula("calcCell + 4");
		assertEquals("12", calcCell2.getValueString());
		assertEquals("16", calcCell3.getValueString());
	}

	/**
	 * セル参照を行っている計算式セルの、参照先セルの値が変わった場合に計算式セルが再計算されること
	 * それを自動再計算を無効にした状態から再計算有効化した際にも正しく行えること
	 * @throws Exception
	 */
	public void testReferWithCalcDisable() throws Exception {

		sheet.getCell("targetCell").setFormula("4");

		book.recalcDisable();
		Cell cell1 = sheet.addCell("Cell1").setNumberValue("1");
		Cell cell2 = sheet.addCell("Cell2").setNumberValue("2");
		Cell calcCell = sheet.addCell("calcCell").setFormula("Cell1+Cell2");
//		assertEquals("3", calcCell.getString());
		cell2.setNumberValue("3");
//		assertEquals("4", calcCell.getString());

		Cell calcCell2 = sheet.addCell("calcCell2").setFormula("calcCell + 3");
//		assertEquals("7", calcCell2.getString());
		cell1.setNumberValue("2");
//		assertEquals("8", calcCell2.getString());

		Cell calcCell3 = sheet.addCell("calcCell3").setFormula("calcCell2 + 4");
//		assertEquals("12", calcCell3.getString());
		cell1.setNumberValue("3");
//		assertEquals("13", calcCell3.getString());

		cell1.setNumberValue("4");
//		assertEquals("14", calcCell3.getString());

		calcCell.setFormula("Cell1+Cell2+1");
//		assertEquals("8", calcCell.getString());
//		assertEquals("11", calcCell2.getString());
//		assertEquals("15", calcCell3.getString());

		calcCell2.setFormula("calcCell + 4");
		book.recalcEnable();
		assertEquals("8", calcCell.getValueString());
		assertEquals("12", calcCell2.getValueString());
		assertEquals("16", calcCell3.getValueString());

	}

	/**
	 * 参照を利用した再計算が正しく行えること
	 * @throws Exception
	 */
	public void testReferCalc() throws Exception {

		sheet.getCell("targetCell").setFormula("5");

		sheet.addCell("cell1").setNumberValue("1");
		sheet.addCell("cell2").setNumberValue("2");

		sheet.addCell("altCell").setFormula("cell2");

		sheet.addCell("cell3").setFormula("cell1+cell2");
		sheet.addCell("cell4").setFormula("cell3");
		sheet.addCell("cell5").setFormula("cell4+altCell");

		assertEquals("2", sheet.getCell("altCell").getValueString());
		assertEquals("5", sheet.getCell("cell5").getValueString());

		book.recalcDisable();
		sheet.addCell("cell2").setNumberValue("3");
		sheet.addCell("altCell").setFormula("cell2");

		sheet.addCell("cell3").setFormula("cell1+cell2");
		sheet.addCell("cell4").setFormula("cell3");
		sheet.addCell("cell5").setFormula("cell4+altCell");
		book.recalcEnable();
		assertEquals("3", sheet.getCell("altCell").getValueString());
		assertEquals("7", sheet.getCell("cell5").getValueString());

		book.recalcDisable();
		sheet.addCell("cell2").setNumberValue("4");
//		sheet.addCell("cell5").setFormula("cell4+altCell");
//		sheet.addCell("altCell").setFormula("cell2");

//		sheet.addCell("cell3").setFormula("cell1+cell2");
//		sheet.addCell("cell4").setFormula("cell3");
		book.recalcEnable();
		assertEquals("4", sheet.getCell("altCell").getValueString());
		assertEquals("9", sheet.getCell("cell5").getValueString());


	}

	/**
	 * 参照先が存在しない状態の計算式の計算結果がセルのエラーとなること
	 * @throws Exception
	 */
	public void testRefErrorToNormalResult() throws Exception {
		sheet.addCell("cell1").setFormula("cell2");
		assertEquals("ERROR:INVALID_REFERENCES", sheet.getCell("cell1").getValueString());
		sheet.addCell("cell2").setNumberValue("10");
		assertEquals("10", sheet.getCell("cell1").getValueString());
		sheet.addCell("cell2").setFormula("cell3");
		assertEquals("ERROR:INVALID_REFERENCES", sheet.getCell("cell1").getValueString());
		assertEquals("ERROR:INVALID_REFERENCES", sheet.getCell("cell2").getValueString());
		sheet.addCell("cell3").setNumberValue("10");
		assertEquals("10", sheet.getCell("cell1").getValueString());
		assertEquals("10", sheet.getCell("cell2").getValueString());
		assertEquals("10", sheet.getCell("cell3").getValueString());
	}

	/**
	 * 循環参照している場合に関係するセルすべてが循環参照エラーとなること
	 * また循環参照が解消された際にはエラーから回復すること
	 * @throws Exception
	 */
	public void testCirculerReferenceCalc() throws Exception {

		sheet.addCell("cell1").setFormula("cell2");
		sheet.addCell("cell2").setFormula("cell3");
		sheet.addCell("cell3").setFormula("cell1");
		assertEquals("ERROR:CIRCULAR_REFERENCE", sheet.getCell("cell1").getValueString());
		assertEquals("ERROR:CIRCULAR_REFERENCE", sheet.getCell("cell2").getValueString());
		assertEquals("ERROR:CIRCULAR_REFERENCE", sheet.getCell("cell3").getValueString());
		sheet.addCell("cell3").setNumberValue("11");
		assertEquals("11", sheet.getCell("cell1").getValueString());
		assertEquals("11", sheet.getCell("cell2").getValueString());
		assertEquals("11", sheet.getCell("cell3").getValueString());
		sheet.addCell("cell3").setFormula("cell1");
		assertEquals("ERROR:CIRCULAR_REFERENCE", sheet.getCell("cell1").getValueString());
		assertEquals("ERROR:CIRCULAR_REFERENCE", sheet.getCell("cell2").getValueString());
		assertEquals("ERROR:CIRCULAR_REFERENCE", sheet.getCell("cell3").getValueString());

		sheet.addCell("cell2_1").setFormula("cell2");
		assertEquals("ERROR:CIRCULAR_REFERENCE", sheet.getCell("cell2_1").getValueString());
		sheet.addCell("cell3").setNumberValue("11");
		assertEquals("11", sheet.getCell("cell1").getValueString());
		assertEquals("11", sheet.getCell("cell2").getValueString());
		assertEquals("11", sheet.getCell("cell2_1").getValueString());
		assertEquals("11", sheet.getCell("cell3").getValueString());
	}

	/**
	 * 不正な式のセルがInvalidFormulaエラーとなること。
	 * またこのセルを参照しているセルもエラーとなること。
	 */
	public void testInvalidFormula() {
		// 最初からInvalidFormulaのセルを参照したセルも同じくInvalidFormula
		sheet.addCell("cell1").setFormula("1+2#3");
		assertEquals("ERROR:INVALID_FORMULA", sheet.getCell("cell1").getValueString());
		sheet.addCell("cell2").setFormula("cell1");
		assertEquals("ERROR:INVALID_FORMULA", sheet.getCell("cell2").getValueString());

		// 正常だったセルがInvalidFormulaになったら、参照しているセルもその時点でInvalidFormula
		sheet.addCell("cell3").setFormula("1*2*3");
		assertEquals("6", sheet.getCell("cell3").getValueString());
		sheet.addCell("cell4").setFormula("cell3");
		assertEquals("6", sheet.getCell("cell3").getValueString());
		sheet.getCell("cell3").setFormula("1#2");
		assertEquals("ERROR:INVALID_FORMULA", sheet.getCell("cell3").getValueString());
		assertEquals("ERROR:INVALID_FORMULA", sheet.getCell("cell4").getValueString());

	}

	/**
	 * 異なるシートのセルを参照できること
	 * @throws IllegalStateException
	 * @throws ReferenceNotFoundException
	 */
	public void testAnotherSheetCalc() throws IllegalStateException, ReferenceNotFoundException {
		Sheet sheet2 = book.addSheet("sheet2");

		sheet.addCell("sheet_cell1").setNumberValue("10");
		sheet2.addCell("sheet2_cell1").setNumberValue("20");
		sheet.addCell("calcCell").setFormula("mySheet!sheet_cell1 + sheet2!sheet2_cell1");
		assertEquals(new BigDecimal("30"), sheet.getCell("calcCell").getValue().getNumber());
	}

	/**
	 * デフォルトシート以外のシートの式に含まれる参照が完全修飾名でない場合、
	 * (デフォルトシートでなく)そのシートのセルを参照すること。
	 */
	public void testNonDefaultSheetCalc() {
		Sheet anotherSheet = book.addSheet("anotherSheet");
		book.getResolver().setCurrentSheet(sheet);

		anotherSheet.addCell("another1").setNumberValue("123");
		anotherSheet.addCell("another2").setFormula("another1 * 10");
		assertEquals("1230", anotherSheet.getCell("another2").getValueString());
	}

	/**
	 * MathContextの設定通りに丸め処理が行われること
	 * また、途中でMathContextを変更した場合、シート全体が再計算されること
	 */
	public void testMathContext() {

		sheet.setMathContext(new MathContext(5, RoundingMode.CEILING));
		sheet.addCell("cell1").setFormula("10/3");
		sheet.addCell("cell2").setFormula("cell1+10");

		Sheet s2 = book.addSheet("newSheet");
		s2.setMathContext(new MathContext(10, RoundingMode.CEILING));
		s2.addCell("newcell1").setFormula("mySheet!cell1");
		s2.addCell("newcell2").setFormula("newcell1");
		s2.addCell("newcell3").setFormula("10/3");

		assertEquals("3.3334", sheet.getCell("cell1").getValue().getNumber().toString());
		assertEquals("13.334", sheet.getCell("cell2").getValue().getNumber().toString());
		assertEquals("3.3334", s2.getCell("newcell1").getValue().getNumber().toString());
		assertEquals("3.3334", s2.getCell("newcell2").getValue().getNumber().toString());
		assertEquals("3.333333334", s2.getCell("newcell3").getValue().getNumber().toString());

		sheet.setMathContext(new MathContext(7, RoundingMode.FLOOR));
		assertEquals("3.333333", sheet.getCell("cell1").getValue().getNumber().toString());
		assertEquals("13.33333", sheet.getCell("cell2").getValue().getNumber().toString());
		assertEquals("3.333333", s2.getCell("newcell1").getValue().getNumber().toString());
		assertEquals("3.333333", s2.getCell("newcell2").getValue().getNumber().toString());
		assertEquals("3.333333334", s2.getCell("newcell3").getValue().getNumber().toString());

	}
}
