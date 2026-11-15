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
/**
 *
 */
package com.nissy_ki_chi.picosheet.core;

/**
 * @author yusuke nishikawa
 * 計算式にエラーがある場合にスローされます。
 */
public class FormulaException extends PicosheetException {

	/**
	 *
	 */
	private static final long serialVersionUID = -5237961644571049021L;

	/**
	 * オブジェクトを初期化します
	 */
	public FormulaException() {
		super();
	}

	/**
	 * @param message
	 * @param cause
	 */
	public FormulaException(String message, Throwable cause) {
		super(message, cause);
	}

	/**
	 * @param message
	 */
	public FormulaException(String message) {
		super(message);
	}

	/**
	 * @param cause
	 */
	public FormulaException(Throwable cause) {
		super(cause);
	}

}
