/**
 * JPicosheet: Spreadsheet engine for Java Applications
 * Copyright (C) 2011 yusuke nishikawa
 * 
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 * 
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
 */
/**
 *
 */
package com.nissy_ki_chi.jpicosheet.core;

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
