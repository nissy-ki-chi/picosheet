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

import java.util.List;
import java.util.Objects;

/**
 * 抽象構文木を表す不変(immutable)のオブジェクトです。<br>
 * 
 * @author yusuke nishikawa
 *
 */

public sealed interface Expr {

	record Unary(UnaryOperatorType operatorType, Expr operand) implements Expr {
		public Unary {
			Objects.requireNonNull(operatorType, "Operator type cannot be null");
			Objects.requireNonNull(operand, "Operand cannot be null");
		}

		public enum UnaryOperatorType {

			// 優先度6－単項演算子
			PLUS(6),
			NEGATE(6),
			
			// 優先度5－パーセント
			PERCENT(5);

			private final int _priority;

			private UnaryOperatorType(int priority) {
				this._priority = priority;
			}

			public int priority() {
				return _priority;
			}

		}

	}

	record Binary(BinaryOperatorType operatorType, Expr leftOperand, Expr rightOperand) implements Expr {
		public Binary {
			Objects.requireNonNull(operatorType, "Operator type cannot be null");
			Objects.requireNonNull(leftOperand, "Left operand cannot be null");
			Objects.requireNonNull(rightOperand, "Right operand cannot be null");
		}

		public enum BinaryOperatorType {

			// 優先度4－べき乗
			/**
			 * べき乗
			 */
			POWER(4),

			// 優先度3－乗除算
			/**
			 * 乗算
			 */
			TIMES(3),
			/**
			 * 除算
			 */
			DIVIDE(3),

			// 優先度2－加減算
			/**
			 * 加算
			 */
			PLUS(2),
			/**
			 * 減算
			 */
			MINUS(2),

			// 優先度1－文字列結合演算子
			/**
			 * 文字列結合
			 */
			CONCATENATE(1),

			// 優先度0－比較演算子
			/**
			 * 未満
			 */
			LESS_THAN(0),
			/**
			 * 以下
			 */
			LESS_EQUAL(0),
			/**
			 * イコール
			 */
			EQUALS(0),
			/**
			 * ノットイコール
			 */
			NOT_EQUALS(0),
			/**
			 * 以上
			 */
			GREATER_EQUAL(0),
			/**
			 * より大きい
			 */
			GREATER_THAN(0);

			private final int _priority;

			private BinaryOperatorType(int priority) {
				this._priority = priority;
			}

			public int priority() {
				return _priority;
			}

		}

	}

	record Function(String functionName, List<Expr> arguments) implements Expr {
		public Function {
			Objects.requireNonNull(functionName, "Function name cannot be null");
			Objects.requireNonNull(arguments, "Arguments list cannot be null");
			arguments = List.copyOf(arguments); // Make a defensive copy to ensure immutability
		}
	}

	record Reference(String reference) implements Expr {
	}

	record GroupReference(String groupReference) implements Expr {
	}

	record TableReference(String tableReference) implements Expr {
	}

	record Literal(Value value) implements Expr {
	}

}
