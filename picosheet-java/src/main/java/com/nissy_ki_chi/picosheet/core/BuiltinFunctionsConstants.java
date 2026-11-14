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
package com.nissy_ki_chi.picosheet.core;

public final class BuiltinFunctionsConstants {

	/**
	 * 組み込み関数の定義
	 */
	public static final String[][] BUILTIN_FUNCTIONS = {
		{"AVERAGE", "com.nissy_ki_chi.picosheet.function.AVERAGE"},
		{"ABS", "com.nissy_ki_chi.picosheet.function.ABS"},
		{"SUM", "com.nissy_ki_chi.picosheet.function.SUM"},
		{"MOD", "com.nissy_ki_chi.picosheet.function.MOD"},
		{"INT", "com.nissy_ki_chi.picosheet.function.INT"},
		{"IF", "com.nissy_ki_chi.picosheet.function.IF"},
		{"AND", "com.nissy_ki_chi.picosheet.function.AND"},
		{"OR", "com.nissy_ki_chi.picosheet.function.OR"},
		{"NOT", "com.nissy_ki_chi.picosheet.function.NOT"},
		{"SQRT", "com.nissy_ki_chi.picosheet.function.SQRT"},
		{"SIN", "com.nissy_ki_chi.picosheet.function.SIN"},
		{"COS", "com.nissy_ki_chi.picosheet.function.COS"},
		{"TAN", "com.nissy_ki_chi.picosheet.function.TAN"},
		{"PI", "com.nissy_ki_chi.picosheet.function.PI"},
		{"RADIANS", "com.nissy_ki_chi.picosheet.function.RADIANS"},
		{"DEGREES", "com.nissy_ki_chi.picosheet.function.DEGREES"},
	};


}
