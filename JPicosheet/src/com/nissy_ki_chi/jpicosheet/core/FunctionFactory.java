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
