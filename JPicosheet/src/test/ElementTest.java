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
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

import junit.framework.Assert;
import junit.framework.TestCase;

import com.nissy_ki_chi.jpicosheet.core.Element;
import com.nissy_ki_chi.jpicosheet.core.Element.ElementType;
import com.nissy_ki_chi.jpicosheet.core.Element.ErrorType;
import com.nissy_ki_chi.jpicosheet.core.Element.Operator;


public class ElementTest extends TestCase{

	public void testElementEmpty() {
		Element elem = Element.newElement(ElementType.EMPTY);
		Element elem2 = Element.newElement(ElementType.EMPTY);

		assertEquals("", elem.toString());
		assertEquals(true, elem.hasNumberValue());
		assertEquals(new BigDecimal(0), elem.getNumber());
		assertEquals("", elem.getString());
		assertEquals(new Date(0), elem.getDate());

		assertTrue(elem.equals(elem2));
		assertEquals(elem.hashCode(), elem2.hashCode());
		assertSame(elem, elem2);

		try {
			elem.getErrorType();
		} catch (IllegalStateException e) {
			// OK
			return;
		}
		fail();
	}

	public void testElementNumber() {
		Element elem = Element.newElement(ElementType.NUMBER, new BigDecimal(15.123456, new MathContext(4, RoundingMode.DOWN)));
		Element elem2 = Element.newElement(ElementType.NUMBER, new BigDecimal(15.123456, new MathContext(8, RoundingMode.DOWN)));

		assertEquals("NUMBER:15.12", elem.toString());
		assertEquals(true, elem.hasNumberValue());
		assertEquals(new BigDecimal("15.12"), elem.getNumber());
		assertEquals(false, elem.equals(elem2));
		assertEquals("15.12", elem.getString());
		assertEquals("15.123455", elem2.getString());

		boolean state = false;
		try {
			assertEquals(new Date(0), elem.getDate());
			state = false;
		} catch (IllegalStateException e) {
			// OK
			state = true;
		}
		assertTrue(state);

		for (int i = -10000; i < 10000; i += 74) {
			elem = Element.newElement(ElementType.NUMBER, new BigDecimal(i));
			elem2 = Element.newElement(ElementType.NUMBER, new BigDecimal(i));
			assertTrue(elem.equals(elem2));
			assertEquals(elem.hashCode(), elem2.hashCode());
			assertSame(elem, elem2);
		}

		for (double d = -10.001; d < 10.000; d += 0.074) {
			elem = Element.newElement(ElementType.NUMBER, new BigDecimal(d));
			elem2 = Element.newElement(ElementType.NUMBER, new BigDecimal(d));
			assertTrue(elem.equals(elem2));
			assertEquals(elem.hashCode(), elem2.hashCode());
			assertSame(elem, elem2);
		}

		try {
			elem.getErrorType();
		} catch (IllegalStateException e) {
			// OK
			return;
		}
		fail();
	}


	public void testElementString() {
		Element elem = Element.newElement(ElementType.STRING, "abcde");
		Element elem2 = Element.newElement(ElementType.STRING, "abcde");

		assertEquals("STRING:abcde", elem.toString());
		assertEquals(false, elem.hasNumberValue());
		assertEquals("abcde", elem.getString());
		assertEquals(true, elem.equals(elem2));

		boolean state = false;
		try {
			assertEquals(new Date(), elem.getDate());
			state = false;
		} catch (IllegalStateException e) {
			// OK
			state = true;
		}
		assertTrue(state);

		try {
			elem.getErrorType();
		} catch (IllegalStateException e) {
			// OK
			return;
		}
		fail();
	}

	public void testElementDate() throws ParseException {
		SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd HHmmss");
		Element elem;
		Element elem2;
		try {
			elem = Element.newElement(ElementType.DATE, sdf.parse("20130101 121001").getTime() + TimeZone.getDefault().getRawOffset());
			elem2 = Element.newElement(ElementType.DATE, sdf.parse("20130101 121001").getTime() + TimeZone.getDefault().getRawOffset());
		} catch (ParseException pe) {
			throw pe;
		}

		assertEquals("DATE:2013/01/01 12:10:01", elem.toString());
		assertEquals(false, elem.hasNumberValue());
		assertEquals("2013/01/01 12:10:01", elem.getString());
		assertEquals(true, elem.equals(elem2));

		boolean state = false;
		try {
			assertEquals(sdf.parse("20130101 121001"), elem.getDate());
			state = true;
		} catch (IllegalStateException e) {
			// NG
			state = false;
		}
		assertTrue(state);

		try {
			elem.getErrorType();
		} catch (IllegalStateException e) {
			// OK
			return;
		}
		fail();

	}

	public void testElementDate2() throws ParseException {
		SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd HHmmss");
		Element elem;
		Element elem2;
		try {
			elem = Element.newElement(ElementType.DATE, sdf.parse("20130101 121001").getTime() + TimeZone.getDefault().getRawOffset());
			elem2 = Element.newElement(ElementType.DATE, sdf.parse("20130101 121001").getTime() + TimeZone.getDefault().getRawOffset());
		} catch (ParseException pe) {
			throw pe;
		}

		assertEquals("DATE:2013/01/01 12:10:01", elem.toString());
		assertEquals(false, elem.hasNumberValue());
		assertEquals("2013/01/01 12:10:01", elem.getString());
		assertEquals(true, elem.equals(elem2));


	}

	public void testElementBoolean() {
		Element elem = Element.newElement(ElementType.BOOLEAN, true);
		Element elem2 = Element.newElement(ElementType.BOOLEAN, true);

		assertEquals("BOOLEAN:true", elem.toString());
		assertEquals(true, elem.hasNumberValue());
		assertEquals("true", elem.getString());
		assertEquals(true, elem.equals(elem2));

		boolean state = false;
		try {
			assertEquals(new Date(), elem.getDate());
			state = false;
		} catch (IllegalStateException e) {
			// OK
			state = true;
		}
		assertTrue(state);

		try {
			elem.getErrorType();
		} catch (IllegalStateException e) {
			// OK
			return;
		}
		fail();
	}

	public void testElementOperator() {
		Element elem = Element.newElement(ElementType.OPERATOR, Operator.PLUS);
		Element elem2 = Element.newElement(ElementType.OPERATOR, Operator.PLUS);

		assertEquals("OPERATOR:PLUS", elem.toString());
		assertEquals(false, elem.hasNumberValue());
		assertEquals("PLUS", elem.getString());
		assertEquals(true, elem.equals(elem2));

		boolean state = false;
		try {
			assertEquals(new Date(), elem.getDate());
			state = false;
		} catch (IllegalStateException e) {
			// OK
			state = true;
		}
		assertTrue(state);

		try {
			elem.getErrorType();
		} catch (IllegalStateException e) {
			// OK
			return;
		}
		fail();
	}

	public void testElementError() {
		Element elem = Element.newElement(ElementType.ERROR, ErrorType.INVALID_REFERENCES);
		Element elem2 = Element.newElement(ElementType.ERROR, ErrorType.INVALID_REFERENCES);

		assertEquals("ERROR:INVALID_REFERENCES", elem.toString());
		assertEquals(false, elem.hasNumberValue());
		assertEquals("INVALID_REFERENCES", elem.getString());
		assertEquals(true, elem.equals(elem2));

		boolean state = false;
		try {
			assertEquals(new Date(), elem.getDate());
			state = false;
		} catch (IllegalStateException e) {
			// OK
			state = true;
		}
		assertTrue(state);

		try {
			elem.getErrorType();
		} catch (IllegalStateException e) {
			// NG
			fail();
		}
	}


	public void testElementFunction() {
		Element elem = Element.newElement(ElementType.FUNCTION, "SUM");
		Element elem2 = Element.newElement(ElementType.FUNCTION, "SUM");

		assertEquals("FUNCTION:SUM", elem.toString());
		assertEquals(false, elem.hasNumberValue());
		assertEquals("SUM", elem.getString());
		assertEquals(true, elem.equals(elem2));

		boolean state = false;
		try {
			assertEquals(new Date(), elem.getDate());
			state = false;
		} catch (IllegalStateException e) {
			// OK
			state = true;
		}
		assertTrue(state);

		try {
			elem.getErrorType();
		} catch (IllegalStateException e) {
			// OK
			return;
		}
		fail();
	}

	public void testElementReference() {
		Element elem = Element.newElement(ElementType.REFERENCE, "refCellName");
		Element elem2 = Element.newElement(ElementType.REFERENCE, "refCellName");

		assertEquals("REFERENCE:refCellName", elem.toString());
		assertEquals(false, elem.hasNumberValue());
		assertEquals("refCellName", elem.getString());
		assertEquals(true, elem.equals(elem2));

		boolean state = false;
		try {
			assertEquals(new Date(), elem.getDate());
			state = false;
		} catch (IllegalStateException e) {
			// OK
			state = true;
		}
		assertTrue(state);

		try {
			elem.getErrorType();
		} catch (IllegalStateException e) {
			// OK
			return;
		}
		fail();
	}

	public void testElementGroupReference() {
		Element elem = Element.newElement(ElementType.GROUP_REFERENCE, "refGroupName@");
		Element elem2 = Element.newElement(ElementType.GROUP_REFERENCE, "refGroupName@");

		assertEquals("GROUP_REFERENCE:refGroupName@", elem.toString());
		assertEquals(false, elem.hasNumberValue());
		assertEquals("refGroupName@", elem.getString());
		assertEquals(true, elem.equals(elem2));

		boolean state = false;
		try {
			assertEquals(new Date(), elem.getDate());
			state = false;
		} catch (IllegalStateException e) {
			// OK
			state = true;
		}
		assertTrue(state);

		try {
			elem.getErrorType();
		} catch (IllegalStateException e) {
			// OK
			return;
		}
		fail();
	}

	public void testElementTableReference() {
		Element elem = Element.newElement(ElementType.TABLE_REFERENCE, "refTableName#");
		Element elem2 = Element.newElement(ElementType.TABLE_REFERENCE, "refTableName#");

		assertEquals("TABLE_REFERENCE:refTableName#", elem.toString());
		assertEquals(false, elem.hasNumberValue());
		assertEquals("refTableName#", elem.getString());
		assertEquals(true, elem.equals(elem2));

		boolean state = false;
		try {
			assertEquals(new Date(), elem.getDate());
			state = false;
		} catch (IllegalStateException e) {
			// OK
			state = true;
		}
		assertTrue(state);

		try {
			elem.getErrorType();
		} catch (IllegalStateException e) {
			// OK
			return;
		}
		fail();
	}


	public void testElementToString() {
		Element elem = Element.newElement(ElementType.EMPTY, null);
		assertEquals("", elem.toString());

		elem = Element.newElement(ElementType.STRING, "abcde");
		assertEquals("STRING:abcde", elem.toString());

		elem = Element.newElement(ElementType.NUMBER, new BigDecimal("0"));
		assertEquals("NUMBER:0", elem.toString());

		elem = Element.newElement(ElementType.NUMBER, new BigDecimal("1"));
		assertEquals("NUMBER:1", elem.toString());

		elem = Element.newElement(ElementType.NUMBER, new BigDecimal("1234567890"));
		assertEquals("NUMBER:1234567890", elem.toString());

		elem = Element.newElement(ElementType.NUMBER, new BigDecimal("0.1234"));
		assertEquals("NUMBER:0.1234", elem.toString());

		elem = Element.newElement(ElementType.NUMBER, new BigDecimal("-100"));
		assertEquals("NUMBER:-100", elem.toString());

		elem = Element.newElement(ElementType.NUMBER, new BigDecimal("-0.1234"));
		assertEquals("NUMBER:-0.1234", elem.toString());

		elem = Element.newElement(ElementType.BOOLEAN, new Boolean(true));
		assertEquals("BOOLEAN:true", elem.toString());

		elem = Element.newElement(ElementType.BOOLEAN, new Boolean(false));
		assertEquals("BOOLEAN:false", elem.toString());

		elem = Element.newElement(ElementType.ERROR, Element.ErrorType.CIRCULAR_REFERENCE );
		assertEquals("ERROR:CIRCULAR_REFERENCE", elem.toString());

		elem = Element.newElement(ElementType.FUNCTION, "myFunction");
		assertEquals("FUNCTION:myFunction", elem.toString());

		elem = Element.newElement(ElementType.GROUP_REFERENCE, "myGroup@");
		assertEquals("GROUP_REFERENCE:myGroup@", elem.toString());

		elem = Element.newElement(ElementType.TABLE_REFERENCE, "myTable#");
		assertEquals("TABLE_REFERENCE:myTable#", elem.toString());

		elem = Element.newElement(ElementType.OPERATOR, Element.Operator.PLUS);
		assertEquals("OPERATOR:PLUS", elem.toString());

		elem = Element.newElement(ElementType.REFERENCE, "myReference");
		assertEquals("REFERENCE:myReference", elem.toString());
	}



}
