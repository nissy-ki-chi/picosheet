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

/**
 * 計算式を分解した各要素を表す不変(immutable)のオブジェクトです。<br>
 * 
 * @author yusuke nishikawa
 *
 */

public sealed interface Token {
	int position();

	record Operator(Symbol symbol, int position) implements Token {
		/**
		 * 式の中の記号の種類を表します。
		 */
		public enum Symbol {

			PLUS("+"), MINUS("-"), TIMES("*"), DIVIDE("/"),
			POWER("^"), PERCENT("%"),
			CONCATENATE("&"),
			LESS_THAN("<"), LESS_EQUAL("<="),
			EQUALS("="), NOT_EQUALS("<>"),
			GREATER_EQUAL(">="), GREATER_THAN(">"),
			COMMA(","), LEFT_PARENTHESIS("("), RIGHT_PARENTHESIS(")");

			private final String text;

			Symbol(String text) {
				this.text = text;
			}

			public String text() {
				return text;
			}
		}

	}

	record Function(String functionName, int position) implements Token {
	}

	record Reference(String reference, int position) implements Token {
	}

	record GroupReference(String groupReference, int position) implements Token {
	}

	record TableReference(String tableReference, int position) implements Token {
	}

	record Literal(Value value, int position) implements Token {
	}

}

