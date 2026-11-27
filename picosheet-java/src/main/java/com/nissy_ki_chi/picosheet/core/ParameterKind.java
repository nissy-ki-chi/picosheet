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
 * 関数の引数の位置ごとに、受け付ける引数の種類です(DESIGN.md 2.6)。<br>
 * 範囲を受け付ける種類(AREA、RANGE、ANY)の位置にセル1つの参照を書いた場合は、1行1列の Argument.Area になります。
 */
public enum ParameterKind {

	/**
	 * 値1つ(Argument.Single)。SQRT の引数、SUMIF の criteria など
	 */
	VALUE,

	/**
	 * テーブルの範囲(Argument.Area)。SUMIF の range、VLOOKUP の表など
	 */
	AREA,

	/**
	 * テーブルの範囲かグループ(Argument.Area か Argument.Bag)。COUNTIF の range など
	 */
	RANGE,

	/**
	 * 何でも(Argument.Single、Argument.Area、Argument.Bag)。SUM、COUNT の引数など
	 */
	ANY;

	/**
	 * 値1つの引数を受け付けるかを返します。
	 */
	boolean acceptsSingle() {
		return this == VALUE || this == ANY;
	}

	/**
	 * グループの引数を受け付けるかを返します。
	 */
	boolean acceptsBag() {
		return this == RANGE || this == ANY;
	}

}
