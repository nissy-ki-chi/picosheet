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
package com.nissy_ki_chi.jpicosheet.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.math.MathContext;
import java.util.concurrent.locks.ReentrantReadWriteLock.WriteLock;

import com.nissy_ki_chi.jpicosheet.core.Book;
import com.nissy_ki_chi.jpicosheet.core.ConcurrentBookWrapper;



public class SimpleReader {

	private ConcurrentBookWrapper _bookWrapper;

	/**
	 * 文字型入力ストリームからブック情報を読み込むためのオブジェクトを作成します。<br>
	 * ブックオブジェクトは同期化されないため、同期化のためのラッパークラスである<code>ConcurrentBookWrapper</code>
	 * でラップしたオブジェクトを引数に渡します。
	 * @param cbr ConcurrentBookWrapperオブジェクト
	 */
	public SimpleReader(ConcurrentBookWrapper cbr) {

		_bookWrapper = cbr;
	}


	/**
	 * ストリームから読み込んだテキストデータを元に、ブックに値をセットします。<br>
	 * このメソッドはConcurrentBookWrapperの書き込みロックによって同期化されます。
	 * @param br 入力ストリーム
	 * @throws IOException
	 */
	public void Read(BufferedReader br) throws IOException {


		try {
//			WriteLock writeLock = _bookWrapper.getWriteLock();
//			Book book = _bookWrapper.getBook(writeLock);
//			Book book = _bookWrapper.getBook();

//			book.recalcDisable();
			_bookWrapper.recalcDisable();
			while(true) {
				// 行を読み込むと同時にエスケープ文字を復元
				String readLine = revertingEscapedChars(br.readLine());
				if (readLine == null) {
					break;
				} else {
					if (readLine.charAt(0) == '@') {
						doAtmarkLine(readLine);
					} else {
						String cellName = readLine.substring(0, readLine.indexOf("\t"));
						String cellValue = readLine.substring(readLine.indexOf("\t")+1);
						_bookWrapper.setCellValue(cellName, cellValue);
					}
				}

			}

			

		} finally {
			_bookWrapper.recalcEnable();
//			_bookWrapper.releaseWriteLock();
		}
	}

	/**
	 * エスケープされた文字列を復元する。nullが渡された場合何もしない。
	 * @param src エスケープ処理済の文字列
	 * @return 復元された文字列
	 */
	private String revertingEscapedChars(String src) {

		if (src == null) {
			return null;
		}

		StringBuilder sb = new StringBuilder(src);
		for (int c = 0; c < sb.length(); c++) {
			switch (sb.charAt(c)) {
			case '\\':
				if (c == sb.length()) {
					throw new RuntimeException("invalid escape character at index " + Integer.toString(c) + " : " + sb.toString());
				}
				switch (sb.charAt(c + 1)) {
				case '\\':
					sb.replace(c, c+2, "\\");
					break;
				case 'n':
					sb.replace(c, c+2, "\n");
					break;
				default:
					throw new RuntimeException("invalid escape character at index " + Integer.toString(c) + " : " + sb.toString());
				}
			}
		}
		return sb.toString();
	}


	/**
	 * アットマーク付きの行に対し、それぞれの値をセットする
	 * @param book ブックオブジェクト
	 * @param line アットマーク付きのテキスト行
	 */
	private void doAtmarkLine(String line){

		if (line.matches("@Book:name\t.*")) {
			String bookName = splitAtmarkValue(line);
			_bookWrapper.setBookName(bookName);
			return;
		}

		if (line.matches("@[^:]+:MathContext\t.*")) {
			String mathContextString = splitAtmarkValue(line);
			String sheetName = line.substring(1, line.indexOf(':'));
			_bookWrapper.addSheet(sheetName);
			_bookWrapper.setMathContext(sheetName, new MathContext(mathContextString));
			return;
		}

		if (line.matches("@[^:]+:Label\t.*")) {
			String labelString = splitAtmarkValue(line);
			String sheetCellName = line.substring(1, line.indexOf(':'));
			_bookWrapper.setCellLabel(sheetCellName, labelString);
			return;
		}

		if (line.matches("@[^:]+:Table\t.*")) {
			String[] rowcolStr = splitAtmarkValue(line).split(",");
			int rowSize = Integer.parseInt(rowcolStr[0]);
			int colSize = Integer.parseInt(rowcolStr[1]);
			String[] sheetTableName = line.substring(1, line.indexOf(':')).split("!");
			_bookWrapper.addTable(sheetTableName[0], sheetTableName[1], rowSize, colSize);
		}

		if (line.matches("@[^:]+:Group\t.*")) {
			String[] cellToAdd = splitAtmarkValue(line).split(",");
			String[] sheetGroupName = line.substring(1, line.indexOf(':')).split("!");
			_bookWrapper.addGroup(sheetGroupName[0], sheetGroupName[1]);
			_bookWrapper.addCellsToGroup(sheetGroupName[0], sheetGroupName[1], cellToAdd);
		}
	}

	/**
	 * アットマーク付きのテキスト行の値部分（最初に現れるタブ文字より後ろすべて）を返す
	 * @param atmarkLine アットマークつきのテキスト行
	 * @return テキスト行の値部分の文字列
	 */
	private String splitAtmarkValue(String atmarkLine) {

		if (atmarkLine.length() > atmarkLine.indexOf("\t")+1) {
			return atmarkLine.substring(atmarkLine.indexOf("\t")+1);
		} else {
			return "";
		}

	}

}
