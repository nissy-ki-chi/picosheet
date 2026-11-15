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
package com.nissy_ki_chi.picosheet.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.nissy_ki_chi.picosheet.core.Book;
import com.nissy_ki_chi.picosheet.core.Cell;
import com.nissy_ki_chi.picosheet.core.PicosheetException;
import com.nissy_ki_chi.picosheet.core.Resolver;


/**
 * @author yusuke nishikawa
 *
 */
public class TextLoader {

	private static final String IGNORE_LINE_PATTERN = "^\\s*#|^\\s+|";
	private static Pattern _ignoreLinePattern = Pattern.compile(IGNORE_LINE_PATTERN);

	private static final String PARAMETER_PATTERN = "[^\\s=]+=[^\\s=]+";
	private static Pattern _parameterPattern = Pattern.compile(PARAMETER_PATTERN);

	private static final String CELLNAME_VALUE_PATTERN = "([a-zA-Z_][a-zA-Z0-9_]*)=(.*)";
	private static final Pattern _cellnameValuePattern = Pattern.compile(CELLNAME_VALUE_PATTERN);

	Book _book;

	public TextLoader() {
		_book = null;
	}

	public TextLoader(Book book) {
		_book = book;
	}

	public Book load(BufferedReader br) throws IOException, PicosheetException {

		// beginbookで始まり、endbookで終わること
		String line = null;
		while ((line = filterdRead(br)) != null) {
			String firstToken = getFirstToken(line);
			// beginbookが見つかったらBookの処理に移る
			if (firstToken.equalsIgnoreCase("beginbook")) {
				loadBook(line, br);
			}
		}
		return _book;
	}


	private void loadBook(String currentLine, BufferedReader br) throws PicosheetException, IOException {

		// Bookオブジェクトが作成されていない場合、nameパラメータを使って作成する
		if (_book == null) {
			Map<String, String> params = getParams(currentLine);
			if (params.get("name") == null) {
				// nameパラメータが無かったらエラー
				throw new PicosheetException("name not found.");
			}
			_book = new Book(params.get("name"));
		}

		String line;
		while ((line = filterdRead(br)) != null) {
			String firstToken = getFirstToken(line);
			// beginsheetが見つかったらシート読み込みの処理に移る
			if (firstToken.equalsIgnoreCase("beginsheet")) {
				loadSheet(line, br);
			}
			// endbookだったらBookの処理を終了
			if (firstToken.equalsIgnoreCase("endbook")) {
				return;
			}
		}
	}

	private void loadSheet(String currentLine, BufferedReader br) throws PicosheetException, IOException {

		Map<String, String> params = getParams(currentLine);
		if (params.get("name") == null) {
			// nameパラメータが無かったらエラー
			throw new PicosheetException("name not found.");
		}
		_book.addSheet(params.get("name"));

		String line;
		while ((line = filterdRead(br)) != null) {
			String firstToken = getFirstToken(line);
			// begincellが見つかったらセル読み込みの処理に移る
			if (firstToken.equalsIgnoreCase("begincell")) {
				loadCell(line, br);
			}
			// begintableが見つかったらテーブル読み込みの処理に移る
			if (firstToken.equalsIgnoreCase("begintable")) {
				loadTable(line, br);
			}
			// endsheetだったらSheetの処理を終了
			if (firstToken.equalsIgnoreCase("endsheet")) {
				return;
			}
		}
	}

	private void loadCell(String currentLine, BufferedReader br) throws PicosheetException, IOException {

		new CellCreator("endcell") {

			@Override
			protected Cell createCell(String cellName) {
				return _book.getResolver().getCurrentSheet().addCell(cellName);
			}

		}.process(br);

	}

	private void loadTable(String currentLine, BufferedReader br) throws PicosheetException, IOException {

		Map<String, String> params = getParams(currentLine);
		final String tableName = params.get("name");
		if (tableName == null) {
			// nameパラメータが無かったらエラー
			throw new PicosheetException("name not found.");
		}

		int rowSize = 0;
		if (params.get("rowsize") == null) {
			// rowsizeパラメータが無かったらエラー
			throw new PicosheetException("rowsize not found.");
		} else {
			try {
				rowSize = Integer.parseInt(params.get("rowsize"));
			} catch (NumberFormatException nfe) {
				throw new PicosheetException("invalid rowsize: rowsize = " + params.get("rowsize"));
			}
		}

		int colSize = 0;
		if (params.get("colsize") == null) {
			// colsizeパラメータが無かったらエラー
			throw new PicosheetException("colsize not found.");
		} else {
			try {
				colSize = Integer.parseInt(params.get("colsize"));
			} catch (NumberFormatException nfe) {
				throw new PicosheetException("invalid colsize: colsize = " + params.get("colsize"));
			}
		}

		_book.getResolver().getCurrentSheet().addTable(tableName, rowSize, colSize);

		new CellCreator("endtable") {

			@Override
			protected Cell createCell(String cellName) {
				// Tableの場合、作成した時点ですでにセルが作られているので、これを得る。
				Resolver resolver = _book.getResolver();
				return resolver.getCell(tableName + cellName);
			}

		}.process(br);

	}


	/**
	 * 文字ストリームからコメント行や空行、前後の空白を除いた、有効な行を取得して返します。
	 * @param br 文字ストリーム
	 * @return 有効な行
	 * @throws IOException 文字ストリームの読み込みに失敗した場合
	 */
	private String filterdRead(BufferedReader br) throws IOException {

		String line = null;
		// ファイルの終わりまでループ
		while ((line = br.readLine()) != null) {
			line = line.trim();
			// 読み込んだ行が有効であれば、そこで読み込みを中止し、読み込んだ行を返す
			if (!_ignoreLinePattern.matcher(line).matches()) {
				return line;
			}
		}
		// 最後まで有効な行が無かったらnullを返す
		return null;
	}

	private String getFirstToken(String str) {
		String[] tokens = str.split("\\s");
		return tokens[0];
	}

	private Map<String, String> getParams(String line) {
		Map<String, String> map = new HashMap<String, String>();
		Matcher m = _parameterPattern.matcher(line);

		while (m.find()) {
			for (int i = 0; i <= m.groupCount(); i++) {
				if (m.group(i) != null){
					String[] paramValue = m.group(i).split("=");
					map.put(paramValue[0].toLowerCase(), paramValue[1]);
				}
			}
		}
		return map;
	}

	/**
	 * セル作成を行うためのインナークラス。シートにセルを追加する場合、およびシートにテーブルを
	 * 作成してその中にセルを追加する場合に使用する。
	 * @author yusuke nishikawa
	 *
	 */
	private abstract class CellCreator {

		private String _endString;

		@SuppressWarnings("unused")
		private CellCreator(){}

		public CellCreator(String endString){
			_endString = endString;
		}

		public void process(BufferedReader br) throws IOException, PicosheetException {

			String line;
			while ((line = br.readLine()) != null) {

				// コンストラクタで渡されたendStringだったら処理を終了
				if (line.equalsIgnoreCase(_endString)) {
					return;
				}

				Matcher m = _cellnameValuePattern.matcher(line);
				if (m.find() && m.groupCount() != 2) {
					// Cell指定がおかしい
					throw new PicosheetException("invalid cell definition.");
				}
				String cellName = m.group(1);
				String cellValue = m.group(2);
				Cell cell = createCell(cellName);
				if (cell != null) {
					cell.setValue(cellValue);
				}

			}
		}

		protected abstract Cell createCell(String cellName);

	}

}
