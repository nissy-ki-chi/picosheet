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
package com.nissy_ki_chi.picosheet.core;

import java.math.BigDecimal;
import java.util.List;

import com.nissy_ki_chi.picosheet.core.Expr.Binary.BinaryOperatorType;

import junit.framework.TestCase;

/**
 * FormulaParser.makeFormula() と Formula のテスト。<br>
 * FormulaParser はパッケージの内部のクラスなので、同じパッケージに置いている。
 */
public class FormulaParserMakeFormulaTest extends TestCase {

	private static Expr num(String number) {
		return new Expr.Literal(new Value.Numeric(new BigDecimal(number)));
	}

	/**
	 * 読める式は Parsed になり、parse() と同じ Expr を持つこと
	 */
	public void testParsed() throws FormulaException {
		Formula formula = FormulaParser.makeFormula("1+2");
		assertTrue(formula instanceof Formula.Parsed);
		Formula.Parsed parsed = (Formula.Parsed) formula;
		assertEquals("1+2", parsed.source());
		assertEquals(new Expr.Binary(BinaryOperatorType.PLUS, num("1"), num("2")), parsed.expr());
	}

	/**
	 * 書いたとおりの文字列を source() に持つこと(空白、大文字と小文字、数値の書き方、括弧)
	 */
	public void testSourceIsKeptAsWritten() {
		for (String source : List.of(" 1 + 2 ", "True", "3.+.5", "((a))", "sum( 1 , 2 )")) {
			Formula formula = FormulaParser.makeFormula(source);
			assertTrue(source, formula instanceof Formula.Parsed);
			assertEquals(source, formula.source());
		}
	}

	/**
	 * 登録されていない関数を使う式も Parsed になること
	 */
	public void testUnknownFunctionIsParsed() {
		Formula formula = FormulaParser.makeFormula("hoge(1)");
		assertTrue(formula instanceof Formula.Parsed);
		assertEquals(new Expr.Function("hoge", List.of(num("1"))), ((Formula.Parsed) formula).expr());
	}

	/**
	 * 字句として読めない文字がある式は、例外を投げずに Invalid になり、その位置を持つこと
	 */
	public void testInvalidByTokenize() {
		Formula formula = FormulaParser.makeFormula("1 + $");
		assertTrue(formula instanceof Formula.Invalid);
		Formula.Invalid invalid = (Formula.Invalid) formula;
		assertEquals("1 + $", invalid.source());
		assertEquals(4, invalid.position());
		assertNotNull(invalid.message());
	}

	/**
	 * 式として正しくない式は、例外を投げずに Invalid になり、parse() の例外と同じ位置と説明を持つこと
	 */
	public void testInvalidByParse() {
		for (String source : List.of("1+", "1 2", "(1", "1)", "")) {
			FormulaException expected = null;
			try {
				FormulaParser.parse(FormulaParser.tokenize(source));
			} catch (FormulaException e) {
				expected = e;
			}
			assertNotNull(source, expected);

			Formula formula = FormulaParser.makeFormula(source);
			assertTrue(source, formula instanceof Formula.Invalid);
			Formula.Invalid invalid = (Formula.Invalid) formula;
			assertEquals(source, invalid.source());
			assertEquals(source, expected.getPosition(), invalid.position());
			assertEquals(source, expected.getMessage(), invalid.message());
		}
	}

	/**
	 * 誤りの位置が字句の位置であること
	 */
	public void testInvalidPosition() {
		// 2 つ目の数値の位置
		assertEquals(2, ((Formula.Invalid) FormulaParser.makeFormula("1 2")).position());
		// 途中で終わっている場合は、最後の字句の位置
		assertEquals(1, ((Formula.Invalid) FormulaParser.makeFormula("1+")).position());
		// 字句がなければ 0
		assertEquals(0, ((Formula.Invalid) FormulaParser.makeFormula("")).position());
	}

	/**
	 * null は受け付けないこと
	 */
	public void testNull() {
		try {
			FormulaParser.makeFormula(null);
			fail();
		} catch (NullPointerException e) {
			// OK
		}
	}

	/**
	 * Formula は値として比べられること
	 */
	public void testEquality() {
		assertEquals(FormulaParser.makeFormula("1+2"), FormulaParser.makeFormula("1+2"));
		// 同じ Expr でも、書き方が違えば別の Formula
		assertFalse(FormulaParser.makeFormula("1+2").equals(FormulaParser.makeFormula("1 + 2")));
		assertEquals(FormulaParser.makeFormula("1+"), FormulaParser.makeFormula("1+"));
	}

}
