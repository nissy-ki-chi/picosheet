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

/**
 * 式の中で使う関数です(DESIGN.md 2.6)。FunctionRegistry に、FunctionSpec と組にして登録します。<br>
 * 関数は状態を持たず、副作用を持たないように作ってください。
 */
@FunctionalInterface
public interface SheetFunction {

	/**
	 * 関数を計算します。
	 * <ul>
	 * <li>引数の数と種類は、登録したときの FunctionSpec に合っています。合わない場合、関数は呼ばれません。</li>
	 * <li>FunctionSpec でエラーを受け取ると指定しなければ、引数(範囲の中も含む)にエラーはありません。</li>
	 * </ul>
	 * @param args 計算済みの引数。変更できません
	 * @param context 計算の設定
	 * @return 計算した結果。null は返さないでください
	 */
	Value apply(List<Argument> args, FunctionContext context);

}
