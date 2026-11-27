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
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import com.nissy_ki_chi.picosheet.core.Value.ErrorKind;

/**
 * 式の木(Expr)を計算して値(Value)を返します(DESIGN.md 3.8)。<br>
 * ほかのセルは計算しません。参照先の値は、渡された ReferenceLookup から得ます。
 * セルの状態を知らず、副作用を持たないので、同じ式と同じ参照先の値からは、いつも同じ値を返します。<br>
 * 計算した結果のエラーは、例外ではなく Value.Error として返します。
 * ReferenceLookup が投げた例外は、そのまま呼び出し側に伝わります。<br>
 * 型の変換と比較の決まりは Excel に合わせます(DESIGN.md 1.2)。
 *
 * @author yusuke nishikawa
 *
 */
final class Evaluator {

	/**
	 * 参照から値を得る口です。<br>
	 * 参照の名前を解決し、参照先のセルの値を返します。範囲(テーブル・グループ)は、その時点の値の写しにします(DESIGN.md 2.6)。
	 * 参照先が見つからない場合は、Value.Error(INVALID_REFERENCE) を返します(範囲の場合は、それを Argument.Single に包みます)。
	 */
	interface ReferenceLookup {

		/**
		 * セル1つの値を返します。
		 */
		Value valueOf(Expr.Reference reference);

		/**
		 * テーブルの範囲の値を返します。Argument.Area か、見つからなければエラーの Argument.Single を返します。
		 */
		Argument areaOf(Expr.TableReference table);

		/**
		 * グループの値を返します。Argument.Bag か、見つからなければエラーの Argument.Single を返します。
		 */
		Argument bagOf(Expr.GroupReference group);
	}

	/**
	 * 関数に渡す計算の設定です。
	 */
	private record Context(MathContext mathContext) implements FunctionContext {
	}

	static final Value.Numeric ZERO = new Value.Numeric(BigDecimal.ZERO);
	static final Value.Numeric ONE = new Value.Numeric(BigDecimal.ONE);
	static final Value.Error WRONG_VALUE = new Value.Error(ErrorKind.WRONG_VALUE);
	static final Value.Error DIVIDE_BY_ZERO = new Value.Error(ErrorKind.DIVIDE_BY_ZERO);
	static final Value.Error INVALID_NUMBER = new Value.Error(ErrorKind.INVALID_NUMBER);
	static final Value.Error UNRECOGNIZED_NAME = new Value.Error(ErrorKind.UNRECOGNIZED_NAME);

	private final MathContext _mc;
	private final ReferenceLookup _lookup;
	private final FunctionRegistry _functions;
	private final Context _context;

	/**
	 * @param mc 計算に使う MathContext
	 * @param lookup 参照から値を得る口
	 * @param functions 関数の一覧
	 */
	Evaluator(MathContext mc, ReferenceLookup lookup, FunctionRegistry functions) {
		this._mc = Objects.requireNonNull(mc, "MathContext cannot be null");
		this._lookup = Objects.requireNonNull(lookup, "Lookup cannot be null");
		this._functions = Objects.requireNonNull(functions, "Functions cannot be null");
		this._context = new Context(mc);
	}

	/**
	 * 式の木を計算します。
	 * @param expr 式の木
	 * @return 計算した結果
	 */
	Value evaluate(Expr expr) {
		return switch (expr) {
			case Expr.Literal literal -> literal.value();
			case Expr.Reference reference -> Objects.requireNonNull(_lookup.valueOf(reference),
					"Lookup returned null: " + reference.reference());
			case Expr.Unary unary -> evaluateUnary(unary);
			case Expr.Binary binary -> evaluateBinary(binary);
			case Expr.Function function -> evaluateFunction(function);
			// 範囲は関数の引数でしか使えない(FormulaParser も、それ以外の位置では受け付けない)
			case Expr.GroupReference group -> WRONG_VALUE;
			case Expr.TableReference table -> WRONG_VALUE;
		};
	}

	/**
	 * 関数を計算します(DESIGN.md 2.6)。
	 */
	private Value evaluateFunction(Expr.Function function) {
		FunctionRegistry.Entry entry = _functions.find(function.functionName());
		return switch (entry) {
			case null -> UNRECOGNIZED_NAME;
			case FunctionRegistry.Lazy lazy -> Objects.requireNonNull(lazy.function().apply(lazyArguments(function.arguments())),
					"Function returned null: " + function.functionName());
			case FunctionRegistry.Eager eager -> callEager(function, eager);
		};
	}

	/**
	 * ふつうの関数を呼びます。<br>
	 * 引数の数と種類を確かめ、引数を計算し、エラーを受け取らない関数なら引数のエラーを確かめてから呼びます。
	 */
	private Value callEager(Expr.Function function, FunctionRegistry.Eager eager) {
		FunctionSpec spec = eager.spec();
		List<Expr> exprs = function.arguments();
		if (!spec.acceptsCount(exprs.size())) {
			return WRONG_VALUE;
		}
		// 種類は式の形だけで決まるので、引数を計算する前に確かめる
		for (int i = 0; i < exprs.size(); i++) {
			if (!fits(spec.kindAt(i), exprs.get(i))) {
				return WRONG_VALUE;
			}
		}
		List<Argument> args = new ArrayList<>(exprs.size());
		for (int i = 0; i < exprs.size(); i++) {
			args.add(toArgument(spec.kindAt(i), exprs.get(i)));
		}
		for (int i = 0; i < args.size(); i++) {
			Value error = firstError(args.get(i));
			if (error == null) {
				continue;
			}
			// 見つからない範囲は、範囲の代わりに値1つになるので、エラーを受け取る関数にも渡さない
			if (!spec.acceptsErrors() || (args.get(i) instanceof Argument.Single && !spec.kindAt(i).acceptsSingle())) {
				return error;
			}
		}
		return Objects.requireNonNull(eager.function().apply(Collections.unmodifiableList(args), _context),
				"Function returned null: " + function.functionName());
	}

	/**
	 * 引数の式が、その位置の種類に合うかを返します。<br>
	 * 範囲を受け付ける位置にはセル1つの参照も書けます。値の位置に範囲は書けず、テーブルの範囲の位置にグループや値は書けません。
	 */
	private static boolean fits(ParameterKind kind, Expr expr) {
		return switch (expr) {
			case Expr.TableReference table -> kind != ParameterKind.VALUE;
			case Expr.GroupReference group -> kind.acceptsBag();
			case Expr.Reference reference -> true;
			default -> kind.acceptsSingle();
		};
	}

	/**
	 * 引数の式を計算して、関数に渡す引数にします。種類が合うことを先に確かめてください。
	 */
	private Argument toArgument(ParameterKind kind, Expr expr) {
		return switch (expr) {
			case Expr.TableReference table -> Objects.requireNonNull(_lookup.areaOf(table),
					"Lookup returned null: " + table.tableReference());
			case Expr.GroupReference group -> Objects.requireNonNull(_lookup.bagOf(group),
					"Lookup returned null: " + group.groupReference());
			// 範囲を受け付ける位置のセル1つの参照は、1行1列の範囲にする(Excel と同じく、SUM(A1) の A1 の論理値は数えない)
			case Expr.Reference reference when kind != ParameterKind.VALUE -> ValueArea.of(evaluate(reference));
			default -> new Argument.Single(evaluate(expr));
		};
	}

	/**
	 * 引数の中の最初のエラーを返します。範囲の中は行ごとの順に見ます(DESIGN.md 2.6)。エラーがなければ null を返します。
	 */
	private static Value firstError(Argument arg) {
		for (Value value : valuesOf(arg)) {
			if (value instanceof Value.Error) {
				return value;
			}
		}
		return null;
	}

	/**
	 * 引数の値を返します。テーブルの範囲は行ごとの順(左から右、上から下)、グループの順序は決まっていません。
	 */
	static Iterable<Value> valuesOf(Argument arg) {
		return switch (arg) {
			case Argument.Single(Value value) -> List.of(value);
			case Argument.Bag bag -> bag.values();
			case Argument.Area area -> {
				List<Value> values = new ArrayList<>(area.rows() * area.columns());
				for (int row = 1; row <= area.rows(); row++) {
					for (int column = 1; column <= area.columns(); column++) {
						values.add(area.get(row, column));
					}
				}
				yield values;
			}
		};
	}

	/**
	 * 遅延評価の関数に渡す、計算する前の引数を作ります。
	 */
	private LazyFunction.Arguments lazyArguments(List<Expr> exprs) {
		return new LazyFunction.Arguments() {
			@Override
			public int size() {
				return exprs.size();
			}

			@Override
			public Value evaluate(int index) {
				return Evaluator.this.evaluate(exprs.get(index));
			}
		};
	}

	/**
	 * 単項演算を計算します。
	 */
	private Value evaluateUnary(Expr.Unary unary) {
		Value operand = evaluate(unary.operand());
		if (operand instanceof Value.Error) {
			return operand;
		}
		return switch (unary.operatorType()) {
			// Excel の単項の + は何もしない(=+"a" は "a")
			case PLUS -> operand;
			case NEGATE -> {
				Value number = toNumeric(operand);
				if (!(number instanceof Value.Numeric(BigDecimal value))) {
					yield number;
				}
				yield new Value.Numeric(value.negate());
			}
			case PERCENT -> {
				Value number = toNumeric(operand);
				if (!(number instanceof Value.Numeric(BigDecimal value))) {
					yield number;
				}
				yield new Value.Numeric(value.movePointLeft(2));
			}
		};
	}

	/**
	 * 二項演算を計算します。<br>
	 * 左の被演算子がエラーなら、右の被演算子は計算せずに、左のエラーを返します。
	 * 両方がエラーでも、左のエラーを返すことになります(Excel と同じ)。
	 */
	private Value evaluateBinary(Expr.Binary binary) {
		Value left = evaluate(binary.leftOperand());
		if (left instanceof Value.Error) {
			return left;
		}
		Value right = evaluate(binary.rightOperand());
		if (right instanceof Value.Error) {
			return right;
		}
		return switch (binary.operatorType()) {
			case PLUS, MINUS, TIMES, DIVIDE, POWER -> arithmetic(binary.operatorType(), left, right);
			case CONCATENATE -> concatenate(left, right);
			case LESS_THAN, LESS_EQUAL, EQUALS, NOT_EQUALS, GREATER_EQUAL, GREATER_THAN ->
					comparison(binary.operatorType(), left, right);
		};
	}

	/**
	 * 算術演算を計算します。
	 */
	private Value arithmetic(Expr.Binary.BinaryOperatorType operatorType, Value left, Value right) {
		Value leftNumber = toNumeric(left);
		if (!(leftNumber instanceof Value.Numeric(BigDecimal l))) {
			return leftNumber;
		}
		Value rightNumber = toNumeric(right);
		if (!(rightNumber instanceof Value.Numeric(BigDecimal r))) {
			return rightNumber;
		}
		try {
			return switch (operatorType) {
				case PLUS -> new Value.Numeric(l.add(r, _mc));
				case MINUS -> new Value.Numeric(l.subtract(r, _mc));
				case TIMES -> new Value.Numeric(l.multiply(r, _mc));
				case DIVIDE -> r.signum() == 0 ? DIVIDE_BY_ZERO : new Value.Numeric(l.divide(r, _mc));
				case POWER -> power(l, r);
				default -> throw new AssertionError("Not an arithmetic operator: " + operatorType);
			};
		} catch (ArithmeticException e) {
			// MathContext の精度が0(無制限)で割り切れない場合や、指数が大きすぎる場合
			return INVALID_NUMBER;
		}
	}

	/**
	 * べき乗を計算します。<br>
	 * 指数が整数のときは BigDecimal のまま計算し、そうでないときは double で計算します。
	 */
	private Value power(BigDecimal base, BigDecimal exponent) {
		if (base.signum() == 0) {
			if (exponent.signum() == 0) {
				// Excel では 0^0 は #NUM!
				return INVALID_NUMBER;
			}
			if (exponent.signum() < 0) {
				// Excel では 0 の負の数乗は #DIV/0!
				return DIVIDE_BY_ZERO;
			}
		}
		if (isInteger(exponent)) {
			try {
				// BigDecimal.pow(int, MathContext) が受け付ける範囲は ±999999999
				int n = exponent.intValueExact();
				if (-999999999 <= n && n <= 999999999) {
					return new Value.Numeric(base.pow(n, _mc));
				}
			} catch (ArithmeticException e) {
				// int に収まらない指数は double で計算する
			}
		}
		double result = Math.pow(base.doubleValue(), exponent.doubleValue());
		if (Double.isNaN(result) || Double.isInfinite(result)) {
			// 負の数の小数乗(Excel でも #NUM!)や、大きすぎる結果
			return INVALID_NUMBER;
		}
		return new Value.Numeric(BigDecimal.valueOf(result).round(_mc));
	}

	private static boolean isInteger(BigDecimal value) {
		return value.signum() == 0 || value.stripTrailingZeros().scale() <= 0;
	}

	/**
	 * 文字列結合を計算します。
	 */
	private static Value concatenate(Value left, Value right) {
		Value leftText = toText(left);
		if (!(leftText instanceof Value.Text(String l))) {
			return leftText;
		}
		Value rightText = toText(right);
		if (!(rightText instanceof Value.Text(String r))) {
			return rightText;
		}
		return new Value.Text(l + r);
	}

	/**
	 * 比較演算を計算します。<br>
	 * Excel と同じく、種類の違う値は 数値 < 文字列 < 論理値 の順に比べます。
	 * 空は、相手の種類の 0、""、FALSE として比べます。文字列は大文字と小文字を区別しません。
	 */
	private static Value comparison(Expr.Binary.BinaryOperatorType operatorType, Value left, Value right) {
		// TODO: 日付の比較は、日付の型(DESIGN.md 4.1)を決めてから作る
		if (left instanceof Value.Date || right instanceof Value.Date) {
			return WRONG_VALUE;
		}
		int c = compare(left, right);
		boolean result = switch (operatorType) {
			case LESS_THAN -> c < 0;
			case LESS_EQUAL -> c <= 0;
			case EQUALS -> c == 0;
			case NOT_EQUALS -> c != 0;
			case GREATER_EQUAL -> c >= 0;
			case GREATER_THAN -> c > 0;
			default -> throw new AssertionError("Not a comparison operator: " + operatorType);
		};
		return new Value.Bool(result);
	}

	/**
	 * 2つの値を比べます。エラーと日付は渡されない前提です。
	 */
	private static int compare(Value left, Value right) {
		if (left instanceof Value.Empty && right instanceof Value.Empty) {
			return 0;
		}
		if (left instanceof Value.Empty) {
			left = emptyAs(right);
		}
		if (right instanceof Value.Empty) {
			right = emptyAs(left);
		}
		int leftRank = rank(left);
		int rightRank = rank(right);
		if (leftRank != rightRank) {
			return Integer.compare(leftRank, rightRank);
		}
		return switch (left) {
			case Value.Numeric(BigDecimal l) -> l.compareTo(((Value.Numeric) right).value());
			case Value.Text(String l) -> l.compareToIgnoreCase(((Value.Text) right).value());
			case Value.Bool(Boolean l) -> l.compareTo(((Value.Bool) right).value());
			default -> throw new AssertionError("Unexpected value: " + left);
		};
	}

	/**
	 * 比べる相手の種類に合わせて、空を置き換える値を返します。
	 */
	private static Value emptyAs(Value other) {
		return switch (other) {
			case Value.Numeric n -> ZERO;
			case Value.Text t -> new Value.Text("");
			case Value.Bool b -> new Value.Bool(false);
			default -> throw new AssertionError("Unexpected value: " + other);
		};
	}

	/**
	 * 種類の違う値を比べるときの順番です(数値 < 文字列 < 論理値)。
	 */
	private static int rank(Value value) {
		return switch (value) {
			case Value.Numeric n -> 0;
			case Value.Text t -> 1;
			case Value.Bool b -> 2;
			default -> throw new AssertionError("Unexpected value: " + value);
		};
	}

	/**
	 * 値を数値に変換します。変換できなければ Value.Error を返します。<br>
	 * 空は0、論理値は1か0、文字列は数値として読めれば数値にします(Excel と同じく ="1"+1 は 2)。
	 */
	static Value toNumeric(Value value) {
		return switch (value) {
			case Value.Numeric numeric -> numeric;
			case Value.Empty empty -> ZERO;
			case Value.Bool(Boolean b) -> b ? ONE : ZERO;
			case Value.Text(String text) -> parseNumber(text);
			// TODO: 日付の計算は、日付の型(DESIGN.md 4.1)を決めてから作る
			case Value.Date date -> WRONG_VALUE;
			case Value.Error error -> error;
		};
	}

	private static Value parseNumber(String text) {
		try {
			return new Value.Numeric(new BigDecimal(text.strip()));
		} catch (NumberFormatException e) {
			return WRONG_VALUE;
		}
	}

	/**
	 * 値を文字列に変換します。変換できなければ Value.Error を返します。<br>
	 * 数値は指数表記を使わず、末尾の0を付けません(1.50 は "1.5")。論理値は "TRUE" か "FALSE" にします。
	 */
	static Value toText(Value value) {
		return switch (value) {
			case Value.Text text -> text;
			case Value.Empty empty -> new Value.Text("");
			case Value.Numeric(BigDecimal number) -> new Value.Text(number.stripTrailingZeros().toPlainString());
			case Value.Bool(Boolean b) -> new Value.Text(b ? "TRUE" : "FALSE");
			// TODO: 日付の文字列は、日付の型(DESIGN.md 4.1)を決めてから作る
			case Value.Date date -> WRONG_VALUE;
			case Value.Error error -> error;
		};
	}


	/**
	 * 値を論理値に変換します。変換できなければ Value.Error を返します。<br>
	 * 空は FALSE、数値は0以外なら TRUE、文字列は "TRUE" か "FALSE"(大文字と小文字は区別しない)なら論理値にします(Excel の IF と同じ)。
	 */
	static Value toBool(Value value) {
		return switch (value) {
			case Value.Bool bool -> bool;
			case Value.Empty empty -> new Value.Bool(false);
			case Value.Numeric(BigDecimal number) -> new Value.Bool(number.signum() != 0);
			case Value.Text(String text) when text.equalsIgnoreCase("TRUE") -> new Value.Bool(true);
			case Value.Text(String text) when text.equalsIgnoreCase("FALSE") -> new Value.Bool(false);
			case Value.Text text -> WRONG_VALUE;
			// TODO: 日付は、日付の型(DESIGN.md 4.1)を決めてから作る
			case Value.Date date -> WRONG_VALUE;
			case Value.Error error -> error;
		};
	}

}
