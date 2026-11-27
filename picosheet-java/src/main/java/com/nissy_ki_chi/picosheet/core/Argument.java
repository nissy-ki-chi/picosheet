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
 * 関数(SheetFunction)に渡す引数です。値1つ、テーブルの範囲、グループのどれかです(DESIGN.md 2.6)。<br>
 * 範囲の引数は、関数を呼ぶ前にエンジンが作る、その時点の値の写しです。関数はセルに触れません。
 */
public sealed interface Argument {

	/**
	 * 値1つの引数です。式に直接書いた値や、計算した結果です。
	 */
	record Single(Value value) implements Argument {
		public Single {
			Objects.requireNonNull(value, "Value cannot be null");
		}
	}

	/**
	 * テーブルの範囲の引数です。行と列の形を持ちます。<br>
	 * 範囲を受け付ける位置に書いたセル1つの参照も、1行1列のこの引数になります。
	 */
	non-sealed interface Area extends Argument {

		/**
		 * 行の数を返します(1以上)。
		 */
		int rows();

		/**
		 * 列の数を返します(1以上)。
		 */
		int columns();

		/**
		 * 指定した位置の値を返します。位置は Range.getCell と同じく1から数えます。
		 * @throws IndexOutOfBoundsException 位置が範囲の外の場合
		 */
		Value get(int row, int column);
	}

	/**
	 * グループの引数です。形も順序も持ちません。<br>
	 * 値を順に見たときの順序は決まっていないので、位置に意味がある使い方はできません。
	 */
	non-sealed interface Bag extends Argument {

		/**
		 * 値の数を返します。
		 */
		int size();

		/**
		 * 値を返します。順序は決まっていません。
		 */
		Iterable<Value> values();
	}

}
