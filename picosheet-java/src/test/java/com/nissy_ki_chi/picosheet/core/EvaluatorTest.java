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
import java.math.MathContext;
import java.util.HashMap;
import java.util.Map;

import com.nissy_ki_chi.picosheet.core.Value.ErrorKind;

import junit.framework.TestCase;

/**
 * Evaluator(式の計算)のテスト。<br>
 * 参照先の値は Map から得るので、Book も Cell も使わない。
 */
public class EvaluatorTest extends TestCase {

	private final Map<String, Value> _cells = new HashMap<>();

	private Value eval(String formula) {
		return eval(formula, MathContext.DECIMAL64);
	}

	private Value eval(String formula, MathContext mc) {
		Formula parsed = FormulaParser.makeFormula(formula);
		assertTrue(formula + " : " + parsed, parsed instanceof Formula.Parsed);
		Evaluator evaluator = new Evaluator(mc, reference -> _cells.getOrDefault(reference.reference(),
				new Value.Error(ErrorKind.INVALID_REFERENCE)));
		return evaluator.evaluate(((Formula.Parsed) parsed).expr());
	}

	private void assertNumber(String expected, String formula) {
		Value value = eval(formula);
		assertTrue(formula + " : " + value, value instanceof Value.Numeric);
		BigDecimal actual = ((Value.Numeric) value).value();
		assertTrue(formula + " : expected " + expected + " but " + actual,
				new BigDecimal(expected).compareTo(actual) == 0);
	}

	private void assertText(String expected, String formula) {
		assertEquals(formula, new Value.Text(expected), eval(formula));
	}

	private void assertBool(boolean expected, String formula) {
		assertEquals(formula, new Value.Bool(expected), eval(formula));
	}

	private void assertError(ErrorKind expected, String formula) {
		assertEquals(formula, new Value.Error(expected), eval(formula));
	}

	/**
	 * リテラルはそのままの値になること
	 */
	public void testLiteral() {
		assertNumber("1.5", "1.5");
		assertText("abc", "\"abc\"");
		assertBool(true, "TRUE");
	}

	/**
	 * 四則演算と優先順位(#14)
	 */
	public void testArithmetic() {
		assertNumber("3", "1+2");
		assertNumber("-1", "1-2");
		assertNumber("6", "2*3");
		assertNumber("2.5", "5/2");
		assertNumber("18", "2*3^2");
		assertNumber("0.3", "0.1+0.2");
		assertNumber("0.3333333333333333", "1/3");
	}

	/**
	 * 単項演算子(単項の - は ^ より先)
	 */
	public void testUnary() {
		assertNumber("4", "-2^2");
		assertNumber("-3", "-3");
		assertNumber("0.5", "50%");
		assertNumber("-3", "-\"3\"");
		assertNumber("-1", "-TRUE");
		// 単項の + は値を変えない
		assertText("a", "+\"a\"");
	}

	/**
	 * べき乗
	 */
	public void testPower() {
		assertNumber("1024", "2^10");
		assertNumber("0.5", "2^-1");
		assertNumber("2", "4^0.5");
		assertNumber("1", "5^0");
		assertError(ErrorKind.INVALID_NUMBER, "0^0");
		assertError(ErrorKind.DIVIDE_BY_ZERO, "0^-1");
		assertError(ErrorKind.INVALID_NUMBER, "(-8)^(1/3)");
	}

	/**
	 * 算術演算での型の変換(空は0、論理値は1か0、数値として読める文字列は数値)
	 */
	public void testArithmeticConversion() {
		_cells.put("empty", new Value.Empty());
		assertNumber("2", "TRUE+1");
		assertNumber("1", "empty+1");
		assertNumber("2", "\"1\"+1");
		assertNumber("6", "\" 2 \"*3");
		assertError(ErrorKind.WRONG_VALUE, "\"a\"+1");
		assertError(ErrorKind.WRONG_VALUE, "\"\"+1");
	}

	/**
	 * エラーの伝わり方。最初に起きたエラーの種類を保つ(#12)
	 */
	public void testErrorPropagation() {
		assertError(ErrorKind.DIVIDE_BY_ZERO, "1/0");
		assertError(ErrorKind.DIVIDE_BY_ZERO, "1/0+1");
		assertError(ErrorKind.DIVIDE_BY_ZERO, "-(1/0)");
		assertError(ErrorKind.DIVIDE_BY_ZERO, "1/0&\"a\"");
		assertError(ErrorKind.DIVIDE_BY_ZERO, "1/0=1");
		// 両方がエラーなら左のエラー
		assertError(ErrorKind.DIVIDE_BY_ZERO, "1/0+(\"a\"+1)");
		assertError(ErrorKind.WRONG_VALUE, "(\"a\"+1)+1/0");
	}

	/**
	 * 精度が無制限の MathContext で割り切れない場合は #NUM! になること
	 */
	public void testUnlimitedMathContext() {
		assertEquals(new Value.Error(ErrorKind.INVALID_NUMBER), eval("1/3", MathContext.UNLIMITED));
		Value value = eval("1/4", MathContext.UNLIMITED);
		assertEquals(0, new BigDecimal("0.25").compareTo(((Value.Numeric) value).value()));
	}

	/**
	 * 文字列結合
	 */
	public void testConcatenate() {
		_cells.put("empty", new Value.Empty());
		assertText("ab", "\"a\"&\"b\"");
		assertText("a1", "\"a\"&1");
		assertText("1.5", "1.50&\"\"");
		// 10^3 は BigDecimal では 1E+3 になるが、文字列では指数表記を使わない
		assertText("1000", "10^3&\"\"");
		assertText("TRUE", "TRUE&\"\"");
		assertText("x", "empty&\"x\"");
	}

	/**
	 * 比較(#15、#16、#17、3.4)
	 */
	public void testComparison() {
		_cells.put("empty", new Value.Empty());
		assertBool(true, "1<2");
		assertBool(true, "1=1.0");
		assertBool(true, "1<>2");
		assertBool(true, "2>=2");
		// 文字列は大文字と小文字を区別しない
		assertBool(true, "\"a\"=\"A\"");
		assertBool(true, "\"a\"<\"B\"");
		// 種類が違う値は 数値 < 文字列 < 論理値
		assertBool(false, "TRUE<9");
		assertBool(true, "1<\"a\"");
		assertBool(true, "\"a\"<TRUE");
		assertBool(false, "1=\"1\"");
		// 空は相手の種類の 0、""、FALSE として比べる
		assertBool(true, "empty=0");
		assertBool(true, "empty=\"\"");
		assertBool(true, "empty=FALSE");
		assertBool(true, "empty=empty");
		// 比較演算子は左から順に計算する
		assertBool(false, "1<5<3");
	}

	/**
	 * 参照は ReferenceLookup から値を得ること
	 */
	public void testReference() {
		_cells.put("x", new Value.Numeric(new BigDecimal("2")));
		_cells.put("y", new Value.Error(ErrorKind.NOT_AVAILABLE));
		assertNumber("3", "x+1");
		assertError(ErrorKind.NOT_AVAILABLE, "y+1");
		assertError(ErrorKind.INVALID_REFERENCE, "nothing+1");
	}

	/**
	 * ReferenceLookup が投げた例外は、そのまま呼び出し側に伝わること
	 */
	public void testLookupExceptionPassesThrough() {
		Formula.Parsed parsed = (Formula.Parsed) FormulaParser.makeFormula("1+x");
		Evaluator evaluator = new Evaluator(MathContext.DECIMAL64, reference -> {
			throw new IllegalStateException(reference.reference());
		});
		try {
			evaluator.evaluate(parsed.expr());
			fail();
		} catch (IllegalStateException e) {
			assertEquals("x", e.getMessage());
		}
	}

	/**
	 * 関数はまだ計算できないこと
	 */
	public void testFunctionIsNotSupportedYet() {
		try {
			eval("SUM(1)");
			fail();
		} catch (UnsupportedOperationException e) {
			// 期待どおり
		}
	}

}
