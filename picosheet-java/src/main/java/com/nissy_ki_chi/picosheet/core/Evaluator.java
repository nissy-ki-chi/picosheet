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
	 * 参照の名前を解決し、参照先のセルの値を返します。参照先が見つからない場合は、
	 * Value.Error(INVALID_REFERENCE) を返します。
	 */
	@FunctionalInterface
	interface ReferenceLookup {
		Value valueOf(Expr.Reference reference);
	}

	private static final Value.Numeric ZERO = new Value.Numeric(BigDecimal.ZERO);
	private static final Value.Numeric ONE = new Value.Numeric(BigDecimal.ONE);
	private static final Value.Error WRONG_VALUE = new Value.Error(ErrorKind.WRONG_VALUE);
	private static final Value.Error DIVIDE_BY_ZERO = new Value.Error(ErrorKind.DIVIDE_BY_ZERO);
	private static final Value.Error INVALID_NUMBER = new Value.Error(ErrorKind.INVALID_NUMBER);

	private final MathContext _mc;
	private final ReferenceLookup _lookup;

	/**
	 * @param mc 計算に使う MathContext
	 * @param lookup 参照から値を得る口
	 */
	Evaluator(MathContext mc, ReferenceLookup lookup) {
		this._mc = Objects.requireNonNull(mc, "MathContext cannot be null");
		this._lookup = Objects.requireNonNull(lookup, "Lookup cannot be null");
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
			// TODO: 関数と範囲の参照は、関数の API(DESIGN.md 4.3)を決めてから作る
			case Expr.Function function -> throw new UnsupportedOperationException(
					"Function is not supported yet: " + function.functionName());
			case Expr.GroupReference group -> throw new UnsupportedOperationException(
					"Group reference is not supported yet: " + group.groupReference());
			case Expr.TableReference table -> throw new UnsupportedOperationException(
					"Table reference is not supported yet: " + table.tableReference());
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
	private static Value toNumeric(Value value) {
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
	private static Value toText(Value value) {
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

}
