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
import com.nissy_ki_chi.picosheet.core.Expr.Unary.UnaryOperatorType;

import junit.framework.TestCase;

/**
 * FormulaParser.parse() のテスト。<br>
 * 組み立てた木の形を確かめる。位置は比べない(位置は FormulaParserPositionTest で確かめる)。<br>
 * FormulaParser はパッケージの内部のクラスなので、同じパッケージに置いている。
 */
public class FormulaParserParseTest extends TestCase {

	private static Expr parse(String formula) throws FormulaException {
		return ExprShape.of(FormulaParser.parse(FormulaParser.tokenize(formula)));
	}

	private static Expr num(String number) {
		return new Expr.Literal(new Value.Numeric(new BigDecimal(number)), 0);
	}

	private static Expr text(String text) {
		return new Expr.Literal(new Value.Text(text), 0);
	}

	private static Expr bool(boolean b) {
		return new Expr.Literal(new Value.Bool(b), 0);
	}

	private static Expr ref(String name) {
		return new Expr.Reference(name, 0);
	}

	private static Expr group(String name) {
		return new Expr.GroupReference(name, 0);
	}

	private static Expr table(String name) {
		return new Expr.TableReference(name, 0);
	}

	private static Expr bin(BinaryOperatorType type, Expr left, Expr right) {
		return new Expr.Binary(type, left, right, 0);
	}

	private static Expr unary(UnaryOperatorType type, Expr operand) {
		return new Expr.Unary(type, operand, 0);
	}

	private static Expr func(String name, Expr... args) {
		return new Expr.Function(name, List.of(args), 0);
	}

	/**
	 * 値と参照が1つだけの式
	 */
	public void testPrimary() throws FormulaException {
		assertEquals(num("1"), parse("1"));
		assertEquals(text("abc"), parse("\"abc\""));
		assertEquals(bool(true), parse("TRUE"));
		assertEquals(ref("a"), parse("a"));
		assertEquals(ref("s!t#R1C1"), parse("s!t#R1C1"));
	}

	/**
	 * 演算子の優先順位は Excel と同じであること
	 */
	public void testPriority() throws FormulaException {
		// * は + より先
		assertEquals(bin(BinaryOperatorType.PLUS, num("1"), bin(BinaryOperatorType.TIMES, num("2"), num("3"))),
				parse("1+2*3"));
		// ^ は * より先(#14)
		assertEquals(bin(BinaryOperatorType.TIMES, num("2"), bin(BinaryOperatorType.POWER, num("3"), num("2"))),
				parse("2*3^2"));
		// & は + より後
		assertEquals(bin(BinaryOperatorType.CONCATENATE, bin(BinaryOperatorType.PLUS, num("1"), num("2")), text("x")),
				parse("1+2&\"x\""));
		// 比較は & より後
		assertEquals(bin(BinaryOperatorType.EQUALS, bin(BinaryOperatorType.CONCATENATE, ref("a"), ref("b")), text("ab")),
				parse("a&b=\"ab\""));
		// 括弧の中が先
		assertEquals(bin(BinaryOperatorType.TIMES, bin(BinaryOperatorType.PLUS, num("1"), num("2")), num("3")),
				parse("(1+2)*3"));
		assertEquals(num("1"), parse("((1))"));
	}

	/**
	 * 二項演算子は、べき乗も比較も左から順に計算すること
	 */
	public void testLeftAssociative() throws FormulaException {
		assertEquals(bin(BinaryOperatorType.MINUS, bin(BinaryOperatorType.MINUS, num("1"), num("2")), num("3")),
				parse("1-2-3"));
		assertEquals(bin(BinaryOperatorType.DIVIDE, bin(BinaryOperatorType.DIVIDE, num("8"), num("4")), num("2")),
				parse("8/4/2"));
		// Excel では 2^3^2 は (2^3)^2
		assertEquals(bin(BinaryOperatorType.POWER, bin(BinaryOperatorType.POWER, num("2"), num("3")), num("2")),
				parse("2^3^2"));
		// 比較演算子も二項演算子として左から順に(DESIGN.md 3.4)
		assertEquals(bin(BinaryOperatorType.LESS_THAN, bin(BinaryOperatorType.LESS_THAN, num("1"), ref("x")), num("9")),
				parse("1<x<9"));
	}

	/**
	 * すべての比較演算子
	 */
	public void testComparisonOperators() throws FormulaException {
		assertEquals(bin(BinaryOperatorType.LESS_THAN, ref("a"), ref("b")), parse("a<b"));
		assertEquals(bin(BinaryOperatorType.LESS_EQUAL, ref("a"), ref("b")), parse("a<=b"));
		assertEquals(bin(BinaryOperatorType.EQUALS, ref("a"), ref("b")), parse("a=b"));
		assertEquals(bin(BinaryOperatorType.NOT_EQUALS, ref("a"), ref("b")), parse("a<>b"));
		assertEquals(bin(BinaryOperatorType.GREATER_EQUAL, ref("a"), ref("b")), parse("a>=b"));
		assertEquals(bin(BinaryOperatorType.GREATER_THAN, ref("a"), ref("b")), parse("a>b"));
	}

	/**
	 * 単項の + と - を、位置から判定すること
	 */
	public void testUnary() throws FormulaException {
		assertEquals(unary(UnaryOperatorType.NEGATE, num("1")), parse("-1"));
		assertEquals(unary(UnaryOperatorType.PLUS, ref("a")), parse("+a"));
		assertEquals(unary(UnaryOperatorType.NEGATE, unary(UnaryOperatorType.NEGATE, num("1"))), parse("--1"));
		assertEquals(bin(BinaryOperatorType.MINUS, num("1"), unary(UnaryOperatorType.NEGATE, num("1"))),
				parse("1--1"));
		assertEquals(bin(BinaryOperatorType.TIMES, num("2"), unary(UnaryOperatorType.NEGATE, num("3"))),
				parse("2*-3"));
		assertEquals(func("SUM", unary(UnaryOperatorType.NEGATE, num("1"))), parse("SUM(-1)"));
		assertEquals(unary(UnaryOperatorType.NEGATE, bool(true)), parse("-true"));
	}

	/**
	 * 単項の - は ^ より先(Excel では -2^2 は 4)
	 */
	public void testUnaryBeforePower() throws FormulaException {
		assertEquals(bin(BinaryOperatorType.POWER, unary(UnaryOperatorType.NEGATE, num("2")), num("2")),
				parse("-2^2"));
		assertEquals(bin(BinaryOperatorType.POWER, num("2"), unary(UnaryOperatorType.NEGATE, num("1"))),
				parse("2^-1"));
	}

	/**
	 * % は後置の単項演算子で、単項の - より後、^ より先
	 */
	public void testPercent() throws FormulaException {
		assertEquals(unary(UnaryOperatorType.PERCENT, num("50")), parse("50%"));
		assertEquals(unary(UnaryOperatorType.PERCENT, unary(UnaryOperatorType.PERCENT, num("50"))), parse("50%%"));
		assertEquals(unary(UnaryOperatorType.PERCENT, unary(UnaryOperatorType.NEGATE, num("5"))), parse("-5%"));
		assertEquals(bin(BinaryOperatorType.POWER, num("2"), unary(UnaryOperatorType.PERCENT, num("50"))),
				parse("2^50%"));
		assertEquals(bin(BinaryOperatorType.TIMES, ref("a"), unary(UnaryOperatorType.PERCENT, num("10"))),
				parse("a*10%"));
	}

	/**
	 * 関数の呼び出し
	 */
	public void testFunction() throws FormulaException {
		assertEquals(func("NOW"), parse("NOW()"));
		assertEquals(func("SUM", num("1")), parse("SUM(1)"));
		assertEquals(func("SUM", num("1"), bin(BinaryOperatorType.PLUS, ref("a"), num("1")), table("t#")),
				parse("SUM(1, a+1, t#)"));
		assertEquals(func("IF", bin(BinaryOperatorType.GREATER_THAN, ref("a"), num("1")), func("SUM", group("g@")), num("0")),
				parse("IF(a>1, SUM(g@), 0)"));
		assertEquals(bin(BinaryOperatorType.PLUS, func("SUM", group("s!g@")), num("1")), parse("SUM(s!g@)+1"));
		// 登録されていない関数名、小文字の関数名もそのまま
		assertEquals(func("hoge", num("1")), parse("hoge(1)"));
		assertEquals(func("sum", table("t#R1C1:R2C2")), parse("sum(t#R1C1:R2C2)"));
	}

	/**
	 * 誤りのある式は、その位置を持つ FormulaException になること
	 */
	public void testInvalid() {
		// 式が途中で終わっている場合は、最後の字句の位置
		assertInvalid("", 0);
		assertInvalid("1+", 1);
		assertInvalid("(1+2", 3);
		assertInvalid("SUM(1", 4);
		assertInvalid("(", 0);
		// 置けない場所にある字句の位置
		assertInvalid("1 2", 2);
		assertInvalid("1+)", 2);
		assertInvalid(")", 0);
		assertInvalid("1)", 1);
		assertInvalid("*1", 0);
		assertInvalid("1,2", 1);
		assertInvalid("SUM(1,)", 6);
		assertInvalid("SUM(,1)", 4);
		assertInvalid("SUM(1 2)", 6);
		assertInvalid("a b", 2);
		assertInvalid("%1", 0);
	}

	/**
	 * グループとテーブルの参照は、関数の引数にそのまま書く場合だけ使えること
	 */
	public void testRangeOnlyAsFunctionArgument() {
		assertInvalid("g@", 0);
		assertInvalid("t#", 0);
		assertInvalid("1+g@", 2);
		assertInvalid("SUM(g@+1)", 4);
		assertInvalid("SUM(-t#)", 5);
		assertInvalid("SUM((g@))", 5);
	}

	private void assertInvalid(String formula, int expectedPosition) {
		try {
			Expr expr = parse(formula);
			fail("FormulaException が発生しなかった: " + formula + " -> " + expr);
		} catch (FormulaException e) {
			assertEquals(formula, expectedPosition, e.getPosition());
		}
	}
}
