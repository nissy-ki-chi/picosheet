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

import java.math.BigDecimal;
import java.util.Objects;

/**
 * セルの値を表す不変(immutable)のオブジェクトです。<br>
 * CellクラスからgetValue()で値を取得した際に渡されるのもこのクラスのオブジェクトです。
 * @author yusuke nishikawa
 *
 */

public sealed interface Value {

	/**
	 * 空を表します。
	 */
	record Empty() implements Value {
	}

	/**
	 * 数値を表します。<code>getNumber()<code>でBigDecimal型のオブジェクトを得ることができます。
	 */
	record Numeric(BigDecimal value) implements Value {
		public Numeric {
			Objects.requireNonNull(value, "Numeric value cannot be null");
		}
	}

	/**
	 * 文字列を表します。<code>getString()<code>でString型のオブジェクトを得ることができます。
	 */
	record Text(String value) implements Value {
		public Text {
			Objects.requireNonNull(value, "Text value cannot be null");
		}
	}

	/**
	 * 論理値を表します。
	 */
	record Bool(Boolean value) implements Value {
		public Bool {
			Objects.requireNonNull(value, "Bool value cannot be null");
		}
	}

	record Date(java.util.Date value) implements Value {
		public Date {
			Objects.requireNonNull(value, "Date value cannot be null");
		}
	}

	// TODO: エラー型は未実装
	// record Error(ErrorType value) implements Value {}
}

