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

import java.util.Objects;

/**
 * セルの式を表す不変(immutable)のオブジェクトです。<br>
 * 利用者が書いた式の文字列と、それを解析した結果を組にして持ちます。
 * 読めた式は Parsed、読めなかった式は Invalid になります。<br>
 * 式を表示・保存するときは source() を使い、Expr から作り直しません。<br>
 * FormulaParser.makeFormula() で作ります。
 *
 * @author yusuke nishikawa
 *
 */
sealed interface Formula {

	/**
	 * 利用者が書いた式の文字列を、そのまま返します。
	 * @return 式の文字列(先頭の = は含まない)
	 */
	String source();

	/**
	 * 解析できた式
	 * @param source 利用者が書いた式の文字列
	 * @param expr 式の構文木の根
	 */
	record Parsed(String source, Expr expr) implements Formula {
		public Parsed {
			Objects.requireNonNull(source, "Source cannot be null");
			Objects.requireNonNull(expr, "Expr cannot be null");
		}
	}

	/**
	 * 解析できなかった式
	 * @param source 利用者が書いた式の文字列
	 * @param position 誤りのある、元の式の中の位置(0始まり)。分からない場合は-1
	 * @param message 誤りの説明
	 */
	record Invalid(String source, int position, String message) implements Formula {
		public Invalid {
			Objects.requireNonNull(source, "Source cannot be null");
		}
	}

}
