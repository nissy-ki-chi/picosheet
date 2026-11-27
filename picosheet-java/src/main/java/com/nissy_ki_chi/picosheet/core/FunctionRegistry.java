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

import java.util.Collections;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.SortedSet;
import java.util.TreeSet;

/**
 * 関数の一覧です(DESIGN.md 2.6、#34)。<br>
 * 関数名は大文字と小文字を区別しません(#6)。関数名は計算するときに探すので、あとから登録しても再計算すれば使えます。<br>
 * このクラスはスレッドセーフではありません。計算している間は、登録や削除をしないでください。
 */
public final class FunctionRegistry {

	/**
	 * 一覧に入れるものです。ふつうの関数か、遅延評価の関数(組み込みだけ)です。
	 */
	sealed interface Entry {
	}

	record Eager(FunctionSpec spec, SheetFunction function) implements Entry {
	}

	record Lazy(LazyFunction function) implements Entry {
	}

	private final Map<String, Entry> _functions = new HashMap<>();

	/**
	 * 空の一覧を作ります。
	 */
	public FunctionRegistry() {
	}

	/**
	 * 組み込みの関数を登録した一覧を作ります。
	 */
	public static FunctionRegistry withBuiltins() {
		FunctionRegistry registry = new FunctionRegistry();
		BuiltinFunctions.registerTo(registry);
		return registry;
	}

	/**
	 * 関数を登録します。同じ名前の関数があれば置き換えます。
	 * @param name 関数名
	 * @param spec 引数の種類とエラーの扱い
	 * @param function 関数
	 * @return この一覧
	 */
	public FunctionRegistry register(String name, FunctionSpec spec, SheetFunction function) {
		Objects.requireNonNull(spec, "Spec cannot be null");
		Objects.requireNonNull(function, "Function cannot be null");
		_functions.put(normalize(name), new Eager(spec, function));
		return this;
	}

	/**
	 * 遅延評価の関数を登録します。同じ名前の関数があれば置き換えます。
	 */
	FunctionRegistry registerLazy(String name, LazyFunction function) {
		Objects.requireNonNull(function, "Function cannot be null");
		_functions.put(normalize(name), new Lazy(function));
		return this;
	}

	/**
	 * 関数を削除します。登録されていなければ何もしません。
	 * @return この一覧
	 */
	public FunctionRegistry remove(String name) {
		_functions.remove(normalize(name));
		return this;
	}

	/**
	 * 関数が登録されているかを返します。
	 */
	public boolean contains(String name) {
		return _functions.containsKey(normalize(name));
	}

	/**
	 * 登録されている関数名を、大文字にして名前の順で返します。
	 */
	public SortedSet<String> names() {
		return Collections.unmodifiableSortedSet(new TreeSet<>(_functions.keySet()));
	}

	/**
	 * 関数を探します。登録されていなければ null を返します。
	 */
	Entry find(String name) {
		return _functions.get(normalize(name));
	}

	private static String normalize(String name) {
		Objects.requireNonNull(name, "Function name cannot be null");
		if (name.isEmpty()) {
			throw new IllegalArgumentException("Function name cannot be empty");
		}
		return name.toUpperCase(Locale.ROOT);
	}

}
