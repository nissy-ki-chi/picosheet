package com.nissy_ki_chi.jpicosheet.core;

import java.util.Arrays;

/**
 * Tableオブジェクトの1つの行を構成するオブジェクトです。
 * @author Yusuke
 *
 */
public class Row {

	/**
	 * カラムの数。<br>
	 * 配列のインデックスは0から始まるが、Tableのアドレスは1から始まる点に注意。
	 */
	private int _colSize = 0;
	/**
	 * カラムを保持する配列
	 */
	private Cell[] _cols;
	/**
	 * この行オブジェクトを持っているRowsChunkオブジェクトへの参照
	 */
	private RowsChunk _chunk;
	
	/**
	 * このオブジェクトが属すRowsChunkオブジェクトと、このオブジェクトのカラムサイズを指定してオブジェクトを初期化します。
	 * @param belongTo このオブジェクトが属すRowsChunkオブジェクト
	 * @param colSize カラムサイズ
	 */
	Row(RowsChunk belongTo, int colSize) {
		if (belongTo == null) throw new NullPointerException("chunkがnullです。");
		if (colSize < 0) throw new IllegalArgumentException("colSizeは1以上でなければなりません。");
		_colSize = colSize;
		_chunk = belongTo;
		_cols = new Cell[_colSize];
		for (int i = 0; i < _colSize; i++) {
			_cols[i] = new TableCell(this);
		}
	}
	
	/**
	 * このオブジェクトが保持しているカラム配列のコピーを返します。
	 * @return カラム配列のコピー
	 */
	Cell[] getCols() {
		return Arrays.copyOf(_cols, _colSize);
	}
	
	/**
	 * 指定したカラム位置のセルを返します。カラムの位置は1から始まる点に注意してください。
	 * @param colPos カラム位置
	 * @return セルオブジェクト
	 */
	Cell getCellByPosition(int colPos) {
		return _cols[colPos-1];
	}
	
	/**
	 * 指定したインデックス番号のセルを返します。インデックス番号は0から始まります。
	 * @param colIndex カラムのインデックス番号
	 * @return セルオブジェクト
	 */
	Cell getCellByIndex(int colIndex) {
		return _cols[colIndex];
	}
	
	/**
	 * このオブジェクトが属しているRowsChunkオブジェクトを返します。
	 * @return このオブジェクトの属すRowsChunkオブジェクト
	 */
	RowsChunk getChunk() {
		return _chunk;
	}
	
	/**
	 * このオブジェクトのカラム数を返します。
	 * @return カラム数
	 */
	int getColSize() {
		return _colSize;
	}
	
	/**
	 * このオブジェクトの指定したカラム位置に、指定した数のカラムを挿入します。
	 * @param position カラムを挿入する位置
	 * @param size 挿入するカラム数
	 */
	void insertColumn(int position, int size) {
		// カラム位置から配列インデックスへの変換
		int index = position -1;
		Cell[] newColumns = new Cell[_colSize + size];
		_colSize = newColumns.length;
		for (int i = 0; i < index; i++) {
			newColumns[i] = _cols[i]; 
		}
		for (int i = index; i < index + size; i++) {
			newColumns[i] = new TableCell(this); 
		}
		int copyFromPos = index;
		for (int i = index + size; i < newColumns.length; i++) {
			newColumns[i]= _cols[copyFromPos];
			copyFromPos++;
		}
		_cols = newColumns;
	}
	
	/**
	 * このオブジェクトの末尾に、指定した数のカラムを追加します。
	 * @param size 追加するカラム数
	 */
	void addColumn(int size) {
		Cell[] newColumns = new Cell[_colSize + size];
		int oldColSize = _colSize;
		_colSize = newColumns.length;
		for (int i = 0; i < oldColSize; i++) {
			newColumns[i] = _cols[i]; 
		}
		for (int i = oldColSize; i < _colSize - 1; i++) {
			newColumns[i] = new TableCell(this); 
		}
		_cols = newColumns;
	}
}
