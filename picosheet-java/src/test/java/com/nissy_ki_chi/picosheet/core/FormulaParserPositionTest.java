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

import junit.framework.TestCase;

/**
 * FormulaParser.parse() が組み立てた Expr の位置のテスト。<br>
 * 木の形は FormulaParserParseTest で確かめるので、ここでは位置だけを確かめる。
 * 位置は、その部分の木の先頭の字句の、元の式の中の位置(0始まり)。
 */
public class FormulaParserPositionTest extends TestCase {

	private static Expr parse(String formula) throws FormulaException {
		return FormulaParser.parse(FormulaParser.tokenize(formula));
	}

	/**
	 * 値と参照は、その字句の位置を持つこと
	 */
	public void testPrimary() throws FormulaException {
		assertEquals(0, parse("1").position());
		assertEquals(2, parse("  a").position());
		assertEquals(1, parse(" \"abc\"").position());
		assertEquals(3, parse("   TRUE").position());
	}

	/**
	 * 二項演算は、左の被演算子の先頭の位置を持つこと(演算子の記号の位置ではない)
	 */
	public void testBinary() throws FormulaException {
		Expr.Binary plus = (Expr.Binary) parse("1 + 2*3");
		assertEquals(0, plus.position());
		assertEquals(0, plus.leftOperand().position());
		Expr.Binary times = (Expr.Binary) plus.rightOperand();
		assertEquals(4, times.position());
		assertEquals(4, times.leftOperand().position());
		assertEquals(6, times.rightOperand().position());
	}

	/**
	 * 左から結び付く二項演算は、どの段も式の先頭の位置を持つこと
	 */
	public void testLeftAssociative() throws FormulaException {
		Expr.Binary outer = (Expr.Binary) parse(" 1-2-3");
		assertEquals(1, outer.position());
		assertEquals(1, outer.leftOperand().position());
		assertEquals(5, outer.rightOperand().position());
	}

	/**
	 * 括弧で始まる部分の木は、その左括弧の位置を持つこと。括弧の内側の木は、内側の先頭の位置を持つこと
	 */
	public void testParenthesis() throws FormulaException {
		Expr.Binary times = (Expr.Binary) parse("(1+2)*3");
		assertEquals(0, times.position());
		assertEquals(1, times.leftOperand().position());

		Expr.Binary plus = (Expr.Binary) parse("((1))+2");
		assertEquals(0, plus.position());
		assertEquals(2, plus.leftOperand().position());

		// 式全体が括弧で囲まれている場合、括弧は木に残らないので、内側の木の位置になる
		assertEquals(1, parse("(1+2)").position());
	}

	/**
	 * 前置の単項演算は演算子の位置を、後置の % は被演算子の先頭の位置を持つこと
	 */
	public void testUnary() throws FormulaException {
		Expr.Unary negate = (Expr.Unary) parse(" -a");
		assertEquals(1, negate.position());
		assertEquals(2, negate.operand().position());

		Expr.Unary minusMinus = (Expr.Unary) parse("--1");
		assertEquals(0, minusMinus.position());
		assertEquals(1, minusMinus.operand().position());

		Expr.Unary percent = (Expr.Unary) parse(" 50%");
		assertEquals(1, percent.position());

		Expr.Unary percentOfParenthesis = (Expr.Unary) parse("(a+1)%%");
		assertEquals(0, percentOfParenthesis.position());
		assertEquals(0, ((Expr.Unary) percentOfParenthesis.operand()).position());
	}

	/**
	 * 関数は関数名の位置を、引数はそれぞれの先頭の位置を持つこと
	 */
	public void testFunction() throws FormulaException {
		Expr.Function sum = (Expr.Function) parse(" SUM(1, a+2, g@, t#)");
		assertEquals(1, sum.position());
		assertEquals(5, sum.arguments().get(0).position());
		assertEquals(8, sum.arguments().get(1).position());
		assertEquals(13, sum.arguments().get(2).position());
		assertEquals(17, sum.arguments().get(3).position());
	}

}
