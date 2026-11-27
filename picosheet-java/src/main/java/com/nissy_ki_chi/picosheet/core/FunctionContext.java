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

import java.math.MathContext;

/**
 * 関数(SheetFunction)に渡す、計算の設定です。<br>
 * 渡すものが増えても関数の形を変えずに済むように、引数とは別にまとめています(DESIGN.md 2.6)。
 */
public interface FunctionContext {

	/**
	 * 数値を計算するときの精度と丸め方を返します。
	 */
	MathContext mathContext();

}
