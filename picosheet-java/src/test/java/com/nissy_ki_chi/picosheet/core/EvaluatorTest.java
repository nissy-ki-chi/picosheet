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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.nissy_ki_chi.picosheet.core.Value.ErrorKind;

import junit.framework.TestCase;

/**
 * Evaluator(式の計算)のテスト。<br>
 * 参照先の値は Map から得るので、Book も Cell も使わない。
 */
public class EvaluatorTest extends TestCase {

	private static final Value.Error NOT_FOUND = new Value.Error(ErrorKind.INVALID_REFERENCE);

	private final Map<String, Value> _cells = new HashMap<>();
	private final Map<String, Argument> _tables = new HashMap<>();
	private final Map<String, Argument> _groups = new HashMap<>();
	/**
	 * 読んだ参照の名前(読んだ順)
	 */
	private final List<String> _reads = new ArrayList<>();
	private final FunctionRegistry _functions = FunctionRegistry.withBuiltins();

	private final Evaluator.ReferenceLookup _lookup = new Evaluator.ReferenceLookup() {
		@Override
		public Value valueOf(Expr.Reference reference) {
			_reads.add(reference.reference());
			return _cells.getOrDefault(reference.reference(), NOT_FOUND);
		}

		@Override
		public Argument areaOf(Expr.TableReference table) {
			_reads.add(table.tableReference());
			return _tables.getOrDefault(table.tableReference(), new Argument.Single(NOT_FOUND));
		}

		@Override
		public Argument bagOf(Expr.GroupReference group) {
			_reads.add(group.groupReference());
			return _groups.getOrDefault(group.groupReference(), new Argument.Single(NOT_FOUND));
		}
	};

	private Value eval(String formula) {
		return eval(formula, MathContext.DECIMAL64);
	}

	private Value eval(String formula, MathContext mc) {
		Formula parsed = FormulaParser.makeFormula(formula);
		assertTrue(formula + " : " + parsed, parsed instanceof Formula.Parsed);
		Evaluator evaluator = new Evaluator(mc, _lookup, _functions);
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
		Evaluator.ReferenceLookup throwing = new Evaluator.ReferenceLookup() {
			@Override
			public Value valueOf(Expr.Reference reference) {
				throw new IllegalStateException(reference.reference());
			}

			@Override
			public Argument areaOf(Expr.TableReference table) {
				throw new IllegalStateException(table.tableReference());
			}

			@Override
			public Argument bagOf(Expr.GroupReference group) {
				throw new IllegalStateException(group.groupReference());
			}
		};
		Evaluator evaluator = new Evaluator(MathContext.DECIMAL64, throwing, _functions);
		// 関数の引数の中で投げられても、そのまま伝わる
		for (String formula : List.of("1+x", "SUM(1,x)", "SUM(x#)", "IF(TRUE,x)")) {
			Formula.Parsed parsed = (Formula.Parsed) FormulaParser.makeFormula(formula);
			try {
				evaluator.evaluate(parsed.expr());
				fail(formula);
			} catch (IllegalStateException e) {
				assertTrue(formula + " : " + e.getMessage(), e.getMessage().startsWith("x"));
			}
		}
	}

	/**
	 * 範囲 2行2列のテーブルを用意する(値は行ごとの順)
	 */
	private void putTable(String name, Value v11, Value v12, Value v21, Value v22) {
		_tables.put(name, new ValueArea(2, 2, List.of(v11, v12, v21, v22)));
	}

	private static Value num(String n) {
		return new Value.Numeric(new BigDecimal(n));
	}

	private static Value error(ErrorKind kind) {
		return new Value.Error(kind);
	}

	/**
	 * SUM: 直接書いた値は数値に変換して足す
	 */
	public void testSum() {
		assertNumber("6", "SUM(1,2,3)");
		assertNumber("0.3", "SUM(0.1,0.2)");
		assertNumber("2", "SUM(\"1\",TRUE)");
		assertError(ErrorKind.WRONG_VALUE, "SUM(\"a\")");
		assertNumber("6", "SUM(1,2)*2");
		// 引数の数が合わない
		assertError(ErrorKind.WRONG_VALUE, "SUM()");
	}

	/**
	 * SUM: 範囲の中は数値だけを足す(DESIGN.md 1.2)
	 */
	public void testSumRange() {
		putTable("t#", num("1"), new Value.Text("2"), new Value.Bool(true), new Value.Empty());
		_groups.put("g@", new ValueBag(List.of(num("3"), new Value.Bool(true), new Value.Text("x"))));
		assertNumber("1", "SUM(t#)");
		assertNumber("3", "SUM(g@)");
		assertNumber("14", "SUM(t#,g@,10)");
	}

	/**
	 * SUM: セル1つの参照は範囲として扱う。Excel と同じく SUM(A1) の論理値は数えず、SUM(TRUE) は数える
	 */
	public void testSumReference() {
		_cells.put("a", new Value.Bool(true));
		_cells.put("n", num("5"));
		assertNumber("0", "SUM(a)");
		assertNumber("1", "SUM(TRUE)");
		assertNumber("5", "SUM(n)");
		// 計算した結果は値1つ
		assertNumber("1", "SUM(a+0)");
	}

	/**
	 * 関数名は大文字と小文字を区別しない(#6)。登録されていない関数は #NAME?
	 */
	public void testFunctionName() {
		assertNumber("3", "sum(1,2)");
		assertNumber("3", "Sum(1,2)");
		assertError(ErrorKind.UNRECOGNIZED_NAME, "NOSUCH(1)");
	}

	/**
	 * 引数のエラーは関数を呼ばずに返す(#11)。範囲の中は行ごとの順で最初のエラー
	 */
	public void testErrorInArgument() {
		assertError(ErrorKind.DIVIDE_BY_ZERO, "SUM(1/0,2)");
		// Excel で確かめた配置(DESIGN.md 2.6): 行ごとの順なら #DIV/0!、列ごとの順なら #N/A
		putTable("t#", num("11"), error(ErrorKind.DIVIDE_BY_ZERO), error(ErrorKind.NOT_AVAILABLE),
				error(ErrorKind.DIVIDE_BY_ZERO));
		assertError(ErrorKind.DIVIDE_BY_ZERO, "SUM(t#)");
		putTable("u#", num("11"), error(ErrorKind.NOT_AVAILABLE), error(ErrorKind.DIVIDE_BY_ZERO), num("1"));
		assertError(ErrorKind.NOT_AVAILABLE, "SUM(u#)");
		// 引数の順で最初のエラー
		assertError(ErrorKind.DIVIDE_BY_ZERO, "SUM(1/0,u#)");
		assertError(ErrorKind.NOT_AVAILABLE, "SUM(u#,1/0)");
		// エラーを受け取らない関数は呼ばれない
		_functions.register("NEVER", FunctionSpec.of(ParameterKind.ANY), (args, context) -> {
			throw new AssertionError("NEVER was called");
		});
		assertError(ErrorKind.DIVIDE_BY_ZERO, "NEVER(1/0)");
		assertError(ErrorKind.DIVIDE_BY_ZERO, "NEVER(t#)");
	}

	/**
	 * 見つからない範囲は #REF!
	 */
	public void testMissingRange() {
		assertError(ErrorKind.INVALID_REFERENCE, "SUM(nothing#)");
		assertError(ErrorKind.INVALID_REFERENCE, "SUM(nothing@)");
	}

	/**
	 * 引数の種類(DESIGN.md 2.6)
	 */
	public void testParameterKind() {
		putTable("t#", num("1"), num("2"), num("3"), num("4"));
		_groups.put("g@", new ValueBag(List.of(num("1"))));
		_cells.put("a", num("7"));
		// テーブルの範囲: 行と列の数を返す
		_functions.register("SHAPE", FunctionSpec.of(ParameterKind.AREA), (args, context) -> {
			Argument.Area area = (Argument.Area) args.get(0);
			return new Value.Numeric(BigDecimal.valueOf(area.rows() * 100 + area.columns()));
		});
		assertNumber("202", "SHAPE(t#)");
		assertNumber("101", "SHAPE(a)");
		assertError(ErrorKind.WRONG_VALUE, "SHAPE(g@)");
		assertError(ErrorKind.WRONG_VALUE, "SHAPE(1)");
		assertError(ErrorKind.WRONG_VALUE, "SHAPE(a+1)");
		// 値: 受け取った値をそのまま返す
		_functions.register("SAME", FunctionSpec.of(ParameterKind.VALUE),
				(args, context) -> ((Argument.Single) args.get(0)).value());
		assertNumber("7", "SAME(a)");
		assertNumber("8", "SAME(a+1)");
		assertError(ErrorKind.WRONG_VALUE, "SAME(t#)");
		assertError(ErrorKind.WRONG_VALUE, "SAME(g@)");
		// 範囲: テーブルの範囲かグループ
		_functions.register("KIND", FunctionSpec.of(ParameterKind.RANGE),
				(args, context) -> new Value.Text(args.get(0) instanceof Argument.Area ? "area" : "bag"));
		assertText("area", "KIND(t#)");
		assertText("bag", "KIND(g@)");
		assertText("area", "KIND(a)");
		assertError(ErrorKind.WRONG_VALUE, "KIND(1)");
	}

	/**
	 * 引数の数: 必ず書く引数、省略できる引数、繰り返せる引数
	 */
	public void testParameterCount() {
		SheetFunction count = (args, context) -> new Value.Numeric(BigDecimal.valueOf(args.size()));
		_functions.register("OPT", FunctionSpec.of(ParameterKind.VALUE).withOptional(ParameterKind.VALUE), count);
		assertError(ErrorKind.WRONG_VALUE, "OPT()");
		assertNumber("1", "OPT(1)");
		assertNumber("2", "OPT(1,2)");
		assertError(ErrorKind.WRONG_VALUE, "OPT(1,2,3)");
		_functions.register("REP", FunctionSpec.of().withRepeated(ParameterKind.VALUE), count);
		assertNumber("0", "REP()");
		assertNumber("3", "REP(1,2,3)");
	}

	/**
	 * エラーを受け取る関数
	 */
	public void testAcceptsErrors() {
		putTable("t#", num("1"), error(ErrorKind.DIVIDE_BY_ZERO), error(ErrorKind.NOT_AVAILABLE), num("2"));
		SheetFunction countErrors = (args, context) -> {
			int count = 0;
			for (Argument arg : args) {
				for (Value value : Evaluator.valuesOf(arg)) {
					if (value instanceof Value.Error) {
						count++;
					}
				}
			}
			return new Value.Numeric(BigDecimal.valueOf(count));
		};
		_functions.register("COUNTERR", FunctionSpec.of().withRepeated(ParameterKind.ANY).acceptingErrors(), countErrors);
		assertNumber("3", "COUNTERR(1/0,t#,1)");
		// 値1つを受け付ける位置なら、見つからない範囲もエラーの値として渡す
		assertNumber("1", "COUNTERR(nothing#)");
		// テーブルの範囲しか受け付けない位置には、見つからない範囲を渡さない
		_functions.register("AREAERR", FunctionSpec.of(ParameterKind.AREA).acceptingErrors(), countErrors);
		assertNumber("2", "AREAERR(t#)");
		assertError(ErrorKind.INVALID_REFERENCE, "AREAERR(nothing#)");
	}

	/**
	 * IF
	 */
	public void testIf() {
		assertNumber("1", "IF(TRUE,1,2)");
		assertNumber("2", "IF(FALSE,1,2)");
		assertBool(false, "IF(FALSE,1)");
		assertText("y", "IF(1,\"y\",\"n\")");
		assertText("n", "IF(0,\"y\",\"n\")");
		assertNumber("1", "IF(\"true\",1,2)");
		assertError(ErrorKind.WRONG_VALUE, "IF(\"a\",1,2)");
		assertError(ErrorKind.DIVIDE_BY_ZERO, "IF(1/0,1,2)");
		assertText("big", "IF(SUM(1,2)>2,\"big\",\"small\")");
		// 引数の数
		assertError(ErrorKind.WRONG_VALUE, "IF(TRUE)");
		assertError(ErrorKind.WRONG_VALUE, "IF(TRUE,1,2,3)");
		// 範囲は値1つにならない
		putTable("t#", num("1"), num("2"), num("3"), num("4"));
		assertError(ErrorKind.WRONG_VALUE, "IF(t#,1,2)");
		assertError(ErrorKind.WRONG_VALUE, "IF(TRUE,t#,2)");
	}

	/**
	 * IF は使わない側の引数を計算しない(DESIGN.md 3.8)
	 */
	public void testIfIsLazy() {
		assertNumber("1", "IF(TRUE,1,1/0)");
		_cells.put("x", num("1"));
		_cells.put("y", num("2"));
		_reads.clear();
		assertNumber("1", "IF(TRUE,x,y)");
		assertEquals(List.of("x"), _reads);
		_reads.clear();
		assertNumber("2", "IF(FALSE,x,y)");
		assertEquals(List.of("y"), _reads);
	}

	/**
	 * 関数の一覧
	 */
	public void testRegistry() {
		assertTrue(_functions.contains("sum"));
		assertTrue(_functions.names().containsAll(List.of("IF", "SUM")));
		// 置き換え
		_functions.register("sum", FunctionSpec.of(), (args, context) -> new Value.Text("replaced"));
		assertText("replaced", "SUM()");
		// 削除
		_functions.remove("Sum");
		assertFalse(_functions.contains("SUM"));
		assertError(ErrorKind.UNRECOGNIZED_NAME, "SUM(1)");
	}

}
