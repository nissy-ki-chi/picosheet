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
package com.nissy_ki_chi.picosheet.core;

import java.util.HashMap;

/**
 * 関数インスタンスを作成、保持するためのファクトリクラスです。
 * @author yusuke nishikawa
 *
 */
class FunctionFactory {

	/**
	 * このファクトリオブジェクトで生成した関数インスタンスを保持する
	 */
	private HashMap<String, Function> _functionMap = new HashMap<String, Function>();


	/**
	 * 組み込み関数を登録します。
	 */
	void loadBuiltinFunctions() {
		try {
			loadFunctions(BuiltinFunctionsConstants.BUILTIN_FUNCTIONS);
		} catch (Exception e) {
			assert true;
		}
	}

	/**
	 * 引数に指定した関数を登録します。<br>
	 * 関数クラスは<code>Function</code>クラスのサブクラスでなければなりません。そうでない場合<code>IllegalArgumentException</code>がスローされます。
	 *
	 * @param functionDefinitio 関数名と関数クラスを要素に持つ配列
	 * @throws ClassNotFoundException
	 * @throws IllegalAccessException
	 * @throws InstantiationException
	 * @throws ClassNotFoundException 指定した関数クラスが見つからなかった場合
	 */
	void loadFunctions(String[][] functionDefinition) throws InstantiationException, IllegalAccessException, ClassNotFoundException {
		for (int i = 0; i < functionDefinition.length; i++) {

			String uppercaseFunctionName = functionDefinition[i][0];
			String functionClassName = functionDefinition[i][1];

			// 関数クラスの存在確認
			Object obj;
			try {
				obj = Class.forName(functionClassName).newInstance();
			} catch (InstantiationException e) {
				throw e;
			} catch (IllegalAccessException e) {
				throw e;
			} catch (ClassNotFoundException e) {
				throw e;
			}
			// objはFunctionのサブクラスでなければならない
			if (!(obj instanceof Function)){
				throw new IllegalArgumentException(functionClassName + "はFunctionクラスのサブクラスではありません。");
			} else {
				// 指定されたクラスがすでに登録されている場合、先に削除する
				removeFunction(functionDefinition[i][0]);
				// 関数を登録
				_functionMap.put(uppercaseFunctionName, (Function)obj);
			}
		}
	}

	/**
	 * 引数に指定した関数を削除します。指定した関数が登録されていなかった場合は何もしません。
	 * @param functionName
	 */
	void removeFunction(String functionName) {
		String upperFunctionName = functionName;
		if (_functionMap.containsKey(upperFunctionName)) {
			_functionMap.remove(upperFunctionName);
		}
	}

	/**
	 * すべての関数を削除します
	 */
	void removeAllFunctions() {
		_functionMap.clear();
	}

	/**
	 * 指定した関数を保持しているかどうかを返します。
	 * @param 関数名
	 * @return functionName 関数を保持している場合true、そうでない場合false
	 */
	boolean hasFunction(String functionName) {
		String upperdFunctionName = functionName;
		return _functionMap.containsKey(upperdFunctionName);
	}

	/**
	 * 指定した関数を返します。関数が登録されていない場合nullを返します。
	 * @param functionName 関数名
	 * @return 関数のインスタンス。関数が登録されていない場合null。
	 */
	Function getFunctionInstance(String functionName) {
		String upperdFunctionName = functionName;
		return _functionMap.get(upperdFunctionName);
	}

	/**
	 * 登録されている関数名の配列を返します
	 * @return 関数名の配列
	 */
	String[] getFunctionNmes() {
		String[] strArrayType = {""};
		return _functionMap.keySet().toArray(strArrayType);
	}

}
