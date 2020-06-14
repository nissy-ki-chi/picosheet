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
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;
import java.util.regex.Pattern;

import com.nissy_ki_chi.jpicosheet.core.Element.ElementType;


/**
 * 値の保持、他のセルの参照や計算を行う"セル"です。<br>
 *
 * セルにはいくつかの種類および状態があります。
 * セルの種類には空、数値、文字列、日時、式があります。種類の情報は<code>getCellType()</code>で得ることができます。<br>
 * セルの状態には計算必要、計算中、計算済み、エラーがあります。状態の情報は<code>getCellStatus()</code>で得ることができます。<br>
 *
 * セルの値は保持しているデータの種類や式の計算結果によって変化しますが、<code>getValue()</code>で得ることができるのはElement型オブジェクトです。
 * Element型オブジェクトはセルの値を表す汎用のデータ型です。<br>
 *
 * セルは必ずいずれかのシートに属します。アプリケーションがセルオブジェクトを単体で作成することはできず、Sheetクラスの
 * <code>addCell()</code>メソッドによって作成します。
 *
 * @author yusuke nishikawa
 *
 */
public abstract class Cell implements Comparable<Cell> {

	static {
		// 空のセル値のため、空のエレメントを作成
		emptyElement = Element.newElement(ElementType.EMPTY);
	}

	/**
	 * 空を表すエレメント
	 */
	protected static Element emptyElement;

	protected static final String EMPTY_LABEL = "";

	// セルの状態を表す
	public enum CellStatus {
		/**
		 * 再計算が必要であることを示すセルステータスです
		 */
		REQUIRE_CALCULATION,
		/**
		 * 計算中であることを示すセルステータスです。あるセルの計算処理中に参照したセルがこのステータスであった場合、一連の参照は循環参照しています。
		 */
		UNDER_CALCULATION,
		/**
		 * 計算済みであることを示すセルステータスです。
		 */
		CALCULATED,
		/**
		 * エラーであることを示すセルステータスです。このセルを参照するセルはすべてエラーとなります。
		 */
		ERROR
	}

	// セルのタイプを表す
	public enum CellType {
		/**
		 * セルが空であることを示します。セルの作成直後はこのタイプになっています。
		 */
		EMPTY,
		/**
		 * セルが計算式を持っていることを示します。
		 */
		FORMULA,
		/**
		 * セルが数値を持っていることを示します。
		 */
		NUMBER,
		/**
		 * セルが文字列を持っていることを示します。
		 */
		STRING,
		/**
		 * セルが論理値を持っていることを示します。
		 */
		BOOLEAN,
		/**
		 * セルが日付値を持っていることを示します。
		 */
		DATE
	}

	/**
	 * 式(中置記法)
	 */
	protected String _formula;

	/**
	 * 式(逆ポーランド式、Element配列)
	 */
	protected Element[] _formulaRPN;

	/**
	 * このセルのタイプ
	 */
	protected CellType _cellType = CellType.EMPTY;
	/**
	 * このセルの状態
	 */
	protected CellStatus _status = CellStatus.CALCULATED;

	/**
	 * このセルの値
	 */
	protected Element _value;

	/**
	 * セル名のパターン文字列
	 */

	protected static final String CELL_NAME_PATTERN = "[a-zA-Z_][a-zA-Z0-9_]*";

	/**
	 * セル名の正規表現パターン
	 */
	protected static Pattern _cellNamePattern = Pattern.compile(CELL_NAME_PATTERN);

	/**
	 * 完全修飾セル名のパターン文字列
	 */
	protected static final String FULLY_QUALIFIED_CELL_NAME_PATTERN = Sheet.SHEET_NAME_PATTERN + "!" + "[a-zA-Z_][a-zA-Z0-9_]*";

	/**
	 * 完全修飾セル名の正規表現パターン
	 */
	protected static Pattern _fullyQualifiedCellNamePattern = Pattern.compile(FULLY_QUALIFIED_CELL_NAME_PATTERN);

	/**
	 * 式を返します。返される文字列にはこの文字列が式であることを明示する"="が先頭に付けられます。<br>
	 * このセルが CellType.FORMULA でない場合、nullを返します。
	 *
	 * @return 式の文字列。セルが CellType.FORMULA でない場合、null
	 */
	public String getFormula() {

		if (_cellType == CellType.FORMULA) {
			return "=" + _formula;
		} else {
			return null;
		}
	}

	/**
	 * 式を逆ポーランド記法で表現したElement配列を返します
	 *
	 * @return 式の逆ポーランド表現
	 */
	Element[] getFormulaRPN() {
		return _formulaRPN;
	}

	/**
	 * このセルの名前を返します
	 *
	 * @return セルの名前
	 */
	public abstract String getName();

	/**
	 * このセルのラベルを返します
	 * @return セルのラベル
	 */
	public abstract String getLabel();
	/**
	 * このセルの完全修飾名を返します
	 * @return セルの完全修飾名
	 */
	public abstract String getFullyQualifiedName();

	/**
	 * このセルを持っているシートへの参照を返します
	 *
	 * @return このセルを持っているシートへの参照
	 */
	public abstract Sheet getSheet();

	/**
	 * このセルのステータスを返します
	 *
	 * @return このセルのステータス
	 */
	public CellStatus getStatus() {
		return _status;
	}

	/**
	 * このセルの値を文字列として返します
	 *
	 * @return セルの値の文字列表現
	 */
	public String getValueString() {
		switch(_value.getType()) {
		case EMPTY:
			return "";
		case BOOLEAN:
			return _value.getBoolean().toString();
		case DATE:
			return Element.DEFAULT_DATETIME_FORMAT.format(
					new Date(_value.getNumber().longValue() - TimeZone.getDefault().getRawOffset()));
		case ERROR:
			return _value.toString();
		case NUMBER:
			return _value.getNumber().toString();
		case STRING:
			return _value.getString();
		default:
			assert false: "unexpected value type: " + _value.toString();
		return "";
		}
	}

	/**
	 * このセルの値のタイプを返します
	 *
	 * @return このセルの、現在の値のタイプ
	 */
	public ElementType getValueType() {
		return this._value.getType();
	}

	/**
	 * このセルのタイプを返します
	 *
	 * @return このセルの、現在のタイプ
	 */
	public CellType getCellType() {
		return this._cellType;
	}

	/**
	 * セルの値を返します
	 *
	 * @return セルの値
	 */
	public Element getValue() {
		return _value;
	}

	/**
	 * 式を逆ポーランド記法で表現したElement配列をセットします
	 *
	 * @param formulaRPN
	 *            式の逆ポーランド表現
	 */
	void setFormulaRPN(Element[] formulaRPN) {
		this._formulaRPN = formulaRPN;
	}


	/**
	 * セル名をセットします
	 *
	 * @param cellName 新しいセル名
	 * @throws IllegalArgumentException セル名として正しくない名前が渡された場合
	 */
	abstract void setName(String cellName);

	/**
	 * セル名をセットします
	 *
	 * @param cellName 新しいセル名
	 * @param createForTableCell テーブル用のセルを作成する場合true、そうでない場合falseを指定
	 * @throws IllegalArgumentException セル名として正しくない名前が渡された場合
	 */
	abstract void setName(String cellName, boolean createForTableCell) throws IllegalArgumentException;

	/**
	 * ラベルをセットします
	 * @param newLabel 新しいラベル
	 * @return このセルオブジェクトへの参照
	 */
	public abstract Cell setLabel(String newLabel);

	/**
	 * セルのステータスをセットします
	 *
	 * @param status
	 *            セルのステータス
	 */
	void setStatus(CellStatus status) {
		this._status = status;
	}

	/**
	 * セルの値をセットします
	 *
	 * @param value
	 *            セルの値
	 */
	void setValue(Element value) {
		this._value = value;
	}

	/**
	 * セルの値をセットします。<br>
	 * 値として渡された文字列をチェックし、適切な型の値として保存します。
	 * @param value 値の文字列表現
	 * @return このセル
	 */
	public Cell setValue(String value) {

		if (value == null || value.equals("")) {
			setValue(Cell.emptyElement);
		} else if (isNumString(value)) {
			setNumberValue(value);
		} else if (isFormulaString(value)){
			setFormula(value);
		} else if (isDateString(value)) {
			Date parsedDate = null;
			for (SimpleDateFormat sdf : Element.READABLE_DATETIME_FORMATS) {
				try {
					parsedDate = sdf.parse(value);
				} catch (ParseException e) {
					continue;
				}
				setDateValue(parsedDate);
				break;
			}
		} else {
			setStringValue(value);
		}
		// TODO:数値と式以外にも判断できないといけない

		return this;
	}

	/**
	 * このセルが式を持っており、式の中に存在しないセルへの参照がある場合、対応するセルを作成します。
	 * セル参照に対応するセルがすでに存在する場合は何も行いません。<br>
	 * セル参照が他のシートへの参照だった場合、そのシートに対しセルを作成します。ただしシートが存在しない場合は
	 * ReferenceNotFoundExceptionが発生します。<br>
	 * このセルに式以外の値がセットされていた場合、何も行いません。
	 * @return cell このセル
	 */
	public Cell createUndefinedReferenceCells() {

		// cellのタイプが式でない場合、何もしない
		if (getCellType() != CellType.FORMULA) {
			return this;
		}

		Resolver resolver = getSheet().getBook().getResolver();
		for (Element elem: getFormulaRPN()) {
			// エレメントがセル参照の場合
			if (elem.getType() == ElementType.REFERENCE) {
				String refName = elem.getCellReference();
				if (resolver.isFullyQualifiedName(refName)) {
					// セル参照が完全修飾セル名の場合、該当シートにセルを追加
					Sheet otherSheet = getSheet().getBook().getSheet(resolver.getSheetNameFromFullyQualifiedName(refName));
					otherSheet.addCell(refName);
				} else {
					// そうでない場合、このシートにセルを追加
					getSheet().addCell(refName);
				}
			}
		}

		// セルの状態を「計算必要」とし、セルの計算を行う
		this._status = CellStatus.REQUIRE_CALCULATION;
		getCalculator().calc(this);

		// 計算済み…シートのエラーセルからこのセルを削除(もしあれば)
		getSheet().removeErrorCell(this);
		return this;
	}




	/**
	 * 文字列が数値を表す文字列かどうかを返します
	 * @param numString チェックする文字列
	 * @return 文字列が数値を表す場合true、そうでない場合false
	 */
	private boolean isNumString(String numString) {
		try {
			new BigDecimal(numString);
		} catch (NumberFormatException nfe) {
			return false;
		}
		return true;
	}

	/**
	 * 文字列が式かどうかを返します
	 * @param formulaString チェックする文字列
	 * @return 文字列が式の場合true、そうでない場合false
	 */
	private boolean isFormulaString(String formulaString) {
		if (formulaString != null && formulaString.length() > 0 && formulaString.substring(0, 1).equals("=")) {
			return true;
		} else {
			return false;
		}
	}

	/**
	 * 文字列が日付形式かどうかを返します
	 * @param dateString チェックする文字列
	 * @return 文字列が日付形式の場合true、そうでない場合false
	 */
	private boolean isDateString(String dateString) {
		boolean isDateString = false;
		for (SimpleDateFormat sdf : Element.READABLE_DATETIME_FORMATS) {
			try {
				sdf.parse(dateString);
			} catch (ParseException e) {
				continue;
			}
			isDateString = true;
			break;
		}
		return isDateString;
	}

	/**
	 * セルに数値をセットします
	 *
	 * @param numString
	 *            数値の文字列表現
	 * @return このセルオブジェクトへの参照
	 */
	public Cell setNumberValue(String numString) {
		// セルのタイプを数値にセット
		this._cellType = CellType.NUMBER;
		// このセルが行っていたセル参照をすべて削除
		removeReferenceFromResolver();

		// 値をセット
		this._value = Element.newElement(ElementType.NUMBER, new BigDecimal(numString, getSheet().getMathContext()));
		// このセルを参照しているセルについて再計算が必要
		getCalculator().calc(this);

		// 計算式関連のメンバをクリア
		this.clearForumlaMembers();

		return this;

	}

	/**
	 * セルに文字列をセットします
	 *
	 * @param string
	 *            セルの値
	 * @return このセルオブジェクトへの参照
	 */
	public Cell setStringValue(String string) {
		// セルのタイプを文字列にセット
		this._cellType = CellType.STRING;
		// このセルが行っていたセル参照をすべて削除
		removeReferenceFromResolver();

		// 値をセット
		this._value = Element.newElement(ElementType.STRING,string);

		// このセルを参照しているセルについて再計算が必要
		getCalculator().calc(this);

		// 計算式関連のメンバをクリア
		this.clearForumlaMembers();

		return this;

	}

	/**
	 * セルに論理値をセットします
	 * @param bool セルの値
	 * @return このセルオブジェクトへの参照
	 */
	public Cell setBooleanValue(boolean bool) {
		// セルのタイプを論理型にセット
		this._cellType = CellType.BOOLEAN;
		// このセルが行っていたセル参照をすべて削除
		removeReferenceFromResolver();

		// 値をセット
		this._value = Element.newElement(ElementType.BOOLEAN, bool);

		// このセルを参照しているセルについて再計算が必要
		getCalculator().calc(this);

		// 計算式関連のメンバをクリア
		this.clearForumlaMembers();

		return this;
	}

	/**
	 * セルに日付をセットします
	 * @param date セルの値
	 * @return このセルオブジェクトへの参照
	 */
	public Cell setDateValue(Date date) {
		// セルのタイプを日付型にセット
		this._cellType = CellType.DATE;
		// このセルが行っていたセル参照をすべて削除
		removeReferenceFromResolver();

		// 値をセット  内部では日時をすべてGMTとして扱うためデフォルトタイムゾーンを利用して調整する
		this._value = Element.newElement(
				ElementType.DATE, date.getTime() + TimeZone.getDefault().getRawOffset());

		// このセルを参照しているセルについて再計算が必要
		getCalculator().calc(this);

		// 計算式関連のメンバをクリア
		this.clearForumlaMembers();

		return this;
	}

	/**
	 * セルに式をセットします。 式が入力されると、このセルは再計算が必要な状態となります。
	 *
	 * @param formula
	 *            式
	 * @return このセルオブジェクトへの参照
	 */
	public Cell setFormula(String formula) {
		// セルのタイプを式にセット
		this._cellType = CellType.FORMULA;

		// このセルが行っていたセル参照をすべて削除
		removeReferenceFromResolver();

		// 式の先頭に"="が付いている場合、これを取り除く
		if (formula != null && formula.length() > 0 && formula.substring(0, 1).equals("=")) {
			formula = formula.substring(1);
		}

		// 式を格納
		this._formula = formula;
		// 式をElementの配列に変換
		Element[] infixElem = FormulaParser.split(this._formula, getSheet().getMathContext(), getCalculator().getFunctionFactory());
		// 要素数1で、0番目のElementがエラーの場合、変換時にエラー
		if (infixElem.length == 1 && infixElem[0].getType() == ElementType.ERROR) {
			this._status = CellStatus.ERROR;
			this._value = infixElem[0];
			getSheet().addErrorCell(this);
			getCalculator().recalcReferencingCells(this);
			return this;
		}

		// 中置記法式として正しいかチェック
		Element errorElem = FormulaParser.checkInfixNotation(infixElem);
		// errorElemがnullでなければチェックNG
		if (errorElem != null) {
			this._status = CellStatus.ERROR;
			this._value = errorElem;
			getSheet().addErrorCell(this);
			getCalculator().recalcReferencingCells(this);
			return this;
		}

		// 式を逆ポーランド記法に変換して格納する
		this._formulaRPN = FormulaParser.infixToRPN(infixElem, getSheet().getMathContext());
		// 要素数1で、0番目のElementがエラーの場合、変換時にエラー
		if (this._formulaRPN.length == 1 && this._formulaRPN[0].getType() == ElementType.ERROR) {
			this._status = CellStatus.ERROR;
			this._value = this._formulaRPN[0];
			this._formulaRPN = null;
			getSheet().addErrorCell(this);
			getCalculator().recalcReferencingCells(this);
			return this;
		}

		// 式の中にセル参照が含まれる場合、参照していることを登録する
		Resolver resolver = getSheet().getBook().getResolver();
		for (Element elem : this._formulaRPN) {
			switch (elem.getType()) {
			case REFERENCE:
				resolver.registReferences(this, elem.getCellReference());
				break;
			case GROUP_REFERENCE:
				resolver.registGroupReferences(this, elem.getGroupReference());
				break;
			case TABLE_REFERENCE:
				resolver.registTableReferences(this, elem.getTableReference());
				break;
			default:
				// それ以外の場合は何もしない
				break;
			}
		}

		// セルの状態を「計算必要」とし、セルの計算を行う
		this._status = CellStatus.REQUIRE_CALCULATION;
		getCalculator().calc(this);

		// 計算済み…シートのエラーセルからこのセルを削除(もしあれば)
		//TODO: 上の計算処理はdisableで計算されないことがある。エラーセルからの削除はこのセルが計算済みステータス、エラーか否かを判断して行うべき。
		getSheet().removeErrorCell(this);
		return this;
	}

	/**
	 * このセルの値を空にします
	 * @return このセルオブジェクトへの参照
	 */
	public Cell setEmptyValue() {
		// セルのタイプを空にセット
		this._cellType = CellType.EMPTY;
		setValue(emptyElement);
		return this;
	}

	/**
	 * このセルにすでに式が格納されていて、その中に参照(グループ参照も含む)が含まれている場合はリゾルバからセル参照を削除します。<br>
	 */
	private void removeReferenceFromResolver() {
		//
		Resolver resolver = this.getSheet().getBook().getResolver();
		if (this._formulaRPN != null) {
			for (Element elem : this._formulaRPN) {
				switch(elem.getType()) {
				case REFERENCE:
					resolver.removeReferences(this, elem.getCellReference());
					break;
				case GROUP_REFERENCE:
					// TODO:resolver.removeGroupReferences
//					resolver.removeGroupReferences(this, elem.getGroupReference());
				}
			}
		}
	}

	/**
	 * 渡された文字列がセル名として正しいかチェックします。
	 * @param cellName チェックするセル名
	 * @return セル名として正しい場合true、そうでない場合false
	 */
	public static boolean isValidCellName(String cellName) {
		return _cellNamePattern.matcher(cellName).matches();
	}

	/**
	 * 渡された文字列が完全修飾セル名として正しいかチェックします。
	 * @param cellName チェックするセル名
	 * @return セル名として正しい場合true、そうでない場合false
	 */
	public static boolean isValidFullyQualifiedCellName(String fullyQualifiedCellName) {
		return _fullyQualifiedCellNamePattern.matcher(fullyQualifiedCellName).matches();
	}
	
	/**
	 * 計算機オブジェクトを返します
	 * @return 計算機オブジェクト
	 */
	private Calculator getCalculator() {
		return getSheet().getBook().getCalculator();
	}

	/**
	 * このセルの、式関連のメンバを初期化します。
	 */
	private void clearForumlaMembers() {
		this._formula = null;
		this._formulaRPN = null;
	}

	/* (非 Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return getName() + "=" + _value.toString();
	}

	/* (非 Javadoc)
	 * @see java.lang.Comparable#compareTo(java.lang.Object)
	 */
	public int compareTo(Cell o) {
		return (this.getFullyQualifiedName().compareTo(o.getFullyQualifiedName()));
	}

	/* (非 Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		if (obj instanceof Cell) {
			Cell c = (Cell)obj;
			if (this.getCellType() == c.getCellType()) {
				if (this.getCellType() == CellType.FORMULA) {
					for (int i = 0; i < this._formulaRPN.length; i++) {
						if (!(this._formulaRPN[i].equals(c._formulaRPN[i]))) {
							return false;
						}
					}
					return true;
				} else {
					if (this.getValueString().equals(c.getValueString())) {
						return true;
					}
				}
			}
		}
		return false;
	}

	/* (非 Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		return this.getFullyQualifiedName().hashCode();
	}
}
