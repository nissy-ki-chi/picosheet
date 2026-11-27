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
 * 引数を必要な分だけ計算する関数(IF など)です。組み込みの関数だけに使い、公開しません(DESIGN.md 2.6)。<br>
 * 引数の数とエラーは、関数が自分で確かめます。使わない引数は計算しないので、その先の参照は読みません(DESIGN.md 3.8)。
 */
@FunctionalInterface
interface LazyFunction {

	Value apply(Arguments args);

	/**
	 * 計算する前の引数です。
	 */
	interface Arguments {

		/**
		 * 引数の数を返します。
		 */
		int size();

		/**
		 * 指定した位置(0から数える)の引数を計算します。範囲の参照は、値1つにならないので #VALUE! にします。
		 */
		Value evaluate(int index);
	}

}
