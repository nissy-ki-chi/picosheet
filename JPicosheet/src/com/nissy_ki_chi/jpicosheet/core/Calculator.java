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
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.Stack;

import com.nissy_ki_chi.jpicosheet.core.Cell.CellStatus;
import com.nissy_ki_chi.jpicosheet.core.Cell.CellType;
import com.nissy_ki_chi.jpicosheet.core.Element.ElementType;
import com.nissy_ki_chi.jpicosheet.core.Element.ErrorType;
import com.nissy_ki_chi.jpicosheet.core.Element.Operator;



class Calculator {

	/**
	 * このオブジェクトを持っているBookオブジェクトへの参照
	 */
	private Book _book;
	/**
	 * 自動再計算の有効無効を示すフラグ
	 */
	private boolean _recalcEnabled;

	/**
	 * このオブジェクトが利用する関数インスタンスを生成するファクトリオブジェクトへの参照
	 */
	private FunctionFactory _functionFactory;

	/**
	 * このオブジェクトを所有するBookオブジェクトを指定して、このオブジェクトを初期化します。
	 * @param _book このオブジェクトを所有するBookオブジェクト
	 */
	public Calculator (Book book) {
		this._book = book;
		this._recalcEnabled = true;
		this._functionFactory = new FunctionFactory();
		this._functionFactory.loadBuiltinFunctions();
	}


	/**
	 * 指定したCellオブジェクトを計算します。<br>
	 * このオブジェクトを参照しているセルがある場合、それらのセルも再計算されます。<br>
	 * <br>
	 * このメソッドを呼び出すセルは、事前に書き込みロックを取得していなければなりません。
	 * @param cellname 計算キューに追加するセル名 ブック名からセル名までを含んだFull Qualified Cell Nameで表します
	 * @throws ReferenceNotFoundException
	 */
	void calc(String cellname) throws ReferenceNotFoundException {

		calc(this._book.getResolver().getCell(cellname));

	}

	/**
	 * 指定したCellオブジェクトを計算します。<br>
	 * このオブジェクトを参照しているセルがある場合、それらのセルも再計算されます。<br>
	 * <br>
	 * このメソッドを呼び出すセルは、事前に書き込みロックを取得していなければなりません。
	 * @param cell 計算キューに追加するCellオブジェクト
	 */
	void calc(Cell cell) {

		// 再計算が許可されていれば即時に再計算する
		if (this._recalcEnabled) {
			doCalc(cell);
			recalcReferencingCells(cell);
		}

	}


	/**
	 * 渡されたSetに含まれるすべてのCellオブジェクトに対して計算を行います。<br>
	 * このメソッドを呼び出すセルは、事前に書き込みロックを取得していなければなりません。
	 * @param cells 計算キューに追加するCellオブジェクトを含むSet
	 */
	void calc(Set<Cell> cells)   {
		for (Cell cell : cells) {
			calc(cell);
		}
	}

	/**
	 * 再計算を許可します。
	 */
	void recalcEnable() {

		this._recalcEnabled = true;

		// 全シートのすべてのセルを計算対象にする
		List<Cell> recalcCells = new ArrayList<Cell>();
		for (Sheet sheet: this._book.getSheets()) {
			for (Cell cell: sheet.getCells()) {
				if (cell.getCellType().equals(Cell.CellType.FORMULA)) {
					// セルのタイプが式であっても、INVALID_FORMULAエラーセルは対象外
					if (cell.getStatus() == CellStatus.ERROR && cell.getValue().getErrorType() == Element.ErrorType.INVALID_FORMULA) {
						continue;
					} else {
						cell.setStatus(Cell.CellStatus.REQUIRE_CALCULATION);
						recalcCells.add(cell);
					}
				}
			}
		}
		// 計算対象のセルを計算する
		for (Cell cell: recalcCells) {
			doCalc(cell);
		}
	}

	/**
	 * 再計算を禁止します。
	 */
	void recalcDisable() {
		// 再計算を禁止する
		this._recalcEnabled = false;
	}

	/**
	 * 再計算が有効かどうかを返します。
	 * @return 再計算が有効の場合true、そうでない場合false
	 */
	boolean isRecalcEnable() {
		return _recalcEnabled;
	}

	/**
	 * 引数に渡されたセルを参照しているセルを再計算します<br>
	 * Cellオブジェクトのコレクションを引数に取る同名のメソッドと異なり、
	 * 引数に渡されたセルを参照しているセルを再帰的に調査し、すべて再計算します。
	 * @param cell 再計算対象のセル
	 */
	void recalcReferencingCells(Cell cell) {

		// このセルを参照しているセルがある場合、それらも再計算が必要
		// このセルを参照しているすべてのセル(=再計算が必要なセル)を取得して再計算する
		Resolver resolver = this._book.getResolver();
		Set<Cell> recalcCells = resolver.getReferencingCells(cell);
		recalcReferencingCells(recalcCells);
	}

	/**
	 * コレクションに含まれるセルを再計算します。<br>
	 * @param recalcCells 再計算対象のセル
	 */
	void recalcReferencingCells(Collection<Cell> recalcCells) {
		// 再計算対象となったセルを「計算必要」ステータスにする
		for (Cell refCell : recalcCells) {
			refCell.setStatus(CellStatus.REQUIRE_CALCULATION);
		}
		// 再計算対象となったセルを計算する
		for (Cell refCell : recalcCells) {
			doCalc(refCell);
		}

	}

	/**
	 * このオブジェクトが保持しているFunctionFactoryオブジェクトを返します
	 * @return FunctionFactoryクラスのオブジェクト
	 */
	FunctionFactory getFunctionFactory() {
		return _functionFactory;
	}


	/**
	 * 渡されたセルを計算します。<br>
	 * @param cell セルオブジェクト
	 */
	private void doCalc(Cell cell) {

		// セルのタイプが式でなければ何もしない
		if (cell.getCellType() != CellType.FORMULA) {
			return;
		}
		// セルのタイプが式であっても、ステータスが「計算必要」でなければ何もしない
		if (cell.getStatus() != CellStatus.REQUIRE_CALCULATION) {
			return;
		}

		// セルの状態を計算中にする
		cell.setStatus(CellStatus.UNDER_CALCULATION);

		// 式が空の場合、セルの値も空にする
		if (cell.getFormulaRPN().length == 0) {
			cell.setEmptyValue();
			// セルの状態を計算済みにする
			cell.setStatus(CellStatus.CALCULATED);
			return;
		}

		// MathContextをSheetオブジェクトから取得
		MathContext mc = cell.getSheet().getMathContext();

		Element token;
		Stack<Element> calcStack = new Stack<Element>();
		Element lastValue = null;
		boolean useLastValue = false;

		// 逆ポーランド記法式のエレメントの数ぶんループ
		for (int i = 0; i < cell.getFormulaRPN().length; i++) {

			// 処理対象のエレメントを取り出す
			token = cell.getFormulaRPN()[i];

			// エレメントの種類に応じて処理を変える
			switch (token.getType()) {
			case NUMBER:
				// 数値の場合、計算スタックにプッシュ
				calcStack.push(token);
				break;

			case STRING:
				// 文字列の場合、計算スタックにプッシュ
				calcStack.push(token);
				break;

			case BOOLEAN:
				// ブーリアンの場合、計算スタックにプッシュ
				calcStack.push(token);
				break;

			case REFERENCE:
				// セル参照の場合、参照先セルの値を取得して計算スタックにプッシュ
				Element refCelValue = null;
				if (cell instanceof TableCell) {
					// このセルがテーブル内のセルである場合、セル参照の解決のためにテーブルオブジェクトへの参照が必要な場合がある
					refCelValue = getReferencesCellValue(token, cell.getSheet(), ((TableCell)cell).getTable());
				} else {
					refCelValue = getReferencesCellValue(token, cell.getSheet(), null);
				}
				// 得た値のタイプがエラー以外の場合
				if (refCelValue.getType() != ElementType.ERROR) {
					// 計算スタックにプッシュ
					calcStack.push(refCelValue);
				} else {
					// エラーの場合、得た値をセル値としてセットし、このセルのステータスをエラーにして計算を中断する
					cell.setValue(refCelValue);
					cell.setStatus(CellStatus.ERROR);
					return;
				}
				break;

			case GROUP_REFERENCE:
				try {
					// エレメントが示すグループが存在する場合、グループが保持するすべてのセルに対し、必要なら再計算を行う
					// TODO:セル参照のたびにCollectionを得てすべてのセルの再計算チェックするのは非効率
					Collection<Cell> cells;
					cells = _book.getResolver().getCellsFromGroup(token.getGroupReference());
					// 各セルに対し、必要に応じ再計算を行う
					for (Cell groupCell: cells) {
						recalcIfRequired(groupCell);
						// エラーの場合、得た値をセル値としてセットし、このセルのステータスをエラーにして計算を中断する
						if (groupCell.getValue().getType() == ElementType.ERROR) {
							cell.setValue(groupCell.getValue());
							cell.setStatus(CellStatus.ERROR);
							return;
						}
					}
					calcStack.push(token);
				} catch (ReferenceNotFoundException e) {
					// エレメントが示すグループが存在しなかった場合、このセルのステータスをエラーにして計算を中断する
					cell.setValue(Element.newElement(ElementType.ERROR, ErrorType.INVALID_REFERENCES));
					cell.setStatus(CellStatus.ERROR);
					return;
				}
				break;

			case TABLE_REFERENCE:
				try {
					// エレメントが示すテーブルが存在する場合、テーブル(またはテーブルの範囲)が保持するすべてのセルに対し、必要なら再計算を行う
					// TODO:セル参照のたびにCollectionを得てすべてのセルの再計算チェックするのは非効率
					Collection<Cell> cells;
					cells = _book.getResolver().getCellsFromTable(token.getTableReference());
					// 各セルに対し、必要に応じ再計算を行う
					for (Cell groupCell: cells) {
						recalcIfRequired(groupCell);
						// エラーの場合、得た値をセル値としてセットし、このセルのステータスをエラーにして計算を中断する
						if (groupCell.getValue().getType() == ElementType.ERROR) {
							cell.setValue(groupCell.getValue());
							cell.setStatus(CellStatus.ERROR);
							return;
						}
					}
					calcStack.push(token);
				} catch (ReferenceNotFoundException e) {
					// エレメントが示すテーブルが存在しなかった場合、このセルのステータスをエラーにして計算を中断する
					cell.setValue(Element.newElement(ElementType.ERROR, ErrorType.INVALID_REFERENCES));
					cell.setStatus(CellStatus.ERROR);
					return;
				}
				break;

			case FUNCTION:
				operateFunction(calcStack, token, mc, _book.getResolver());
				break;
			case OPERATOR:
				// 演算子の種類に応じて処理する
				switch(token.getOperator()) {
				case USE_LAST_RVALUE:
					useLastValue = true;
					break;

				case COMMA:
					calcStack.push(token);
					break;

				case PLUS:
				case MINUS:
				case TIMES:
				case DIVIDE:
				case POWER:
				case UNARY_PLUS:
				case UNARY_MINUS:
				case LEFT_PARENTHESIS:
				case RIGHT_PARENTHESIS:
					lastValue = operateArithmetic(calcStack, token, mc, _book.getResolver());
					useLastValue = false;
					break;

				case LESS_THAN:
				case LESS_EQUAL:
				case NOT_EQUALS:
				case EQUALS:
				case GRATOR_THAN:
				case GRATER_EQUAL:
					if (useLastValue) {
						lastValue = operateComparison(calcStack, token, mc, _book.getResolver(), lastValue);
					} else {
						lastValue = operateComparison(calcStack, token, mc, _book.getResolver(), null);
					}
					useLastValue = false;
					break;

				case CONCATENATE:
					lastValue = operateString(calcStack, token, mc, _book.getResolver());
					useLastValue = false;
					break;

				default:
					// すべてのオペレータはなんらかの処理がされなければならない。
					assert false: "処理されないオペレータ：" + token.toString();
				}
				break;
			default:
				// すべてのエレメントはなんらかの処理がされなければならない。
				assert false: "処理されないエレメント：" + token.toString();
			}
		}

		// この時点で計算スタック中のElement数が１でなければならない
		assert calcStack.size() == 1:"計算終了時点で計算スタック中のElement数が1でなければならない";

		// 最後に残ったものが計算結果
		cell.setValue(calcStack.pop());

		// セルの状態を計算済みにする
		cell.setStatus(CellStatus.CALCULATED);
	}

	/**
	 * 渡されたReference型エレメントが示す参照先セルから値を取得します
	 * @param referenceElement 参照するセルを示したElement
	 * @param referencesSheet 参照されるシートオブジェクト。nullの場合デフォルトシート。
	 * @param defaultTable referenceElementの参照先がセルアドレス形式の場合に参照するテーブルオブジェクト
	 * @return 参照先セルの値
	 */
	private Element getReferencesCellValue(Element referenceElement, Sheet referencesSheet, Table defaultTable) {

		// 参照するセルオブジェクトを得る
		Cell referCell;
		referCell = getReferencesCell(referenceElement, referencesSheet, defaultTable);
		if (referCell == null) {
			return Element.newElement(ElementType.ERROR, ErrorType.INVALID_REFERENCES);
		}

		// このセルが計算必要ステータスである場合、再計算する
		recalcIfRequired(referCell);

		// 参照するセルの値を得る
		return referCell.getValue();
	}

	/**
	 * 渡されたReference型エレメントが示す参照先セルを取得します。<br>
	 * セルが見つからなかった場合nullを返します
	 * @param referenceElement 参照するセルを示したElement
	 * @param referencesSheet 参照されるシートオブジェクト。nullの場合デフォルトシート。
	 * @param defaultTable referenceElementの参照先がセルアドレス形式の場合に参照するテーブルオブジェクト
	 * @return 参照先セル。参照先セルが見つからなかった場合null。
	 */
	private Cell getReferencesCell(Element referenceElement, Sheet referencesSheet, Table defaultTable) {
		// 参照するセルオブジェクトを得る
		Cell referCell;
		try {
			String referenceCellName;
			if (defaultTable == null && 
					!Cell.isValidFullyQualifiedCellName(referenceElement.getCellReference())) {
				referenceCellName = referencesSheet.getName() + "!" + referenceElement.getCellReference();
			} else {
				referenceCellName = referenceElement.getCellReference();
			}
			referCell = this._book.getResolver().getCell(referenceCellName, defaultTable);
		} catch (ReferenceNotFoundException e) {
			// 参照先が見つからない場合、nullを返す
			return null;
		} catch (IllegalStateException e) {
			// referenceElementがセル参照で無いことは無い
			throw new AssertionError(e.getMessage());
		}
		return referCell;
	}


	/**
	 * 渡されたセルのステータスが計算必要である場合、そのセルに対し計算を行います。<br>
	 * 渡されたセルのステータスが計算中であった場合、そのセルに対し循環参照エラーをセットします。
	 * @param cell 対象とするセル
	 */
	private void recalcIfRequired(Cell cell) {
		// 参照するセルのステータスが計算中の場合、循環参照している
		if (cell.getStatus() == CellStatus.UNDER_CALCULATION) {
			// 循環参照エラーの値をセットする
			cell.setValue(Element.newElement(ElementType.ERROR, ErrorType.CIRCULAR_REFERENCE));
			cell.setStatus(CellStatus.ERROR);
		}

		// 参照するセルのステータスが計算必要の場合、計算を行う
		if (cell.getStatus() == CellStatus.REQUIRE_CALCULATION) {
			doCalc(cell);
		}
	}


	/**
	 * 算術演算を行います。
	 * @param calcStack 計算用スタックオブジェクト
	 * @param operatorElem 演算子エレメント
	 * @param mc MathContextオブジェクト
	 * @param resolver リゾルバオブジェクト
	 * @return 演算結果
	 */
	private Element operateArithmetic(Stack<Element> calcStack, Element operatorElem, MathContext mc, Resolver resolver) {

		// operatorElemは算術演算子のはず
		assert (operatorElem.getType() == ElementType.OPERATOR) && operatorElem.getOperator().isArithmeticOperator(): "不正な演算子エレメント：" + operatorElem.toString();

		BigDecimal resultValue;

		switch (operatorElem.getOperator()) {
		case PLUS:
		case MINUS:
		case TIMES:
		case DIVIDE:
		case POWER:
		{
			// スタックから取り出す際は右辺→左辺の順に取り出される。他のオペレータの場合も同様。
			Element rElem = calcStack.pop();
			Element lElem = calcStack.pop();
			// 両辺のElementTypeがNUMBER、BOOLEAN、Date、あるいはEMPTYでなければならない
			if ((rElem.getType() == ElementType.NUMBER ||
					rElem.getType() == ElementType.BOOLEAN ||
					rElem.getType() == ElementType.DATE ||
					rElem.getType() == ElementType.EMPTY) &&
					(lElem.getType() == ElementType.NUMBER ||
					lElem.getType() == ElementType.BOOLEAN ||
					lElem.getType() == ElementType.DATE ||
					lElem.getType() == ElementType.EMPTY)) {
				// OK。何もしない。
			} else {
				calcStack.push(Element.newElement(ElementType.ERROR, ErrorType.WRONG_VALUE));
				break;
			}

			BigDecimal rValue = rElem.getNumber();
			BigDecimal lValue = lElem.getNumber();
			ElementType newElemType = null;
			if (rElem.getType() == ElementType.DATE || lElem.getType() == ElementType.DATE) {
				newElemType = ElementType.DATE;
			} else {
				newElemType = ElementType.NUMBER;
			}
			switch (operatorElem.getOperator()) {
			case PLUS:
				resultValue = lValue.add(rValue, mc);
				calcStack.push(Element.newElement(newElemType, resultValue));
				break;
			case MINUS:
				resultValue = lValue.subtract(rValue, mc);
				calcStack.push(Element.newElement(newElemType, resultValue));
				break;
			case TIMES:
				resultValue = lValue.multiply(rValue, mc);
				calcStack.push(Element.newElement(newElemType, resultValue));
				break;
			case DIVIDE:
				if (rValue.signum() == 0) {
					//割る数が0の場合、結果を0除算エラーとする
					calcStack.push(Element.newElement(ElementType.ERROR, ErrorType.DIVIDE_BY_ZERO));
				} else {
					resultValue = lValue.divide(rValue, mc);
					calcStack.push(Element.newElement(newElemType, resultValue));
				}
				break;
			case POWER:
				resultValue = new BigDecimal(Math.pow(lValue.doubleValue(), rValue.doubleValue()));
				calcStack.push(Element.newElement(newElemType, resultValue));
				break;
			}
			break;
		}

		case UNARY_PLUS:
		case UNARY_MINUS:
		{
			Element rElem = calcStack.pop();
			// 右辺のElementTypeがNUMBERでなければならない
			if (rElem.getType() != ElementType.NUMBER) {
				calcStack.push(Element.newElement(ElementType.ERROR, ErrorType.WRONG_VALUE));
			}

			switch (operatorElem.getOperator()) {
			case UNARY_PLUS:
				// 右辺のElementTypeがNUMBERであれば、単項演算子＋は何もしない。popしたElementをそのまま返す。
				calcStack.push(rElem);
				break;
			case UNARY_MINUS:
				BigDecimal rValue = rElem.getNumber();
				resultValue = rValue.negate();
				calcStack.push(Element.newElement(ElementType.NUMBER, resultValue));
				break;
			}
			break;
		}

		default:
			// すべてのオペレータはなんらかの処理がされなければならない。
			assert false: "処理されないオペレータ：" + operatorElem.toString();
		};

		// この計算結果…スタック最上段のElement返す
		return calcStack.peek();
	}

	/**
	 * 比較演算を行います。引数lastValueにnull以外が渡された場合、比較演算の左辺にこの値を使用します。
	 * @param calcStack 計算用スタックオブジェクト
	 * @param operatorElem 演算子エレメント
	 * @param mc MathContextオブジェクト
	 * @param resolver リゾルバオブジェクト
	 * @param lastValue 左辺値として使用する値。calcStackからの値を使う場合はnullをセットする。
	 * @return 演算時に使用した右辺値
	 */
	private Element operateComparison(Stack<Element> calcStack, Element operatorElem, MathContext mc, Resolver resolver, Element lastValue) {

		// operatorElemは比較演算子のはず
		assert (operatorElem.getType() == ElementType.OPERATOR) && operatorElem.getOperator().isComparisonOperator(): "不正な演算子エレメント：" + operatorElem.toString();

		Element rElem = calcStack.pop();
		Element lElem = calcStack.pop();

		Element rOperand = rElem;
		Element lOperand = null;

		// lastValueの値により演算の左辺値を決める
		if (lastValue != null) {
			lOperand = lastValue;
		} else {
			lOperand = lElem;
		}

		// エレメントのTypeが異なるなら等しくない。計算前の右辺値を返して終了。
		if (lOperand.getType() != rOperand.getType()) {
			calcStack.push(Element.newElement(ElementType.BOOLEAN, false));
			return rElem;
		}

		// エレメントのTypeが同じなら、両者の内容を比較
		boolean result = false;
		switch (operatorElem.getOperator()) {
		case EQUALS:
			switch (lOperand.getType()) {
			case NUMBER:
				result = (lOperand.getNumber().compareTo(rOperand.getNumber()) == 0);
				break;
			case STRING:
				result = lOperand.getString().equals(rOperand.getString());
				break;
			case BOOLEAN:
				result = lOperand.getBoolean().equals(rOperand.getBoolean());
				break;
			case DATE:
				// TODO:DATE型の場合の処理を正しく実装する。今はfalse決め打ち。
				result = false;
			default:
				assert false: "不正なエレメント：" + operatorElem.toString();
			}
			break;

		case NOT_EQUALS:
			switch (lOperand.getType()) {
			case NUMBER:
				result = (lOperand.getNumber().compareTo(rOperand.getNumber()) != 0);
				break;
			case STRING:
				result = !lOperand.getString().equals(rOperand.getString());
				break;
			case BOOLEAN:
				result = !lOperand.getBoolean().equals(rOperand.getBoolean());
				break;
			case DATE:
				// TODO:DATE型の場合の処理を正しく実装する。今はfalse決め打ち。
				result = false;
			default:
				assert false: "不正なエレメント：" + operatorElem.toString();
			}
			break;

		case LESS_THAN:
			switch (lOperand.getType()) {
			case NUMBER:
				result = (lOperand.getNumber().compareTo(rOperand.getNumber()) < 0);
				break;
			case STRING:
				result = false;
				break;
			case BOOLEAN:
				result = false;
				break;
			case DATE:
				// TODO:DATE型の場合の処理を正しく実装する。今はfalse決め打ち。
				result = false;
			default:
				assert false: "不正なエレメント：" + operatorElem.toString();
			}
			break;

		case LESS_EQUAL:
			switch (lOperand.getType()) {
			case NUMBER:
				result = (lOperand.getNumber().compareTo(rOperand.getNumber()) <= 0);
				break;
			case STRING:
				result = false;
				break;
			case BOOLEAN:
				result = false;
				break;
			case DATE:
				// TODO:DATE型の場合の処理を正しく実装する。今はfalse決め打ち。
				result = false;
			default:
				assert false: "不正なエレメント：" + operatorElem.toString();
			}
			break;

		case GRATOR_THAN:
			switch (lOperand.getType()) {
			case NUMBER:
				result = (lOperand.getNumber().compareTo(rOperand.getNumber()) > 0);
				break;
			case STRING:
				result = false;
				break;
			case BOOLEAN:
				result = false;
				break;
			case DATE:
				// TODO:DATE型の場合の処理を正しく実装する。今はfalse決め打ち。
				result = false;
			default:
				assert false: "不正なエレメント：" + operatorElem.toString();
			}
			break;

		case GRATER_EQUAL:
			switch (lOperand.getType()) {
			case NUMBER:
				result = (lOperand.getNumber().compareTo(rOperand.getNumber()) >= 0);
				break;
			case STRING:
				result = false;
				break;
			case BOOLEAN:
				result = false;
				break;
			case DATE:
				// TODO:DATE型の場合の処理を正しく実装する。今はfalse決め打ち。
				result = false;
			default:
				assert false: "不正なエレメント：" + operatorElem.toString();
			}
			break;

		default:
			// すべてのオペレータはなんらかの処理がされなければならない。
			assert false: "処理されないオペレータ：" + operatorElem.toString();
		};

		// lastValueを使った演算を行った場合、先に行われた比較演算の結果…lElemの値(BOOLEAN型のはず)…とのANDを取る
		if (lastValue != null) {
			assert lElem.getType() == ElementType.BOOLEAN: "lElemがBOOLEANでない：" + lElem.toString();
			result = lElem.getBoolean().booleanValue() && result;
		}

		calcStack.push(Element.newElement(ElementType.BOOLEAN, result));

		// 計算前の右辺値を返す
		return rElem;
	}

	/**
	 * 文字列演算（現在のところ文字列結合のみ）を行います。
	 * @param calcStack 計算用スタックオブジェクト
	 * @param operatorElem 演算子エレメント
	 * @param mc MathContextオブジェクト
	 * @param resolver リゾルバオブジェクト
	 * @return 演算結果
	 */
	private Element operateString(Stack<Element> calcStack, Element operatorElem, MathContext mc, Resolver resolver) {

		// operatorElemは文字列結合演算子のはず
		assert (operatorElem.getType() == ElementType.OPERATOR) && operatorElem.getOperator().isStringOperator(): "不正な演算子エレメント：" + operatorElem.toString();

		// スタックから取り出す際は右辺→左辺の順に取り出される。他のオペレータの場合も同様。
		Element rElem = calcStack.pop();
		Element lElem = calcStack.pop();

		calcStack.push(Element.newElement(ElementType.STRING, lElem.getString() + rElem.getString()));

		// この計算結果…スタック最上段のElement返す
		return calcStack.peek();
	}

	/**
	 * 計算を行います
	 * @param calcStack 計算用スタックオブジェクト
	 * @param operatorElem 演算子エレメント
	 * @param mc MathContextオブジェクト
	 * @param resolver リゾルバオブジェクト
	 */
	private Element operateFunction(Stack<Element> calcStack, Element operatorElem, MathContext mc, Resolver resolver) {

		switch (operatorElem.getType()) {
		case FUNCTION:
			// 関数に応じて必要な引数の数が異なる。
			// スタックには0個以上のカンマが積まれている。カンマをすべてpopし、popしたカンマの数だけ
			// 引数をさらにpopして引数とする…カンマの数は、中置記法表記の関数に使われたカンマの数+1個がRPN式に入っている。
			int argCount = 0;
			while(true) {
				// 計算スタックからポップ可能で、タイプがオペレータの場合
				if (calcStack.size() > 0) {
					// 計算スタックの一番上にあるオペレータの種類をチェックする
					Element topElem = calcStack.peek();
					if (topElem.getType() == ElementType.OPERATOR && topElem.getOperator() == Operator.COMMA) {
						// カンマだった場合、argCount数に+1し、計算スタックからポップし捨てる
						argCount++;
						calcStack.pop();
					} else {
						// カンマ以外の場合、argCount数が確定
						break;
					}
				} else {
					// スタックが空の場合、argCount数が確定
					break;
				}
			}

			// argCountの数だけスタックから値を取得する
			Element[] args = new Element[argCount];
			for (int i = argCount-1; 0 <= i; i--) {
				args[i] = calcStack.pop();
			}
			// 関数を実行し、結果をスタックにプッシュ
			Function func = _functionFactory.getFunctionInstance(operatorElem.getString());
			if (func == null) {
				// 関数インスタンスが見つからなかった場合、エラー
				calcStack.push(Element.newElement(ElementType.ERROR, ErrorType.INVALID_FORMULA));
			} else {
				calcStack.push(func.call(args, mc, resolver));
			}
			break;
		}

		// 常に空のElementを返す
		return Element.newElement(ElementType.EMPTY);
	}

}

