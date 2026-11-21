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
	 * 誤りのある、元の式の中の位置(0始まり)。分からない場合は-1。
	 */
	private final int _position;

	/**
	 * オブジェクトを初期化します
	 */
	public FormulaException() {
		super();
		this._position = -1;
	}

	/**
	 * 誤りのある位置を指定して、オブジェクトを初期化します
	 * @param message メッセージ
	 * @param position 誤りのある、元の式の中の位置(0始まり)
	 */
	public FormulaException(String message, int position) {
		super(message);
		this._position = position;
	}

	/**
	 * 誤りのある、元の式の中の位置を返します。
	 * @return 誤りのある位置(0始まり)。分からない場合は-1
	 */
	public int getPosition() {
		return _position;
	}

	/**
	 * @param message
	 * @param cause
	 */
	public FormulaException(String message, Throwable cause) {
		super(message, cause);
		this._position = -1;
	}

	/**
	 * @param message
	 */
	public FormulaException(String message) {
		super(message);
		this._position = -1;
	}

	/**
	 * @param cause
	 */
	public FormulaException(Throwable cause) {
		super(cause);
		this._position = -1;
	}

}
