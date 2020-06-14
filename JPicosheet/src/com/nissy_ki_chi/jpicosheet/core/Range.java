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
		Cell[] rows = new Cell[_offsetRowTo - _offsetRowFrom + 1];
		for (int index = 0; index < rows.length; index++) {
			rows[index] = _table.getCell(_offsetRowFrom + rowIndex, _offsetColFrom + index);
		}
		return rows;
	}


	/**
	 * 指定した列を表す配列を返します。
	 * @param colPos このレンジに対する列位置
	 * @return 指定した列のセルオブジェクト配列
	 */
	public Cell[] getColArray(int colPos) {
		Cell[] cols = new Cell[_offsetColTo - _offsetColFrom + 1];
		for (int index = 0; index < cols.length; index++) {
			cols[index] = _table.getCell(_offsetRowFrom + index, _offsetColFrom + colPos);
		}
		return cols;
	}


	/* (非 Javadoc)
	 * @see com.nissy_ki_chi.jpicosheet.core.CellGroupReference#getCells()
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
