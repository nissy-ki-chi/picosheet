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

import java.math.BigDecimal;
import java.math.MathContext;
import java.util.ArrayList;
import java.util.List;
import java.util.Stack;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.nissy_ki_chi.jpicosheet.core.Element.ElementType;
import com.nissy_ki_chi.jpicosheet.core.Element.ErrorType;
import com.nissy_ki_chi.jpicosheet.core.Element.Operator;




/**
 * 計算式をパースするためのクラスです。
 * @author yusuke nishikawa
 *
 */
class FormulaParser {

	private static final String SHEETCELL_NAME_PATTERN = Sheet.SHEET_NAME_PATTERN + "!" + Cell.CELL_NAME_PATTERN;
	private static final String SHEETGROUP_NAME_PATTERN = Sheet.SHEET_NAME_PATTERN + "!" + Group.GROUP_NAME_PATTERN;
	private static final String SHEETTABLE_NAME_WITH_ADDRESS_PATTERN = Sheet.SHEET_NAME_PATTERN + "!" + Table.TABLE_NAME_WITH_ADDRESS_PATTERN;
	private static final String SHEETTABLE_NAME_WITH_RANGE_PATTERN = Sheet.SHEET_NAME_PATTERN + "!" + Table.TABLE_NAME_WITH_RANGE_PATTERN;
	private static final String SHEETTABLE_NAME_PATTERN = Sheet.SHEET_NAME_PATTERN + "!" + Table.TABLE_NAME_PATTERN;
	private static final String SPACE_PATTERN = "\\s+";
	private static final String NUMERIC_PATTERN = "(([0-9]+\\.[0-9]*)|(\\.[0-9]+)|([0-9]+))";
	private static final String TRUE_FALSE_PATTERN = "true|false";
	private static final String STRING_PATTERN = "\"[^\"]*\"";
	private static final String FUNCTION_NAME_PATTERN = "[a-zA-Z][a-zA-Z0-9_]*\\(";
	private static final String ARITHMETIC_OPERATOR_PATTERN = "\\+|-|\\*|/|\\^";
	private static final String PARENTHESIS_PATTERN = "\\(|\\)";
	private static final String COMPARISON_OPERATOR_PATTERN = "<=|>=|<>|<|>|=";
	private static final String STRING_OPERATOR_PATTERN = "&";
	private static Pattern _tokens = Pattern.compile(SPACE_PATTERN + "|" +
														NUMERIC_PATTERN + "|" +
														TRUE_FALSE_PATTERN + "|" +
														STRING_PATTERN + "|" +
														COMPARISON_OPERATOR_PATTERN + "|" +
														ARITHMETIC_OPERATOR_PATTERN + "|" +
														STRING_OPERATOR_PATTERN + "|" +
														PARENTHESIS_PATTERN + "|" +
														FUNCTION_NAME_PATTERN + "|" + "," + "|" +
														SHEETGROUP_NAME_PATTERN + "|" +
														SHEETCELL_NAME_PATTERN + "|" +
														SHEETTABLE_NAME_WITH_RANGE_PATTERN + "|" +
														SHEETTABLE_NAME_WITH_ADDRESS_PATTERN + "|" +
														Group.GROUP_NAME_PATTERN + "|" +
														Table.TABLE_NAME_WITH_RANGE_PATTERN + "|" +
														Table.TABLE_NAME_WITH_ADDRESS_PATTERN + "|" +
														SHEETTABLE_NAME_PATTERN + "|" +
														Table.TABLE_NAME_PATTERN + "|" +
														Cell.CELL_NAME_PATTERN);
	private static Pattern _cellRefPattern = Pattern.compile(
			SHEETTABLE_NAME_WITH_ADDRESS_PATTERN + "|" + Table.TABLE_NAME_WITH_ADDRESS_PATTERN + "|" +
			SHEETCELL_NAME_PATTERN + "|" + Cell.CELL_NAME_PATTERN);
	private static Pattern _groupRefPattern = Pattern.compile(SHEETGROUP_NAME_PATTERN + "|" + Group.GROUP_NAME_PATTERN);
	private static Pattern _tableRefPattern = Pattern.compile(SHEETTABLE_NAME_PATTERN + "|" + Table.TABLE_NAME_PATTERN + "|" + Table.TABLE_NAME_WITH_RANGE_PATTERN);
	private static Pattern _spacePattern = Pattern.compile(SPACE_PATTERN);
	private static Pattern _numericPattern = Pattern.compile(NUMERIC_PATTERN);
	private static Pattern _truefalsePattern = Pattern.compile(TRUE_FALSE_PATTERN);
	private static Pattern _stringPattern = Pattern.compile(STRING_PATTERN);
	private static Pattern _functionPattern = Pattern.compile(FUNCTION_NAME_PATTERN);

	private enum InvokerOpType {
		FUNCTION,
		LEFT_PARENTHESIS,
		NOTHING;
	}

	/**
	 * 文字列をトークンごとに分割します
	 * @param calcString 分割対象の式の文字列
	 * @param mc MathContextオブジェクト
	 * @param funcFactory 関数インスタンスを生成するファクトリオブジェクト
	 * @return トークンごとに分割されたElement配列
	 */
	static Element[] split(String calcString, MathContext mc, FunctionFactory funcFactory) {

		// 文字列をトークンに分解する
		List<Element> list = new ArrayList<Element>();
		Matcher match = _tokens.matcher(calcString);
		String chunkStr;
		int nextStartIndex = 0;

		boolean containsFunction = false;
		boolean splitSuccess = true;

		while (match.find()) {
			// マッチした最初の文字位置は、nextStartIndexの文字位置でなければならない。
			// そうでない場合、nextStartIndexからstartまでの間の文字列が式の正規表現にマッチしていない。
			if ((nextStartIndex) != match.start()) {
				// マッチしない文字列が見つかった。エラー
				list.clear();
				containsFunction = false;
				list.add(Element.newElement(ElementType.ERROR, ErrorType.INVALID_FORMULA));
				splitSuccess = false;
				break;
			}

			// マッチした文字列をトークンとして格納
			// つまり、オペレータをトークンとして格納する
			chunkStr = match.group();

			// 空文字列とマッチした場合、何もしない
			if (chunkStr.length() == 0) {
				continue;
			}

			// マッチした文字列に応じて処理
			if (chunkStr.equals("+") || chunkStr.equals("-")) {
				// +,- の場合、単項演算子か二項演算子かの判断が必要
				boolean isUnary = false;
				if (list.size() == 0) {
					// 式の左端に現れた場合、単項演算子
					isUnary = true;
				} else {
					Element leftElem = list.get(list.size()-1);
					//トークンの左隣が関数もしくはオペレータの場合
					// 左隣が+,-,*,/,^,&,関数、左括弧,比較演算子の場合は単項演算子
					if (leftElem.getType() == ElementType.FUNCTION) {
						isUnary = true;
					} else if (leftElem.getType() == ElementType.OPERATOR) {
						switch(list.get(list.size()-1).getOperator()) {
							case COMMA:
							case PLUS:
							case MINUS:
							case TIMES:
							case DIVIDE:
							case POWER:
							case CONCATENATE:
							case UNARY_PLUS:
							case UNARY_MINUS:
							case LEFT_PARENTHESIS:
							case LESS_THAN:
							case LESS_EQUAL:
							case EQUALS:
							case NOT_EQUALS:
							case GRATOR_THAN:
							case GRATER_EQUAL:
								isUnary = true;
								break;
							default:
								// それ以外の場合は二項演算子
								isUnary = false;
						}
					} else {
						// トークンの左隣が関数もしくはオペレータでない場合、二項演算子
						isUnary = false;
					}
				}

				// 単項/二項演算子別にElementを生成する
				if (isUnary) {
					if (chunkStr.equals("+")) {
						list.add(Element.newElement(ElementType.OPERATOR, Element.Operator.UNARY_PLUS));
					} else {
						list.add(Element.newElement(ElementType.OPERATOR, Element.Operator.UNARY_MINUS));
					}
				} else {
					if (chunkStr.equals("+")) {
						list.add(Element.newElement(ElementType.OPERATOR, Element.Operator.PLUS));
					} else {
						list.add(Element.newElement(ElementType.OPERATOR, Element.Operator.MINUS));
					}
				}
			}else if (chunkStr.equals("*")){
				// *
				list.add(Element.newElement(ElementType.OPERATOR, Element.Operator.TIMES));
			}else if (chunkStr.equals("/")) {
				// /
				list.add(Element.newElement(ElementType.OPERATOR, Element.Operator.DIVIDE));
			}else if (chunkStr.equals("^")) {
				// ^
				list.add(Element.newElement(ElementType.OPERATOR, Element.Operator.POWER));
			}else if (chunkStr.equals("<")) {
				// <
				list.add(Element.newElement(ElementType.OPERATOR, Element.Operator.LESS_THAN));
			}else if (chunkStr.equals(">")) {
				// >
				list.add(Element.newElement(ElementType.OPERATOR, Element.Operator.GRATOR_THAN));
			}else if (chunkStr.equals("<=")) {
				// <=
				list.add(Element.newElement(ElementType.OPERATOR, Element.Operator.LESS_EQUAL));
			}else if (chunkStr.equals(">=")) {
				// >=
				list.add(Element.newElement(ElementType.OPERATOR, Element.Operator.GRATER_EQUAL));
			}else if (chunkStr.equals("<>")) {
				// <>
				list.add(Element.newElement(ElementType.OPERATOR, Element.Operator.NOT_EQUALS));
			}else if (chunkStr.equals("=")) {
				// =
				list.add(Element.newElement(ElementType.OPERATOR, Element.Operator.EQUALS));
			}else if (_truefalsePattern.matcher(chunkStr).matches()) {
				// true/false
				if (chunkStr.equalsIgnoreCase("TRUE")) {
					list.add(Element.newElement(ElementType.BOOLEAN, true));
				} else {
					list.add(Element.newElement(ElementType.BOOLEAN, false));
				}
			}else if (chunkStr.equals("&")) {
				// &
				list.add(Element.newElement(ElementType.OPERATOR, Element.Operator.CONCATENATE));
			}else if (_cellRefPattern.matcher(chunkStr).matches()) {
				// 参照(アドレス付きテーブル参照も含む)
				list.add(Element.newElement(ElementType.REFERENCE, chunkStr));
			}else if (_groupRefPattern.matcher(chunkStr).matches()) {
				// グループ参照
				list.add(Element.newElement(ElementType.GROUP_REFERENCE, chunkStr));
			}else if (_tableRefPattern.matcher(chunkStr).matches()) {
				// テーブル参照(テーブルのみ、もしくは範囲付きテーブル)
				list.add(Element.newElement(ElementType.TABLE_REFERENCE, chunkStr));
			}else if (chunkStr.equals("(")) {
				// 左括弧
				list.add(Element.newElement(ElementType.OPERATOR, Element.Operator.LEFT_PARENTHESIS));
			}else if (_functionPattern.matcher(chunkStr).matches()) {
				// 関数
				// chunkStrの末尾には左括弧が付いている。ElementのValueにはこれを除いてセットする。
				String functionName = chunkStr.substring(0, chunkStr.length()-1);
				if (funcFactory.hasFunction(functionName)) {
					list.add(Element.newElement(ElementType.FUNCTION, functionName));
					containsFunction = true;
				} else {
					list.clear();
					containsFunction = false;
					list.add(Element.newElement(ElementType.ERROR, ErrorType.INVALID_FORMULA));
					splitSuccess = false;
					break;
				}
			}else if (chunkStr.equals(")")) {
				// 右括弧
				list.add(Element.newElement(ElementType.OPERATOR, Element.Operator.RIGHT_PARENTHESIS));
			}else if (chunkStr.equals(",")) {
				// カンマ
				list.add(Element.newElement(ElementType.OPERATOR, Element.Operator.COMMA));
			}else if (_numericPattern.matcher(chunkStr).matches()) {
				// 数値
				list.add(Element.newElement(ElementType.NUMBER, new BigDecimal(chunkStr, mc)));
			}else if (_stringPattern.matcher(chunkStr).matches()) {
				// 文字列
				String stripped = chunkStr.substring(1, chunkStr.length() -1);
				list.add(Element.newElement(ElementType.STRING, stripped));
			}else if (_spacePattern.matcher(chunkStr).matches()) {
				// 空白文字
				// Elementは作成しない
			}else {
				// それ以外の場合想定しないオペレータである。
				assert false: "unknown operator " + chunkStr;
			}

			// nextStartIndexを更新
			nextStartIndex = match.end();
		}

		if (splitSuccess) {
			// 式文字列の最後までマッチしたか？
			if (nextStartIndex != calcString.length()) {
				// 文字列の末尾にマッチしない文字列がある。エラー
				list.clear();
				containsFunction = false;
				list.add(Element.newElement(ElementType.ERROR, ErrorType.INVALID_FORMULA));
			}
	
			// 式の中に関数が含まれていて、かつlistのサイズが1以上の場合
			if (containsFunction && list.size() > 1) {
				// 引数のある関数の場合、関数の直後にカンマを1つ追加し、引数の数とカンマの数を合わせる
				for (int i = list.size() -1; i >= 0; i--) {
					Element elem = list.get(i);
					// エレメントが関数で、かつ式の右端でない場合
					if ((elem.getType() == ElementType.FUNCTION) && (i < (list.size() -1))) {
						Element rightElem = list.get(i+1);
						// 関数の右隣のエレメントが右括弧でない場合(=関数に引数がある場合)
						if (!(rightElem.getType() == ElementType.OPERATOR && rightElem.getOperator() == Operator.RIGHT_PARENTHESIS)) {
							// 関数の右隣にカンマを挿入
							list.add(i+1, Element.newElement(ElementType.OPERATOR, Operator.COMMA));
						}
					}
				}
			}
		}

		// 処理結果をElement型の配列にして返す
		Element[] retStrArray = new Element[list.size()];
		retStrArray = list.toArray(retStrArray);
		return retStrArray;
	}


	/**
	 * 中置記法の順に並べられたElementの配列が、中置記法式として正しいかチェックします。
	 * @param infixElem チェック対象となる、中置記法式の順に配置されたElement配列
	 * @return 中置記法式として正しい場合空文字、正しくない場合場合エラーメッセージ文字列
	 */
	static Element checkInfixNotation(Element[] infixElem) {
		IndexInfo indexInfo = new IndexInfo(0, infixElem.length);
		return checkInfixNotation_doCheck(infixElem, indexInfo, null);
	}

	private static class IndexInfo{
		/**
		 * checkInfixNotation関数の中で使われるインナークラスです
		 */
		int startIndex = 0;
		int endIndex = 0;

		/**
		 * 開始インデックス位置、終了インデックス位置を示してオブジェクトを初期化します
		 * @param startIndex 開始インデックス位置 checkInfixNotation関数(実際には、そこから呼び出される
		 * 再帰処理を行うcheckInfixNotation_doCheck関数)がチェックを開始するインデックス位置
		 * @param endIndex 再帰呼び出しから復帰する際、再帰した関数上で最後にチェックしたインデックス位置
		 */
		IndexInfo(int startIndex,int endIndex) {
			this.startIndex = startIndex;
			this.endIndex = endIndex;
		}
	}

	/**
	 * checkInfixNotation関数から呼び出される、チェック関数本体
	 * @param infixElem チェック対象となる、中置記法式の順に配置されたElement配列
	 * @param indexInfo このメソッド呼び出しでチェックを開始する位置の記録されたオブジェクト
	 * @param opType このメソッドの呼び出し元メソッドが再帰呼び出しを行うきっかけとなったオペレータのタイプ。
	 * OPERATOR.functionあるいはOperator.leftparenthesis
	 * @return 中置記法式として正しい場合null、正しくない場合場合エラーのElementオブジェクト
	 */
	private static Element checkInfixNotation_doCheck(Element[] infixElem, IndexInfo indexInfo, InvokerOpType opType) {

		// indexInfo.startIndexから最後までループ
		for(int i = indexInfo.startIndex; i <= infixElem.length-1; i++) {
			// チェック対象のエレメントと、前後のエレメントを取得
			// 先頭もしくは末尾のエレメントの場合、その前、あるいは後ろのエレメントとしてnullが得られる
			Element checkElem = infixElem[i];
			Element beforeElem = getBeforeElem(infixElem, i);
			Element afterElem = getAfterElem(infixElem, i);
			// エレメントのタイプで判断
			switch (checkElem.getType()) {
			case DATE:
			case NUMBER:
			case STRING:
			case REFERENCE:
			case BOOLEAN:
				// 日付、数値、文字列、参照、グループ参照の場合、前後がオペレータあるいはnullでなければならない
				if ((beforeElem == null ||beforeElem.getType() == ElementType.OPERATOR) &&
						(afterElem == null || afterElem.getType() == ElementType.OPERATOR)) {
					// OK
					break;
				} else {
					// NG
					return Element.newElement(ElementType.ERROR, ErrorType.INVALID_FORMULA);
				}
			case GROUP_REFERENCE:
			case TABLE_REFERENCE:
				// グループ参照もしくはテーブル参照の場合、関数呼び出しの中に存在し、前後がカンマあるいはnullでなければならない
				if (opType == InvokerOpType.FUNCTION &&
						(
							(beforeElem == null || (beforeElem.getType() == ElementType.OPERATOR) && (beforeElem.getOperator() == Operator.COMMA)) ||
							(afterElem == null || (afterElem.getType() == ElementType.OPERATOR) && (afterElem.getOperator() == Operator.COMMA))
						)) {
					// OK
					break;
				} else {
					// NG
					return Element.newElement(ElementType.ERROR, ErrorType.INVALID_FORMULA);
				}
			case FUNCTION:
				// 関数の場合、前がオペレータ(算術/論理/文字列演算子、左括弧、カンマのいずれか)あるいはnullでなければならない
				if ((beforeElem == null) ||
						((beforeElem.getType() == ElementType.OPERATOR) &&
								(beforeElem.getOperator().isArithmeticOperator() ||
								beforeElem.getOperator().isComparisonOperator() ||
								beforeElem.getOperator().isStringOperator() ||
								beforeElem.getOperator() == Operator.LEFT_PARENTHESIS ||
								beforeElem.getOperator() == Operator.COMMA)
						)
					) {
					// OK. 何もしない
				} else {
					// NG
					return Element.newElement(ElementType.ERROR, ErrorType.INVALID_FORMULA);
				}
				// オペレータの種類に関数あるいは左括弧を指定してこの処理を呼び出す(再帰)
				indexInfo.startIndex = i+1;
				{
					Element elem;
					elem = checkInfixNotation_doCheck(infixElem, indexInfo, InvokerOpType.FUNCTION);
					if (elem == null) {
						// チェック位置を再帰呼び出しチェックを行った最後のエレメント位置にする
						indexInfo.startIndex = indexInfo.endIndex;
						i = indexInfo.endIndex;
					} else {
						// elemがnullでない(=再帰呼び出しでエラーがあった)場合、retStrを返す
						return elem;
					}
				}
				break;
			case OPERATOR:
				// オペレータの場合、さらにオペレータの種類で判断
				switch(checkElem.getOperator()) {
				case UNARY_PLUS:
				case UNARY_MINUS:
					// 単項演算子＋－の場合、後ろのエレメントが数値、参照、単項演算子＋－、関数、左括弧のいずれか
					// nullはNG
					if (afterElem == null) {
						// NG
						return Element.newElement(ElementType.ERROR, ErrorType.INVALID_FORMULA);
					}

					if (afterElem.getType() == ElementType.NUMBER ||
						afterElem.getType() == ElementType.REFERENCE ||
						afterElem.getType() == ElementType.FUNCTION ||
							(afterElem.getType() == ElementType.OPERATOR &&
								afterElem.getOperator() == Operator.UNARY_PLUS ||
								afterElem.getOperator() == Operator.UNARY_MINUS ||
								afterElem.getOperator() == Operator.LEFT_PARENTHESIS)) {
						// OK
					} else {
						// NG
						return Element.newElement(ElementType.ERROR, ErrorType.INVALID_FORMULA);
					}
					break;

				case PLUS:
				case MINUS:
				case TIMES:
				case DIVIDE:
				case POWER:
					// 四則演算子、べき乗の場合、前：数値、参照、関数、右括弧  後：数値、参照、関数、左括弧、単項演算子＋－であること
					// nullはNG
					if (beforeElem == null || afterElem == null) {
						// NG
						return Element.newElement(ElementType.ERROR, ErrorType.INVALID_FORMULA);
					}

					if (isSuitableElementsOfArithmetic(beforeElem, afterElem)) {
						// OK
					} else {
						// NG
						return Element.newElement(ElementType.ERROR, ErrorType.INVALID_FORMULA);
					}
					break;

				case EQUALS:
				case NOT_EQUALS:
				case LESS_THAN:
				case LESS_EQUAL:
				case GRATOR_THAN:
				case GRATER_EQUAL:
					// 比較演算子の場合、四則演算子のルールに加えてブーリアン型でもよい
					// nullはNG
					if (beforeElem == null || afterElem == null) {
						// NG
						return Element.newElement(ElementType.ERROR, ErrorType.INVALID_FORMULA);
					}

					if (isSuitableElementsOfComparison(beforeElem, afterElem)) {
						// OK
					} else {
						// NG
						return Element.newElement(ElementType.ERROR, ErrorType.INVALID_FORMULA);
					}
					break;

				case CONCATENATE:
					// 文字列結合演算子の場合、チェックは比較演算子と同じ
					// nullはNG
					if (beforeElem == null || afterElem == null) {
						// NG
						return Element.newElement(ElementType.ERROR, ErrorType.INVALID_FORMULA);
					}

					if (isSuitableElementsOfString(beforeElem, afterElem)) {
						// OK
					} else {
						// NG
						return Element.newElement(ElementType.ERROR, ErrorType.INVALID_FORMULA);
					}
					break;

				case LEFT_PARENTHESIS:
					// 左括弧の場合、前がオペレータ(算術/論理/文字列演算子、左括弧、カンマのいずれか)あるいはnullでなければならない
					if ((beforeElem == null) ||
							((beforeElem.getType() == ElementType.OPERATOR) &&
									(beforeElem.getOperator().isArithmeticOperator() ||
									beforeElem.getOperator().isComparisonOperator() ||
									beforeElem.getOperator().isStringOperator() ||
									beforeElem.getOperator() == Operator.LEFT_PARENTHESIS ||
									beforeElem.getOperator() == Operator.COMMA)
							)
						) {
						// OK. 何もしない
					} else {
						// NG
						return Element.newElement(ElementType.ERROR, ErrorType.INVALID_FORMULA);
					}
					// オペレータの種類に関数あるいは左括弧を指定してこの処理を呼び出す(再帰)
					indexInfo.startIndex = i+1;
					{
						Element elem;
						elem = checkInfixNotation_doCheck(infixElem, indexInfo, InvokerOpType.LEFT_PARENTHESIS);
						if (elem == null) {
							//　startIndexとendIndexが同じ…括弧内に何も無かった。エラー
							if (indexInfo.endIndex == indexInfo.startIndex) {
								return Element.newElement(ElementType.ERROR, ErrorType.INVALID_FORMULA);
							}
							// チェック位置を再帰呼び出しチェックを行った最後のエレメント位置にする
							indexInfo.startIndex = indexInfo.endIndex;
							i = indexInfo.endIndex;
						} else {
							// elemがnullでない(=再帰呼び出しでエラーがあった)場合、elemを返す
							return elem;
						}
					}
					break;
				case COMMA:
					// この処理は関数を引数に呼び出されたか？
					if (opType == InvokerOpType.FUNCTION) {
						// 引数を含む関数の場合、パースした時点でhoge(1,2,3)をhoge(,1,2,3)と、
						// 最初の引数の前にカンマを1つ加えてカンマと値をセットにした形に変形してある。
						// このため、カンマと、それに続くElementをチェックする。

						// 後ろがnull、空、右括弧の場合NG
						if (afterElem == null || afterElem.getType() == ElementType.EMPTY ||
								(afterElem.getType() == ElementType.OPERATOR &&
									afterElem.getOperator() == Operator.RIGHT_PARENTHESIS)) {
							return Element.newElement(ElementType.ERROR, ErrorType.INVALID_FORMULA);
						}
						// 後ろがカンマだったらNG
						if (afterElem.getType() == ElementType.OPERATOR && afterElem.getOperator() == Operator.COMMA) {
							return Element.newElement(ElementType.ERROR, ErrorType.INVALID_FORMULA);
						}
					} else {
						// カンマは関数のカッコの中にしか存在できない。NG
						return Element.newElement(ElementType.ERROR, ErrorType.INVALID_FORMULA);
					}
					break;
				case RIGHT_PARENTHESIS:
					// この処理は関数あるいは左括弧を引数に呼び出されたか？
					if (opType == InvokerOpType.FUNCTION || opType == InvokerOpType.LEFT_PARENTHESIS) {
						// カッコ内の処理終了。OK。
						indexInfo.endIndex = i;
						return null;
					} else {
						return Element.newElement(ElementType.ERROR, ErrorType.INVALID_FORMULA);
					}

				default:
					// 処理されないオペレータがあってはならない
					assert false: "処理されないオペレータ" + checkElem.getOperator().toString();

				}
				break;
			default:
				// 処理されていないタイプがあってはならない
				assert false: "処理されないタイプ" + checkElem.getType().toString();
			}
		}
		// opTypeはnull(=再帰呼び出しでない、最初の関数呼び出し)か？
		if (opType == null) {
			// 最後までチェックをパスした。OK。
			return null;
		} else {
			// 関数あるいは左括弧による再帰呼び出しの最中に式の右端に到達した…左括弧が足りない。NG。
			return Element.newElement(ElementType.ERROR, ErrorType.INVALID_FORMULA);
		}
	}


	/**
	 * 算術演算子の前後にあるエレメントが算術演算を行う上で正しい種類のものかチェックします。
	 * @param beforeElem 演算子の前(左辺)のエレメント
	 * @param afterElem 演算子の後ろ(右辺)のエレメント
	 * @return 算術演算可能なエレメントである場合true、そうでない場合false
	 */
	private static boolean isSuitableElementsOfArithmetic(Element beforeElem, Element afterElem) {
		return 	(
					beforeElem.getType() == ElementType.NUMBER ||
					beforeElem.getType() == ElementType.REFERENCE ||
					beforeElem.getType() == ElementType.FUNCTION ||
					beforeElem.getType() == ElementType.BOOLEAN ||
					(beforeElem.getType() == ElementType.OPERATOR && beforeElem.getOperator() == Operator.RIGHT_PARENTHESIS)
				) &&
				(
					afterElem.getType() == ElementType.NUMBER ||
					afterElem.getType() == ElementType.REFERENCE ||
					afterElem.getType() == ElementType.FUNCTION ||
					afterElem.getType() == ElementType.BOOLEAN ||
					(afterElem.getType() == ElementType.OPERATOR && afterElem.getOperator() == Operator.LEFT_PARENTHESIS) ||
					(afterElem.getType() == ElementType.OPERATOR && afterElem.getOperator() == Operator.UNARY_PLUS) ||
					(afterElem.getType() == ElementType.OPERATOR && afterElem.getOperator() == Operator.UNARY_MINUS)
				);
	}

	/**
	 * 比較演算子の前後にあるエレメントが比較演算を行う上で正しい種類のものかチェックします。
	 * @param beforeElem 演算子の前(左辺)のエレメント
	 * @param afterElem 演算子の後ろ(右辺)のエレメント
	 * @return 比較演算可能なエレメントである場合true、そうでない場合false
	 */
	private static boolean isSuitableElementsOfComparison(Element beforeElem, Element afterElem) {
		return 	(
					beforeElem.getType() == ElementType.NUMBER ||
					beforeElem.getType() == ElementType.REFERENCE ||
					beforeElem.getType() == ElementType.FUNCTION ||
					beforeElem.getType() == ElementType.BOOLEAN ||
					(beforeElem.getType() == ElementType.OPERATOR && beforeElem.getOperator() == Operator.RIGHT_PARENTHESIS)
				) &&
				(
					afterElem.getType() == ElementType.NUMBER ||
					afterElem.getType() == ElementType.REFERENCE ||
					afterElem.getType() == ElementType.FUNCTION ||
					afterElem.getType() == ElementType.BOOLEAN ||
					(afterElem.getType() == ElementType.OPERATOR && afterElem.getOperator() == Operator.LEFT_PARENTHESIS) ||
					(afterElem.getType() == ElementType.OPERATOR && afterElem.getOperator() == Operator.UNARY_PLUS) ||
					(afterElem.getType() == ElementType.OPERATOR && afterElem.getOperator() == Operator.UNARY_MINUS)
				);
	}

	/**
	 * 文字列演算子の前後にあるエレメントが文字列演算を行う上で正しい種類のものかチェックします。
	 * @param beforeElem 演算子の前(左辺)のエレメント
	 * @param afterElem 演算子の後ろ(右辺)のエレメント
	 * @return 文字列 演算可能なエレメントである場合true、そうでない場合false
	 */
	private static boolean isSuitableElementsOfString(Element beforeElem, Element afterElem) {
		return 	(
					beforeElem.getType() == ElementType.NUMBER ||
					beforeElem.getType() == ElementType.REFERENCE ||
					beforeElem.getType() == ElementType.FUNCTION ||
					beforeElem.getType() == ElementType.BOOLEAN ||
					beforeElem.getType() == ElementType.STRING ||
					(beforeElem.getType() == ElementType.OPERATOR && beforeElem.getOperator() == Operator.RIGHT_PARENTHESIS)
				) &&
				(
					afterElem.getType() == ElementType.NUMBER ||
					afterElem.getType() == ElementType.REFERENCE ||
					afterElem.getType() == ElementType.FUNCTION ||
					afterElem.getType() == ElementType.BOOLEAN ||
					afterElem.getType() == ElementType.STRING ||
					(afterElem.getType() == ElementType.OPERATOR && afterElem.getOperator() == Operator.LEFT_PARENTHESIS) ||
					(afterElem.getType() == ElementType.OPERATOR && afterElem.getOperator() == Operator.UNARY_PLUS) ||
					(afterElem.getType() == ElementType.OPERATOR && afterElem.getOperator() == Operator.UNARY_MINUS)
				);
	}

	/**
	 * Element配列の、指定されたインデックス位置の前のElementを返します。
	 * 指定されたインデックスが0以下である場合、nullを返します。
	 * @param infixElem Element配列
	 * @param index 現在注目しているインデックス位置
	 * @return 指定されたインデックス位置の1つ前のElement
	 */
	private static Element getBeforeElem(Element[] infixElem, int index) {
		if (index < 1) {
			return null;
		} else {
			return infixElem[index-1];
		}
	}

	/**
	 * Element配列の、指定されたインデックス位置の後ろのElementを返します。
	 * 指定されたインデックス位置が配列の最大要素番号以上の場合、nullを返します。
	 * @param infixElem Element配列
	 * @param index 現在注目しているインデックス位置
	 * @return 指定されたインデックス位置の1つ後ろのElement
	 */
	private static Element getAfterElem(Element[] infixElem, int index) {
		if (infixElem.length-1 <= index ) {
			return null;
		} else {
			return infixElem[index+1];
		}
	}


	/**
	 * 中置記法の計算式を逆ポーランド記法に変換する
	 * @param parsedTokens トークンごとに分割された中置記法の文字列配列
	 * @param mc MathContextオブジェクト
	 * @return 逆ポーランド記法の文字列配列
	 */
	static Element[] infixToRPN(Element[] parsedTokens, MathContext mc) {
		Element token;
		List<Element> result = new ArrayList<Element>();
		Stack<Element> stack = new Stack<Element>();

		// トークンがあるだけループ
		for (int i = 0; i < parsedTokens.length; i++) {

			// トークンを取り出す
			token = parsedTokens[i];

			// トークンのタイプ別に処理
			switch (token.getType()) {
			// トークンが数値、文字列、ブーリアン、セル参照、グループ参照だったら
			case NUMBER:
			case STRING:
			case BOOLEAN:
			case REFERENCE:
			case GROUP_REFERENCE:
			case TABLE_REFERENCE:
				// トークンを結果バッファに追加
				result.add(token);
				break;

			// トークンが関数あるいはオペレータだったら
			case FUNCTION:
			case OPERATOR:
				processOperator(token, stack, result);
				break;

			default:
				assert false:"処理されないトークンのタイプがあってはならない " + token.getType().toString();
			}
		}
		// スタックに残っている演算子を結果バッファに追加
		while (!stack.empty()) {
			// スタックにUSE_LAST_RVALUEが含まれる場合があるためこのメソッドを使う
			popAndAdd(stack, result);

		}
		// 処理結果をElement型の配列にして返す
		Element[] retStrArray = new Element[result.size()];
		retStrArray = result.toArray(retStrArray);
		return retStrArray;

	}


	/**
	 * 中置記法の計算式を逆ポーランド記法に変換する処理のうち、トークンが関数あるいはオペレータだった場合の処理
	 * @param token 処理するトークン
	 * @param stack 作業用スタック
	 * @param result 結果バッファ
	 */
	private static void processOperator(Element token, Stack<Element> stack, List<Element> result) {

		if (token.getType() == ElementType.FUNCTION) {
			// 関数の場合、無条件にスタックに積む
			stack.add(token);
			return;
		}

		// オペレータの場合、種類に応じて処理
		switch(token.getOperator()) {
		case LEFT_PARENTHESIS:
			// 左括弧の場合、無条件にスタックに積む
			stack.add(token);
			break;

		case RIGHT_PARENTHESIS:
			// 右括弧の場合
			processRightparenthesis(stack, result);
			break;

		case COMMA:
			// カンマの場合
			processComma(token, stack, result);
			break;

		default:
			// それ以外の場合、演算子、あるいは関数
			// 演算子スタックが空の場合、トークンをスタックに追加して終了
			if (stack.empty()) {
				stack.push(token);
				break;
			}

			Operator tokenOp = token.getOperator();

			// トークンが四則演算子、比較演算子、文字列演算子のいずれかの場合
			switch(tokenOp) {
			case PLUS:
			case MINUS:
			case TIMES:
			case DIVIDE:
			case POWER:
			case CONCATENATE:
			case LESS_THAN:
			case LESS_EQUAL:
			case EQUALS:
			case NOT_EQUALS:
			case GRATOR_THAN:
			case GRATER_EQUAL:

				Operator peekedOp = stack.peek().getOperator();

				// まず、スタック最上段のオペレータが単項演算子+または-である場合
				// スタックにある単項演算子+または-を結果バッファに追加する
				if (peekedOp == Operator.UNARY_PLUS ||
						peekedOp == Operator.UNARY_MINUS) {
					// スタックが空になるか、単項演算子+または-以外のオペレータが現れるまで繰り返す
					while (!stack.empty() &&
							(peekedOp == Operator.UNARY_PLUS || peekedOp == Operator.UNARY_MINUS)) {
						result.add(stack.pop());
						if (!stack.isEmpty()) {
							peekedOp = stack.peek().getOperator();
						}
					}
				}

				// 次に、スタック最上段の演算子の優先順位がトークンの演算子より高いか等しい場合
				boolean appendUseLastRValue = false;
				if (!stack.empty() &&
					(peekedOp.evalPriority(tokenOp) >= 0)) {
					// スタック最上段の演算子を結果バッファに追加
					// スタック最上段に演算子の優先順位が低いオペレータが現れるまで続ける
					do {
						// スタック最上段およびトークンが共に比較演算子である場合、1=2=3のような式である。
						// この場合2つ目の比較演算子(この処理におけるトークン側の比較演算子)は、最初の比較演算子の計算結果ではなく
						// 直前の計算で使用した右辺の値を使う必要がある。
						if (stack.peek().getOperator().isComparisonOperator() && tokenOp.isComparisonOperator()) {
							appendUseLastRValue = true;
						}
						// スタックにUSE_LAST_RVALUEが含まれる場合があるためこのメソッドを使う
						popAndAdd(stack, result);
					} while(!stack.empty() && stack.peek().getOperator().evalPriority(tokenOp) >= 0);
				}

				// トークンをスタックに追加
				// 必要に応じてUSE_LAST_RVALUEを先に追加する
				if (appendUseLastRValue) {
					stack.push(Element.newElement(ElementType.OPERATOR, Operator.USE_LAST_RVALUE));
				}
				stack.push(token);

				break;
			default:
				// そうでない場合、トークンをスタックに追加
				stack.push(token);
			}
		}

	}

	/** stackからElementを取り出し、resultに追加します。
	 * もしstackの上から2番目のオペレータUSE_LAST_RVALUEだった場合、これも取り出した上で、先にresultに追加します。
	 * @param stack 作業用スタック
	 * @param result 結果バッファ
	 */
	private static void popAndAdd(Stack<Element> stack, List<Element> result) {
		Element poppedElem = stack.pop();
		if (!stack.empty() && poppedElem.getType() == ElementType.OPERATOR && poppedElem.getOperator().isComparisonOperator()) {
			Element peekedElem = stack.peek();
			if (peekedElem.getType() == ElementType.OPERATOR && peekedElem.getOperator() == Operator.USE_LAST_RVALUE) {
				result.add(stack.pop());
			}
		}
		result.add(poppedElem);
	}

	/**
	 * オペレータ処理のうち、トークンが右括弧だった場合の処理
	 * @param stack 作業用スタック
	 * @param result 結果バッファ
	 */
	private static void processRightparenthesis (Stack<Element> stack, List<Element> result) {
		// 右括弧の場合、スタックにある左括弧あるいは関数までのオペレータを結果バッファに追加
		// 右括弧は捨てる
		while(true) {
			if (stack.size() == 0) {
				// 左右の括弧の数が合わない…エラー。checkInfixNotationメソッドでチェック済みのはず。
				assert false: "invalid parenthesis.";
			}

			// 取り出したのは左括弧、あるいは関数か？
			Element poppedElem = stack.pop();
			if (poppedElem.getType() == ElementType.FUNCTION) {
				// 関数の場合、それを結果バッファに追加してループから抜ける
				result.add(poppedElem);
				break;
			} else if (poppedElem.getOperator() == Operator.LEFT_PARENTHESIS) {
				// 左括弧の場合、それを捨ててループから抜ける
				break;
			} else {
				// それ以外のオペレータの場合、結果バッファに追加してループを継続する
				result.add(poppedElem);
			}
		}

	}

	/**
	 * オペレータ処理のうち、トークンがカンマだった場合の処理
	 * @param token トークン
	 * @param stack 作業用スタック
	 * @param result 結果バッファ
	 */
	private static void processComma(Element token, Stack<Element> stack, List<Element> result) {
		// カンマの場合、スタックにあるカンマ又は関数までのオペレータを結果バッファに追加
		// スタックにあったカンマ又は関数はそのまま残しておき、さらにトークンのカンマをスタックに積む
		while(true) {
			if (stack.empty()) {
				// 不正な位置にカンマがあった…エラー。checkInfixNotationメソッドでチェック済みのはず。
				assert false: "invalid COMMA.";
			}

			// スタックにあるのは関数又はカンマか ？
			Element peekedElem = stack.peek();
			if (peekedElem.getType() == ElementType.FUNCTION || peekedElem.getOperator() == Operator.COMMA) {
				// そうである場合、トークン(カンマ)をスタックに積む
				stack.push(token);
				break;
			} else {
				// そうでない場合、スタック最上段の要素を結果バッファに追加
				result.add(stack.pop());
			}
		}

	}
}
