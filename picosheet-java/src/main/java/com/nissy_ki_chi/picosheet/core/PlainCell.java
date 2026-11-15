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
public class PlainCell extends Cell implements Comparable<Cell> {

	/**
	 * セル名
	 */
	private String _name;

	/**
	 * セルのラベル
	 */
	private String _label;

	/**
	 * このセルを持っているSheetオブジェクトへの参照
	 */
	private Sheet _sheet;

	/**
	 * セル名、シートを指定してオブジェクトを作成します
	 *
	 * @param cellName セル名
	 * @param sheet このセルが属するシートオブジェクト
	 * @throws IllegalArgumentException セル名が正しくない場合
	 */
	PlainCell(String cellName, Sheet sheet) throws IllegalArgumentException {
		this(cellName, sheet, false);
	}

	/**
	 * セル名、シートを指定してオブジェクトを作成します
	 * @param cellName セル名
	 * @param sheet このセルが属するシートオブジェクト
	 * @param createForTableCell テーブル用のセルを作成する場合true、そうでない場合falseを指定
	 * @throws IllegalArgumentException
	 *             セル名が正しくない場合
	 */
	PlainCell(String cellName, Sheet sheet, boolean createForTableCell) throws IllegalArgumentException {

		// createForTableCellによってTable用の名前チェックを行うか否か決定する
		if (createForTableCell) {
			if (!Table.isValidTableNameWithAddress(cellName)) {
				throw new IllegalArgumentException("invalid cell name for Table \"" + cellName + "\"");
			}
		} else {
			if (!isValidCellName(cellName)) {
				throw new IllegalArgumentException("invalid cell name \"" + cellName + "\"");
			}
		}

		// メンバに値をセット
		this._name = cellName;
		this._label = EMPTY_LABEL;
		this._formula = null;
		this._value = emptyElement;
		this._sheet = sheet;
	}

	/**
	 * このセルの名前を返します
	 *
	 * @return セルの名前
	 */
	@Override
	public String getName() {
		return _name;
	}

	/**
	 * このセルのラベルを返します
	 * @return セルのラベル
	 */
	@Override
	public String getLabel() {
		return _label;
	}

	/**
	 * このセルの完全修飾名を返します
	 * @return セルの完全修飾名
	 */
	@Override
	public String getFullyQualifiedName() {
		return _sheet.getName() + "!" + _name;
	}

	/**
	 * このセルを持っているシートへの参照を返します
	 *
	 * @return このセルを持っているシートへの参照
	 */
	@Override
	public Sheet getSheet() {
		return this._sheet;
	}

	/**
	 * セル名をセットします
	 *
	 * @param cellName 新しいセル名
	 * @throws IllegalArgumentException セル名として正しくない名前が渡された場合
	 */
	@Override
	void setName(String cellName) {
		setName(cellName, false);
	}

	/**
	 * セル名をセットします
	 *
	 * @param cellName 新しいセル名
	 * @param createForTableCell テーブル用のセルを作成する場合true、そうでない場合falseを指定
	 * @throws IllegalArgumentException セル名として正しくない名前が渡された場合
	 */
	@Override
	void setName(String cellName, boolean createForTableCell) throws IllegalArgumentException {
		// 正しいセル名かチェック
		// createForTableCellによってTable用の名前チェックを行うか否か決定する
		if (createForTableCell) {
			if (!Table.isValidTableNameWithAddress(cellName)) {
				throw new IllegalArgumentException("invalid cell name for Table \"" + cellName + "\"");
			}
		} else {
			if (!isValidCellName(cellName)) {
				throw new IllegalArgumentException("invalid cell name \"" + cellName + "\"");
			}
		}
		_name = cellName;
	}

	/**
	 * ラベルをセットします
	 * @param newLabel 新しいラベル
	 * @return このセルオブジェクトへの参照
	 */
	@Override
	public Cell setLabel(String newLabel) {
		_label = newLabel;
		return this;
	}

	/* (非 Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return _name + "=" + _value.toString();
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
