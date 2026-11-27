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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

/**
 * 関数を登録するときに指定する、引数の種類とエラーの扱いです(DESIGN.md 2.6)。<br>
 * 引数は、必ず書く引数、省略できる引数、繰り返せる引数の順に並びます。
 * <pre>
 * FunctionSpec.of(ParameterKind.VALUE)                                     // SQRT(x)
 * FunctionSpec.of(ParameterKind.AREA, ParameterKind.VALUE)
 *         .withOptional(ParameterKind.AREA)                                // SUMIF(range, criteria, [sum_range])
 * FunctionSpec.of(ParameterKind.ANY).withRepeated(ParameterKind.ANY)      // SUM(x, ...)
 * </pre>
 *
 * @param required 必ず書く引数の種類
 * @param optional 省略できる引数の種類。前から順に省略できない
 * @param repeated 省略できる引数のあとに、いくつでも書ける引数の種類。繰り返せなければ null
 * @param acceptsErrors 引数のエラーを受け取るか。false なら、引数(範囲の中も含む)にエラーがあると関数を呼ばず、最初のエラーを結果にする
 */
public record FunctionSpec(List<ParameterKind> required, List<ParameterKind> optional, ParameterKind repeated,
		boolean acceptsErrors) {

	public FunctionSpec {
		required = List.copyOf(Objects.requireNonNull(required, "Required parameters cannot be null"));
		optional = List.copyOf(Objects.requireNonNull(optional, "Optional parameters cannot be null"));
	}

	/**
	 * 必ず書く引数の種類を指定して作ります。省略できる引数と繰り返せる引数はなく、エラーは受け取りません。
	 */
	public static FunctionSpec of(ParameterKind... required) {
		return new FunctionSpec(Arrays.asList(required), List.of(), null, false);
	}

	/**
	 * 省略できる引数を足したものを返します。
	 */
	public FunctionSpec withOptional(ParameterKind... kinds) {
		List<ParameterKind> newOptional = new ArrayList<>(optional);
		newOptional.addAll(Arrays.asList(kinds));
		return new FunctionSpec(required, newOptional, repeated, acceptsErrors);
	}

	/**
	 * 最後にいくつでも書ける引数の種類を指定したものを返します。
	 */
	public FunctionSpec withRepeated(ParameterKind kind) {
		return new FunctionSpec(required, optional, Objects.requireNonNull(kind, "Kind cannot be null"), acceptsErrors);
	}

	/**
	 * 引数のエラーを受け取るようにしたものを返します。
	 */
	public FunctionSpec acceptingErrors() {
		return new FunctionSpec(required, optional, repeated, true);
	}

	/**
	 * 引数の数が合っているかを返します。
	 */
	boolean acceptsCount(int count) {
		if (count < required.size()) {
			return false;
		}
		return repeated != null || count <= required.size() + optional.size();
	}

	/**
	 * 指定した位置(0から数える)の引数の種類を返します。引数の数が合っていることを先に確かめてください。
	 */
	ParameterKind kindAt(int index) {
		if (index < required.size()) {
			return required.get(index);
		}
		if (index < required.size() + optional.size()) {
			return optional.get(index - required.size());
		}
		if (repeated == null) {
			throw new IndexOutOfBoundsException(index);
		}
		return repeated;
	}

}
