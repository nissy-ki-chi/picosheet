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
package com.nissy_ki_chi.jpicosheet.core;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.regex.Pattern;


/**
 * 複数のシートを持つことのできる"ブック"です。<br>
 * シートの保持だけでなく、保持しているシートの自動再計算の制御やセル間の参照関係を維持する機能も保持しています。
 * @author yusuke nishikawa
 *
 */
public class Book {


	/**
	 * ブック名
	 */
	private String _name;
	/**
	 * 計算機オブジェクト
	 */
	private Calculator _calculator;
	/**
	 * セル参照解決オブジェクト
	 */
	private Resolver _resolver;
	/**
	 * このブックが持つシートのMap
	 */
	private HashMap<String, Sheet> _sheets = new HashMap<String, Sheet>();
	/**
	 * 関数インスタンスを生成するファクトリオブジェクト
	 */
	private FunctionFactory _functionFactory;


	/**
	 * ブック名のパターン文字列
	 */
	static final String BOOK_NAME_PATTERN = "[a-zA-Z_][a-zA-Z0-9_]*";
	/**
	 * ブック名の正規表現パターン
	 */
	private static Pattern _bookNamePattern = Pattern.compile(BOOK_NAME_PATTERN);


	@SuppressWarnings("unused")
	private Book() {}

	/**
	 * ブック名を指定してブックを作成します
	 * @param bookName ブック名
	 * @throws IllegalArgumentException ブック名が正しくない場合
	 */
	public Book(String bookName) throws IllegalArgumentException {

		// ブック名のチェック
		validateBookName(bookName);

		this._name = bookName;
		this._calculator = new Calculator(this);
		this._resolver = new Resolver(this);
		this._functionFactory = new FunctionFactory();
		this._functionFactory.loadBuiltinFunctions();
	}


	/**
	 * ブック名を返します
	 * @return ブック名
	 */
	public String getName(){
		return this._name;
	}

	/**
	 * ブック名をセットします
	 * @param bookName ブック名
	 * @throws IllegalArgumentException ブック名が正しくない場合
	 */
	public void setName(String bookName) {
		// ブック名のチェック
		validateBookName(bookName);
		this._name = bookName;
	}

	/**
	 * 計算オブジェクトを返します。
	 * @return 計算オブジェクト
	 */
	Calculator getCalculator() {
		return this._calculator;
	}

	/**
	 * リゾルバを返します。
	 * @return リゾルバ
	 */
	public Resolver getResolver() {
		return this._resolver;
	}

	/**
	 * 関数ファクトリを返します。
	 * @return 関数ファクトリ
	 */
	FunctionFactory getFunctionFactory() {
		return this._functionFactory;
	}

	/**
	 * 指定した名前のシートがブックに存在する場合、Trueを返します。
	 * @param sheetName 存在を確認するシート名
	 * @return 存在する場合true、そうでない場合false
	 */
	public boolean sheetExists(String sheetName) {
		return this._sheets.containsKey(sheetName);
	}
	
	/**
	 * 指定したシートオブジェクトがブックに存在する場合、Trueを返します。
	 * @param sheet 存在を確認するシートオブジェクト
	 * @return 存在する場合true、そうでない場合false
	 */
	public boolean sheetExists(Sheet sheet) {
		// Sheet#equalsは内容が同じである異なるオブジェクトに対しtrueを返すため、equalsは使わない
		for (Sheet s: this._sheets.values()) {
			if (s == sheet) {
				return true;
			}
		}
		return false;
	}
	
	/**
	 * シートを追加します。<br>
	 * 指定したシート名がすでに存在する場合、既存のシートオブジェクトを返します。
	 * @param sheetName シート名
	 * @return 追加したシートオブジェクト
	 */
	public Sheet addSheet(String sheetName)   {
		// シートを追加する
		if (this._sheets.containsKey(sheetName)) {
			return this._sheets.get(sheetName);
		} else {
			Sheet sheet = new Sheet(sheetName, this);
			this._sheets.put(sheetName, sheet);
			// リゾルバのデフォルトシートに登録が無い場合、このシートをデフォルトシートにする
			if (this._resolver.getCurrentSheet() == null) {
				this._resolver.setCurrentSheet(sheet);
			}
			return sheet;
		}
	}


	/**
	 * このブックが保持するすべてのシートへの参照を返します
	 * @return このブックが保持するシートへの参照
	 */
	public List<Sheet> getSheets() {
		return new ArrayList<Sheet>(this._sheets.values());
	}

	/**
	 * 既存のシートのシート名を変更します。<br>
	 * 変更対象のシートが存在しない場合、ReferenceNotFoundExceptionが発生します。<br>
	 * 変更後のシート名を持つシートがすでに存在していた場合、そのシートオブジェクトは削除されます。<br>
	 * 名前を変更したのがデフォルトシートだった場合、名前を変更してもデフォルトシートはそのシートのままです。
	 * @param sheetName 変更対象のシート名
	 * @param newSheetName 変更後のシート名
	 */
	public void renameSheet(String sheetName, String newSheetName) {
		// シート名を変更する
		if (this._sheets.containsKey(sheetName)) {
			// Sheetオブジェクト内部の名前を変更
			Sheet sheet = this._sheets.get(sheetName);
			sheet.setName(newSheetName);
			// sheets上の管理をnewSheetNameに変える
			this._sheets.put(newSheetName, this._sheets.get(sheetName));
			this._sheets.remove(sheetName);
		} else {
			throw new ReferenceNotFoundException("no such sheet " + sheetName);
		}
	}

	/**
	 * 引数に指定したシートを削除します。<br>
	 * 指定したシートが存在しない場合、ReferenceNotFoundExceptionが発生します。
	 * @param sheetName 削除対象のシート名
	 */
	public void deleteSheet(String sheetName) {
		// シートを削除する
		if (this._sheets.containsKey((sheetName))) {
			this._sheets.remove(sheetName);
			if (_resolver.getCurrentSheet().getName().equals(sheetName)) {
				// 削除したシートがデフォルトシートだった場合、デフォルトシートをnullにする
				_resolver.setCurrentSheet(null);
			}
		} else {
			throw new ReferenceNotFoundException("no such sheet " + sheetName);
		}
	}

	/**
	 * 引数に指定したシートオブジェクトを返します。<br>
	 * 指定したシートが存在しない場合、ReferenceNotFoundExceptionが発生します。
	 * @param sheetName シート名
	 * @return シートオブジェクト
	 */
	public Sheet getSheet(String sheetName) {
		// シートを返す
		if (this._sheets.containsKey(sheetName)) {
			return this._sheets.get(sheetName);
		} else {
			throw new ReferenceNotFoundException("no such sheet " + sheetName);
		}
	}



	/**
	 * ブックに対する自動再計算を有効にします。<br>
	 * 同時に、すべてのセルを再計算します。
	 */
	public void recalcEnable() {
		this._calculator.recalcEnable();
	}

	/**
	 * ブックに対する自動再計算を無効にします。<br>
	 */
	public void recalcDisable() {
		this._calculator.recalcDisable();
	}

	/**
	 * ブックに対する自動計算が有効か否かを返します。<br>
	 * @return 自動再計算が有効の場合true、無効の場合false
	 */
	public boolean isRecalcEnable() {
		return _calculator.isRecalcEnable();
	}

	// TODO: 関数の追加・変更・削除が行えるようにする。

	/**
	 * 利用可能な関数名の配列を返します。
	 * @return 関数名の配列
	 */
	public String[] getFunctionNames() {
		return _functionFactory.getFunctionNmes();
	}


	/**
	 * 渡された文字列がブック名として正しいかチェックします。<br>
	 * 正しくない場合、例外がスローされます。
	 * @param bookName チェックするシート名
	 * @throws IllegalArgumentException ブック名として正しくない場合
	 */
	private void validateBookName(String bookName) throws IllegalArgumentException {
		if (! isValidBookName(bookName)) {
			throw new IllegalArgumentException("invalid book name \"" + bookName + "\"");
		}
	}

	/**
	 * 渡された文字列がブック名として正しいかチェックします。
	 * @param bookName チェックするシート名
	 * @return 渡された文字列がブック名として正しい場合true、そうでない場合false
	 */
	public static boolean isValidBookName(String bookName) {
		return _bookNamePattern.matcher(bookName).matches();
	}
}

