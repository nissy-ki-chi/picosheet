package com.nissy_ki_chi.jpicosheet.core;

public class RowsChunk {

	final int ROW_SIZE = 20;
	final Row[] _rows = new Row[ROW_SIZE];
	
	private int _beginRowPos = 1;
	private int _filledCount = 0;
	private Table _table;
	private RowsChunk _before = null;
	private RowsChunk _after = null;
	
	
	/**
	 * 新しいオブジェクトを作成します。
	 * @param table このオブジェクトを持つTableオブジェクト
	 * @param beginRowPos このオブジェクトがTableの何行目からの行を持っているかを示す番号
	 * @param beforeChunk このオブジェクトの前のRowsChunkオブジェクト
	 */
	RowsChunk(Table table, int beginRowPos, RowsChunk beforeChunk) {
		if (table == null) throw new NullPointerException("tableがnullです。");
		if (beginRowPos < 1) throw new IllegalArgumentException("beginRowNumは1以上でなければなりません。");
		_table = table;
		_beginRowPos = beginRowPos;
		_before = beforeChunk;
		if (_before != null) _before.setAfter(this);
	}
	
	/**
	 * このオブジェクトの前のRowsChunkオブジェクトから、、そのオブジェクトの指定インデックス番号以降の行データをコピーして新しいオブジェクトを作成します。
	 * @param table このオブジェクトを持つTableオブジェクト
	 * @param beginRowPos このオブジェクトがTableの何行目からの行を持っているかを示す番号
	 * @param beforeChunk このオブジェクトの前のRowsChunkオブジェクト
	 * @param copyFromIndex このオブジェクトの前のRowsChunkオブジェクトの、何番目の行からコピーするかを示すインデックス。
	 */
	RowsChunk(Table table, int beginRowPos, RowsChunk beforeChunk, int copyFromIndex) {
		if (table == null) throw new NullPointerException("tableがnullです。");
		if (beginRowPos < 1) throw new IllegalArgumentException("beginRowNumは1以上でなければなりません。");
		if (beforeChunk == null) throw new NullPointerException("beforeChunkがnullです。");
		if (copyFromIndex < 0) throw new IllegalArgumentException("copyFromIndexは0以上でなければなりません。");
		_table = table;
		_beginRowPos = beforeChunk.getBeginRowNum() + copyFromIndex;
		_before = beforeChunk;
		_after = beforeChunk.getAfter();
		_before.setAfter(this);
		for (int i = copyFromIndex; (_before._rows[i] != null) && (i < ROW_SIZE); i++) {
			this._rows[_filledCount] = _before._rows[i];
			_filledCount++;
		}
		
	}
	
	/**
	 * このオブジェクトに続くRowsChunkオブジェクトを指定します。
	 * @param after
	 */
	void setAfter(RowsChunk after) {
		this._after = after;
	}
	
	/**
	 * このオブジェクトの次のRowsChunkオブジェクトを返します。
	 */
	RowsChunk getAfter() {
		return this._after;
	}
	
	/**
	 * 指定されたRowオブジェクトが、このRowsChunkのチェーン全体の何行目か(=テーブルの何行目か)を返します。
	 * @param row 調査対象のRowオブジェクト。このRowsChunkに含まれるオブジェクトでなければならない。
	 * @return テーブルの行番号
	 */
	int getRowNumber(Row row) {
		for (int index = 0; index < ROW_SIZE; index++) {
			// nullを検索してしまう前に見つかるはず
			assert (_rows[index] != null);
			if (_rows[index] == row) {
				return index + _beginRowPos;
			}
		}
		return -1;
	}
	
	/**
	 * このテーブルの末尾に、addRowCountで指定した行数を追加します。
	 * @param addRowCount 追加する行数。
	 */
	/**
	 * このテーブルの末尾に、addRowCountで指定した行数を追加します。
	 * @param addRowCount 追加する行数。
	 * @return チェーン末尾のRowsChunkオブジェクト
	 */
	RowsChunk addRow(int addRowCount) {
		if (addRowCount < 1) throw new IllegalArgumentException("追加する行数は1以上でなければなりません。");
		
		int remainingRows = ROW_SIZE - _filledCount;
		if (addRowCount <= remainingRows) {
			// このRowsChunkの残りの空き行数が足りている
			addRowToThis(addRowCount);
			return this;
		} else {
			// 空き行数が足りない。新しいRowsChunkを追加する
			addRowToThis(remainingRows);
			int moreNeed = addRowCount - remainingRows;
			RowsChunk newLastChunk;
			int newChunkCnt = moreNeed / ROW_SIZE + 1;
			int lastChunkRows = moreNeed % ROW_SIZE;
			newLastChunk = addFullFilledChunk(newChunkCnt);
			if (lastChunkRows > 0) {
				newLastChunk = addHalfFilledChunk(newLastChunk, lastChunkRows);
			}
			return newLastChunk;
		}
	}
	
	private void addRowToThis(int addRowCount) {
		for (int i = _filledCount; i < addRowCount; i++) {
			_rows[i] = new Row(this, this._table.getColSize());
			_filledCount++;
		}
	}
	
	private RowsChunk addFullFilledChunk(int chunkCount) {
		RowsChunk beforeChunk = this;
		int beginRowNum = this._beginRowPos + this._filledCount;
		RowsChunk lastChunk = this;
		for (int i = 0; i < chunkCount; i++) {
			RowsChunk newChunk = new RowsChunk(this._table, beginRowNum + (ROW_SIZE * i), beforeChunk);
			newChunk.addRowToThis(ROW_SIZE);
			beforeChunk.setAfter(newChunk);
			lastChunk = newChunk;
		}
		return lastChunk;
	}
	
	private RowsChunk addHalfFilledChunk(RowsChunk lastChunk, int addRowCount) {
		// 0行追加なら何もしない
		if (addRowCount == 0) return this;

		RowsChunk beforeChunk = lastChunk;
		RowsChunk newLastChunk = new RowsChunk(this._table, lastChunk.getLastRowNum() + 1, beforeChunk);
		newLastChunk.addRowToThis(addRowCount);
		return newLastChunk;
	}
	
	
	
	/**
	 * このオブジェクトの最初のRowが、Tableの何行目であるかを指定します。
	 * @param newRownum
	 */
	void setBeginRowNum(int newRownum) {
		_beginRowPos = newRownum;
	}
	
	/**
	 * このオブジェクトの最初のRowが、Tableの何行目であるかを返します。
	 * @return
	 */
	int getBeginRowNum() {
		return _beginRowPos;
	}
	
	/**
	 * このオブジェクトの最後のRowが、Tableの何行目であるかを返します。
	 * @return
	 */
	int getLastRowNum() {
		return _beginRowPos + _filledCount - 1;
	}
	
	
	/**
	 * このオブジェクトに作成されたRowオブジェクトの最大のインデックス番号を返します。
	 * @return Rowオブジェクトの最大のインデックス番号
	 */
	int getFilledCount() {
		return _filledCount;
	}
	
	/**
	 * 指定されたインデックス番号のRowオブジェクトを返します。
	 * @param index 取得するRowオブジェクトの、このオブジェクトにおけるインデックス番号
	 * @return Rowオブジェクト
	 */
	Row getRowByIndex(int index) {
		if (index < 0) throw new IllegalArgumentException("indexは0以上でなければなりません。");
		return _rows[index];
	}
	
	/**
	 * このオブジェクトを持つTableオブジェクトを返します。
	 * @return このオブジェクトを持つTableオブジェクト
	 */
	Table getTable() {
		return _table;
	}
	
	/**
	 * 指定されたインデックス番号以降のRowオブジェクトを削除します。
	 * @param index 削除するRowオブジェクトのインデックス
	 */
	void eraseRowsAfter(int index) {
		for (int i = index; i < _filledCount; i++) {
			_rows[i] = null;
		}
		_filledCount = index;
	}
}
