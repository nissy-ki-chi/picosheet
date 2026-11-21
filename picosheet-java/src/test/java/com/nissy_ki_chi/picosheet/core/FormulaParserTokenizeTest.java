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

import com.nissy_ki_chi.picosheet.core.Token.Operator.Symbol;

import junit.framework.TestCase;

/**
 * FormulaParser.tokenize() のテスト。<br>
 * FormulaParser はパッケージの内部のクラスなので、同じパッケージに置いている。
 */
public class FormulaParserTokenizeTest extends TestCase {

	private static Token op(Symbol symbol, int position) {
		return new Token.Operator(symbol, position);
	}

	private static Token num(String number, int position) {
		return new Token.Literal(new Value.Numeric(new BigDecimal(number)), position);
	}

	private static Token text(String text, int position) {
		return new Token.Literal(new Value.Text(text), position);
	}

	private static Token bool(boolean b, int position) {
		return new Token.Literal(new Value.Bool(b), position);
	}

	private static Token ref(String name, int position) {
		return new Token.Reference(name, position);
	}

	private static Token group(String name, int position) {
		return new Token.GroupReference(name, position);
	}

	private static Token table(String name, int position) {
		return new Token.TableReference(name, position);
	}

	private static Token func(String name, int position) {
		return new Token.Function(name, position);
	}

	private static List<Token> tokenize(String formula) throws FormulaException {
		return FormulaParser.tokenize(formula);
	}

	/**
	 * 空の式は空の並びになること
	 */
	public void testEmpty() throws FormulaException {
		assertEquals(List.of(), tokenize(""));
		assertEquals(List.of(), tokenize("   "));
	}

	/**
	 * 各トークンが元の式の中の位置を持ち、空白は捨てられること
	 */
	public void testPositionAndSpace() throws FormulaException {
		assertEquals(List.of(num("1", 0), op(Symbol.PLUS, 1), num("2", 2)), tokenize("1+2"));
		assertEquals(List.of(num("1", 1), op(Symbol.PLUS, 3), num("2", 6)), tokenize(" 1 +  2 "));
	}

	/**
	 * 単項か二項かを判定せず、+ と - はそのまま返すこと
	 */
	public void testNoUnaryDecision() throws FormulaException {
		assertEquals(List.of(op(Symbol.MINUS, 0), num("1", 1)), tokenize("-1"));
		assertEquals(List.of(num("2", 0), op(Symbol.TIMES, 1), op(Symbol.MINUS, 2), num("3", 3)), tokenize("2*-3"));
		assertEquals(List.of(op(Symbol.PLUS, 0), op(Symbol.MINUS, 1), ref("x", 2)), tokenize("+-x"));
	}

	/**
	 * すべての記号を字句に分けられること
	 */
	public void testOperators() throws FormulaException {
		assertEquals(
				List.of(op(Symbol.PLUS, 0), op(Symbol.MINUS, 1), op(Symbol.TIMES, 2), op(Symbol.DIVIDE, 3),
						op(Symbol.POWER, 4), op(Symbol.PERCENT, 5), op(Symbol.CONCATENATE, 6),
						op(Symbol.COMMA, 7), op(Symbol.LEFT_PARENTHESIS, 8), op(Symbol.RIGHT_PARENTHESIS, 9)),
				tokenize("+-*/^%&,()"));
	}

	/**
	 * 2文字の比較演算子を1文字の比較演算子より優先すること
	 */
	public void testComparisonOperators() throws FormulaException {
		assertEquals(List.of(op(Symbol.LESS_EQUAL, 0), op(Symbol.GREATER_EQUAL, 3), op(Symbol.NOT_EQUALS, 6)),
				tokenize("<= >= <>"));
		assertEquals(List.of(op(Symbol.LESS_THAN, 0), op(Symbol.GREATER_THAN, 2), op(Symbol.EQUALS, 4)),
				tokenize("< > ="));
	}

	/**
	 * 数値のリテラル
	 */
	public void testNumber() throws FormulaException {
		assertEquals(List.of(num("12", 0)), tokenize("12"));
		assertEquals(List.of(num("1.5", 0)), tokenize("1.5"));
		assertEquals(List.of(num(".5", 0)), tokenize(".5"));
		assertEquals(List.of(num("3.", 0)), tokenize("3."));
	}

	/**
	 * 文字列のリテラルは前後の " を除くこと
	 */
	public void testText() throws FormulaException {
		assertEquals(List.of(text("a b", 0)), tokenize("\"a b\""));
		assertEquals(List.of(text("", 0), op(Symbol.CONCATENATE, 2), text("x", 3)), tokenize("\"\"&\"x\""));
	}

	/**
	 * 真偽値のリテラルは大文字と小文字を区別せず、true、false で始まる名前は参照になること
	 */
	public void testBoolean() throws FormulaException {
		assertEquals(List.of(bool(true, 0)), tokenize("true"));
		assertEquals(List.of(bool(true, 0)), tokenize("TRUE"));
		assertEquals(List.of(bool(false, 0)), tokenize("False"));
		assertEquals(List.of(ref("trueValue", 0), op(Symbol.PLUS, 9), num("1", 10)), tokenize("trueValue+1"));
		assertEquals(List.of(ref("falsehood", 0)), tokenize("falsehood"));
	}

	/**
	 * セル・グループ・テーブルの参照を種類ごとに分けること
	 */
	public void testReferences() throws FormulaException {
		assertEquals(List.of(ref("a", 0)), tokenize("a"));
		assertEquals(List.of(ref("s!a", 0)), tokenize("s!a"));
		assertEquals(List.of(ref("t#R1C2", 0)), tokenize("t#R1C2"));
		assertEquals(List.of(ref("s!t#R1C2", 0)), tokenize("s!t#R1C2"));

		assertEquals(List.of(group("g@", 0)), tokenize("g@"));
		assertEquals(List.of(group("s!g@", 0)), tokenize("s!g@"));

		assertEquals(List.of(table("t#", 0)), tokenize("t#"));
		assertEquals(List.of(table("s!t#", 0)), tokenize("s!t#"));
		assertEquals(List.of(table("t#R1C1:R2C2", 0)), tokenize("t#R1C1:R2C2"));
		assertEquals(List.of(table("s!t#RxC1", 0)), tokenize("s!t#RxC1"));
	}

	/**
	 * 関数名は登録されているかを確かめず、名前と左括弧に分けること
	 */
	public void testFunction() throws FormulaException {
		assertEquals(
				List.of(func("SUM", 0), op(Symbol.LEFT_PARENTHESIS, 3), ref("a", 4), op(Symbol.COMMA, 5),
						num("1", 7), op(Symbol.RIGHT_PARENTHESIS, 8)),
				tokenize("SUM(a, 1)"));
		// 登録されていない関数名、小文字の関数名もそのまま返す
		assertEquals(List.of(func("hoge", 0), op(Symbol.LEFT_PARENTHESIS, 4), op(Symbol.RIGHT_PARENTHESIS, 5)),
				tokenize("hoge()"));
		assertEquals(List.of(func("sum", 0), op(Symbol.LEFT_PARENTHESIS, 3), num("1", 4), op(Symbol.RIGHT_PARENTHESIS, 5)),
				tokenize("sum(1)"));
	}

	/**
	 * 字句として読めない文字があると、その位置を持つ FormulaException になること
	 */
	public void testInvalidCharacter() {
		assertInvalid("1 $ 2", 2);
		assertInvalid("a+?", 2);
		assertInvalid("\"abc", 0);
		assertInvalid("1+2 !", 4);
	}

	private void assertInvalid(String formula, int expectedPosition) {
		try {
			tokenize(formula);
			fail("FormulaException が発生しなかった: " + formula);
		} catch (FormulaException e) {
			assertEquals(formula, expectedPosition, e.getPosition());
		}
	}
}
