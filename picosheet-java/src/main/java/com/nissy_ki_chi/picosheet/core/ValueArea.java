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
 * 値の参照を行ごとの順(左から右、上から下)に写して持つ、テーブルの範囲の引数です(DESIGN.md 2.6)。
 *
 * @param values 行ごとの順に並べた値。数は rows * columns
 */
record ValueArea(int rows, int columns, List<Value> values) implements Argument.Area {

	ValueArea {
		if (rows < 1 || columns < 1) {
			throw new IllegalArgumentException("Invalid size: rows=" + rows + " columns=" + columns);
		}
		values = List.copyOf(Objects.requireNonNull(values, "Values cannot be null"));
		if (values.size() != rows * columns) {
			throw new IllegalArgumentException("Values size " + values.size() + " does not match " + rows + "x" + columns);
		}
	}

	/**
	 * 値1つを1行1列の範囲にします。
	 */
	static ValueArea of(Value value) {
		return new ValueArea(1, 1, List.of(value));
	}

	@Override
	public Value get(int row, int column) {
		if (row < 1 || rows < row || column < 1 || columns < column) {
			throw new IndexOutOfBoundsException("row=" + row + " column=" + column + " in " + rows + "x" + columns);
		}
		return values.get((row - 1) * columns + (column - 1));
	}

}
