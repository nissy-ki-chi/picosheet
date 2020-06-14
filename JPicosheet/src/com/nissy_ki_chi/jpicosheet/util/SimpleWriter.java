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

import java.io.IOException;
import java.io.Writer;
import java.util.concurrent.locks.ReentrantReadWriteLock.WriteLock;

import com.nissy_ki_chi.jpicosheet.core.Book;
import com.nissy_ki_chi.jpicosheet.core.Cell;
import com.nissy_ki_chi.jpicosheet.core.ConcurrentBookWrapper;
import com.nissy_ki_chi.jpicosheet.core.Group;
import com.nissy_ki_chi.jpicosheet.core.Sheet;
import com.nissy_ki_chi.jpicosheet.core.Table;


public class SimpleWriter {

	public static String OPTION_MARK = "@";

	private ConcurrentBookWrapper _bookWrapper;

	/**
	 * ブックオブジェクトの情報を、文字型出力ストリームに書き出すためのオブジェクトを作成します<br>
	 * ブックオブジェクトは同期化されないため、同期化のためのラッパークラスである<code>ConcurrentBookWrapper</code>
	 * でラップしたオブジェクトを引数に渡します。
	 * @param cbr ConcurrentBookWrapperオブジェクト
	 */
	public SimpleWriter(ConcurrentBookWrapper cbr) {
		_bookWrapper = cbr;
	}


	/**
	 * ブックオの情報をストリームに書き出します。<br>
	 * このメソッドはConcurrentBookWrapperの書き込みロックによって同期化されます。
	 * @param w 出力ストリーム
	 * @throws IOException
	 */
	public void write(Writer w) throws IOException {

		try {
			WriteLock writeLock = _bookWrapper.getWriteLock();
			// 出力用バッファ
			StringBuilder sb = new StringBuilder();

			Book book = _bookWrapper.getBook(writeLock);

			// Book固有の設定を出力
			sb.setLength(0);
			sb.append(OPTION_MARK).append("Book:name").append("\t").append(book.getName());
			output(w, sb);

			// Book内のシート分ループ
			for (Sheet sheet: book.getSheets()) {

				// シート固有の設定を出力
				sb.setLength(0);
				sb.append(OPTION_MARK).append(sheet.getName()).append(":Name").append("\t").append(sheet.getName());
				output(w, sb);

				sb.setLength(0);
				sb.append(OPTION_MARK).append(sheet.getName()).append(":MathContext").append("\t").append(sheet.getMathContext().toString());
				output(w, sb);

				// シート内のセル分ループ
				for (Cell cell: sheet.getCells()) {

					// セルがテーブルセルの場合、ここでは出力しない
					if (cell.getName().indexOf("#") >= 0) {
						continue;
					}

					// セル値の出力
					sb.setLength(0);
					sb = getCellValueLine(cell, sb);
					output(w, sb);

					// セルラベルの出力
					sb.setLength(0);
					if (!cell.getLabel().equals("")) {
						sb.append(OPTION_MARK).append(cell.getFullyQualifiedName()).append(":Label").append("\t").append(cell.getLabel());
						output(w, sb);
					}
				}

				// シート内のテーブル分ループ
				for (Table table: sheet.getTables()) {

					//テーブル設定の出力
					sb.setLength(0);
					sb.append(OPTION_MARK).append(table.getFullyQualifiedName()).append(":Table").append("\t").append(table.getRowSize()).append(",").append(table.getColSize());
					output(w, sb);

					// テーブル値の出力
					for (Cell tableCell: table.getCells()) {
						sb.setLength(0);
						sb = getCellValueLine(tableCell, sb);
						output(w, sb);
					}

				}

				// シート内のグループ分ループ
				for (Group group: sheet.getGroups()) {

					// グループ設定の出力
					sb.setLength(0);
					StringBuilder groupCellsSb = new StringBuilder();
					for (Cell groupCell: group.getCells()) {
						groupCellsSb.append(groupCell.getFullyQualifiedName()).append(",");
					}
					if (groupCellsSb.length() > 1) {
						groupCellsSb.deleteCharAt(groupCellsSb.length()-1);
					}
					sb.append(OPTION_MARK).append(group.getFullyQualifiedName()).append(":Group").append("\t").append(groupCellsSb.toString());
					output(w, sb);
				}
			}
			w.flush();
		} finally {
			_bookWrapper.releaseWriteLock();
		}
	}


	/**
	 * セルの値をStringBuilderにセットする
	 * @param cell 対象とするセル
	 * @param sb セルの値をセットするオブジェクト
	 * @return セルの値がセットされたオブジェクト
	 */
	private StringBuilder getCellValueLine (Cell cell, StringBuilder sb) {

		sb.append(cell.getFullyQualifiedName());
		sb.append("\t");
		switch (cell.getCellType()) {
		case FORMULA:
			sb.append(cell.getFormula());
			break;
		case NUMBER:
			sb.append(cell.getValue().getNumber().toString());
			break;
		case STRING:
			sb.append(cell.getValue().getString());
			break;
		case BOOLEAN:
			sb.append(cell.getValue().getString());
			break;
		case EMPTY:
			sb.append("");
			break;
		default:
				assert false: "処理されないセルタイプ：" + cell.getCellType().toString();
		}
		return sb;

	}
	/**
	 * セルの値を書き出す
	 * @param w 出力ストリーム
	 * @param sb セルの値がセットされたStringBuilderオブジェクト
	 * @throws IOException
	 */
	private void output(Writer w, StringBuilder sb) throws IOException {

		for (int c = 0; c < sb.length(); c++) {
			switch (sb.charAt(c)) {
			case '\n':
				sb.replace(c, c+1, "\\n");
				c++;
				break;
			case '\\':
				sb.replace(c, c+1, "\\");
				c++;
				break;
			}
		}
		w.write(sb.append("\n").toString());
	}
}
