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

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * テーブルの一部または全部の範囲を表すオブジェクトです。<br>
 * オブジェクトの作成時に元とするテーブルオブジェクトの範囲を指定します。指定した範囲の左上のセル位置が
 * 新しく作成するこのオブジェクトの左上位置となります。<br>
 * このオブジェクトが示す範囲は元となるTableオブジェクトに対し固定的です。例えばこのオブジェクトが示す
 * 範囲にかかるようにTableオブジェクトから行を挿入した場合、このオブジェクトからアクセスできるセルは
 * 挿入された行を含み、それまで範囲の最下行にあった行は範囲から外れ、アクセスできなくなります。
 *
 * @author yusuke nishikawa
 *
 */
public class Range implements CellGroupReference {

	/**
	 * この範囲の元となるTableオブジェクト
	 */
	private Table _table;
	/**
	 * 行の開始位置の、Tableに対するオフセット
	 */
	private int _offsetRowFrom;
	/**
	 * 行の終了位置の、Tableに対するオフセット
	 */
	private int _offsetRowTo;
	/**
	 * 列の開始位置の、Tableに対するオフセット
	 */
	private int _offsetColFrom;
	/**
	 * 列の終了位置の、Tableに対するオフセット
	 */
	private int _offsetColTo;


	/**
	 * 指定されたテーブルの指定範囲を表すRangeオブジェクトを作ります。<br>
	 * @param table 元とするTableオブジェクト
	 * @param rowPosFrom tableの行の開始位置
	 * @param colPosFrom tableの列の開始位置
	 * @param rowPosTo tableの行の終了位置
	 * @param colPosTo tableの列の終了位置
	 */
	Range(Table table, int rowPosFrom, int colPosFrom, int rowPosTo, int colPosTo) {

		// tableはnullであってはならない
		if (table == null) {
			throw new NullPointerException("table is null.");
		}

		_table = table;

		// rowNumFrom, colNumFromは1以上でなければならず、 rowNumTo, colNumToはそれぞれのFrom以上でなければならない
		if ((rowPosFrom < 1) ||
				(colPosFrom < 1) ||
				(rowPosTo < rowPosFrom) ||
				(colPosTo < colPosFrom) ) {
			throw new IllegalArgumentException("ingalid row/col parameter." +
					" rowNumFrom=" + Integer.toString(rowPosFrom) +
					" colNumFrom=" + Integer.toString(colPosFrom) +
					" rowNumTo=" + Integer.toString(rowPosTo) +
					" colNumTo=" + Integer.toString(colPosTo));
		}

		_offsetRowFrom = rowPosFrom;
		_offsetColFrom = colPosFrom;
		_offsetRowTo = rowPosTo;
		_offsetColTo = colPosTo;

	}


	/**
	 * 指定した位置のセルを返します。
	 * @param rowPos このレンジに対する行位置
	 * @param colPos このレンジに対する列位置
	 * @return 指定した位置のセルオブジェクト
	 */
	public Cell getCell(int rowPos, int colPos) {
		int rowIndex = rowPos - 1;
		int colIndex = colPos - 1;
		return _table.getCell(_offsetRowFrom + rowIndex, _offsetColFrom + colIndex);
	}


	/**
	 * 指定した行を表す配列を返します。
	 * @param rowPos このレンジに対する行位置
	 * @return 指定した行のセルオブジェクト配列
	 */
	public Cell[] getRowArray(int rowPos) {
		int rowIndex = rowPos - 1;
		Cell[] rowCells = new Cell[_offsetColTo - _offsetColFrom + 1];
		for (int index = 0; index < rowCells.length; index++) {
			rowCells[index] = _table.getCell(_offsetRowFrom + rowIndex, _offsetColFrom + index);
		}
		return rowCells;
	}


	/**
	 * 指定した列を表す配列を返します。
	 * @param colPos このレンジに対する列位置
	 * @return 指定した列のセルオブジェクト配列
	 */
	public Cell[] getColArray(int colPos) {
		Cell[] colCells = new Cell[_offsetRowTo - _offsetRowFrom + 1];
		for (int index = 0; index < colCells.length; index++) {
			colCells[index] = _table.getCell(_offsetRowFrom + index, _offsetColFrom + colPos);
		}
		return colCells;
	}


	/* (非 Javadoc)
	 * @see com.nissy_ki_chi.picosheet.core.CellGroupReference#getCells()
	 */
	public Collection<Cell> getCells() {
		// Tableには同一セルが複数含まれている場合があるため、SetでなくListを使う必要がある。
		int rowSize = _offsetRowTo - (_offsetRowFrom - 1);
		int colSize = _offsetColTo - (_offsetColFrom - 1);
		List<Cell> cells = new ArrayList<Cell>(rowSize * colSize);
		for (int rowPos = _offsetRowFrom; rowPos <= (_offsetRowFrom - 1) + rowSize; rowPos++) {
			for (int colPos = _offsetColFrom; colPos <= (_offsetColFrom - 1) + colSize; colPos++) {
				cells.add(_table.getCell(rowPos, colPos));
			}
		}
		return cells;
	}

	/**
	 * このレンジの行数を返します。
	 * @return このレンジの行数
	 */
	public int getRowSize() {
		return _offsetRowTo - _offsetRowFrom + 1;
	}

	/**
	 * このレンジの列数を返します。
	 * @return このレンジの列数
	 */
	public int getColSize() {
		return _offsetColTo - _offsetColFrom + 1;
	}

}
