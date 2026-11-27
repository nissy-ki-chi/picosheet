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
 * 値の参照を写して持つ、グループの引数です(DESIGN.md 2.6)。並びに意味はありません。
 */
record ValueBag(List<Value> values) implements Argument.Bag {

	ValueBag {
		values = List.copyOf(Objects.requireNonNull(values, "Values cannot be null"));
	}

	@Override
	public int size() {
		return values.size();
	}

}
