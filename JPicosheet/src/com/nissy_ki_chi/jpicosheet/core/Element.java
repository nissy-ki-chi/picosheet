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
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;
import java.util.TimeZone;
import java.util.WeakHashMap;


/**
 * 計算式の各要素を表す不変(immutable)のオブジェクトです。<br>
 * CellクラスからgetValue()で値を取得した際に渡されるのもこのクラスのオブジェクトです。
 * @author yusuke nishikawa
 *
 */
public class Element {

	/**
	 * このエレメントの型を表します
	 * @author yusuke nishikawa
	 *
	 */
	public enum ElementType {
		/**
		 * 空を表します。
		 */
		EMPTY,
		/**
		 * 数値を表します。<code>getNumber()<code>でBigDecimal型のオブジェクトを得ることができます。
		 */
		NUMBER,
		/**
		 *文字列を表します。<code>getString()<code>でString型のオブジェクトを得ることができます。
		 */
		STRING,
		/**
		 * 日時を表します。
		 */
		DATE,
		/**
		 * 論理値を表します。
		 */
		BOOLEAN,
		/**
		 * エラーを表します。<code>getErrorType()<code>でErrorType型の列挙型オブジェクトを得ることができます。
		 */
		ERROR,
		/**
		 * オペレータを表します。<code>getOperator()<code>でOperator型の列挙型オブジェクトを得ることができます。
		 */
		OPERATOR,
		/**
		 * 関数を表します。
		 */
		FUNCTION,
		/**
		 * セル参照を表します。
		 */
		REFERENCE,
		/**
		 * グループ参照を表します。
		 */
		GROUP_REFERENCE,
		/**
		 * テーブル参照を表します。
		 */
		TABLE_REFERENCE
	}

	/**
	 * エレメントの型がErrorの場合に、エラーの種類を表します
	 * @author yusuke nishikawa
	 *
	 */
	public enum ErrorType {
		/**
		 * ゼロ除算
		 */
		DIVIDE_BY_ZERO,
		/**
		 * 不正な値
		 */
		WRONG_VALUE,
		/**
		 * 不正な参照
		 */
		INVALID_REFERENCES,
		/**
		 * 不正な式
		 */
		INVALID_FORMULA,
		/**
		 * 不正な名前
		 */
		UNRECOGNIZED_NAME,
		/**
		 * 不正な数値
		 */
		INVALID_NUMBER,
		/**
		 * 有効でない値
		 */
		NOT_AVAILABLE_VALUE,
		/**
		 * 循環参照
		 */
		CIRCULAR_REFERENCE,

		/**
		 * 不正なパラメータ
		 */
		INVALID_PARAMETER
	}

	/**
	 * オペレータの種類を表します。オペレータ同士の優先順位情報と、それを比較するメソッドも持っています。
	 * @author yusuke nishikawa
	 *
	 */
	public enum Operator {

		/**
		 * 計算処理指示用。直近に行われた計算結果でなく、右辺値を使う。CellのメンバformulaRPNの中にしか存在しない。
		 */
		USE_LAST_RVALUE(-1,3),
		/**
		 * カンマ
		 */
		COMMA(0, 3),
		/**
		 * 左括弧
		 */
		LEFT_PARENTHESIS(0, 3),
		/**
		 * 右括弧
		 */
		RIGHT_PARENTHESIS(0, 3),
		/**
		 * 加算
		 */
		PLUS(2, 1),
		/**
		 * 減算
		 */
		MINUS(2, 1),
		/**
		 * 乗算
		 */
		TIMES(3, 1),
		/**
		 * 除算
		 */
		DIVIDE(3, 1),
		/**
		 * べき乗
		 */
		POWER(3, 1),
		/**
		 * 単項演算子 プラス
		 */
		UNARY_PLUS(4, 1),
		/**
		 * 単項演算子 マイナス
		 */
		UNARY_MINUS(4, 1),
		/**
		 * 未満
		 */
		LESS_THAN(1, 0),
		/**
		 * 以下
		 */
		LESS_EQUAL(1, 0),
		/**
		 * イコール
		 */
		EQUALS(1, 0),
		/**
		 * ノットイコール
		 */
		NOT_EQUALS(1, 0),
		/**
		 * 以上
		 */
		GRATER_EQUAL(1, 0),
		/**
		 * より大きい
		 */
		GRATOR_THAN(1, 0),
		/**
		 * 文字列結合
		 */
		CONCATENATE(0, 2);

		static final int OPTYPE_COMPARISON = 0;
		static final int OPTYPE_ARITHMETIC = 1;
		static final int OPTYPE_STRING = 2;
		static final int OPTYPE_OTHER = 3;

		private int _priority;
		private int _opType;

		private Operator (int priority, int opType) {
			this._priority = priority;
			this._opType = opType;
		}

		/**
		 * 引数に渡されたオペレータの優先順位をこのオペレータと比較します。<br>
		 * このオペレータの優先順位のほうが高い場合1以上の値を返し、低い場合は-1以下の値を返します。<br>
		 * 2つのオペレータの優先順位が等しい場合、0を返します。
		 *
		 * @param arg1 比較するオペレータ
		 * @return オペレータの優先順位の比較結果
		 */
		public int evalPriority(Operator arg1) {
			return this._priority - arg1._priority;
		}

		/**
		 * この列挙子が比較演算子である場合、trueを返します。
		 * @return 比較演算子の場合true、そうでない場合false
		 */
		public boolean isComparisonOperator() {
			return _opType == OPTYPE_COMPARISON;
		}

		/**
		 * この列挙子が算術演算子である場合、trueを返します。
		 * @return 算術演算子の場合true、そうでない場合false
		 */
		public boolean isArithmeticOperator() {
			return _opType == OPTYPE_ARITHMETIC;
		}

		/**
		 * この演算子が文字列結合演算子である場合、trueを返します。
		 * @return 文字列結合演算子の場合true、そうでない場合false
		 */
		public boolean isStringOperator() {
			return _opType == OPTYPE_STRING;
		}

		/**
		 * この列挙子が比較演算子、算術演算子のどちらでもない場合にtrueを返します。
		 * @return 比較演算子、算術演算子のどちらでもない場合true、そうでない場合false
		 */
		public boolean isOtherOperator() {
			return _opType == OPTYPE_OTHER;
		}
	}

	/**
	 * 同一内容のエレメントを作らないようにするためのキャッシュ
	 */
	private static Map<Element, Element> _elementCache = new WeakHashMap<Element, Element>();

	/**
	 * エレメントの型
	 */
	private final ElementType _type;
	/**
	 * エレメントの値
	 */
	private final Object _value;

	/**
	 * エレメントのハッシュ値
	 */
	private int _hashCode = 0;

	/**
	 * Date型エレメントの標準日時フォーマット
	 */
	static final String DEFAULT_DATETIME_FORMAT_STRING = "yyyy/MM/dd HH:mm:ss";
	static final SimpleDateFormat DEFAULT_DATETIME_FORMAT = new SimpleDateFormat(DEFAULT_DATETIME_FORMAT_STRING);

	/**
	 * Date型エレメントがサポートする日時フォーマット
	 */
	static final SimpleDateFormat[] READABLE_DATETIME_FORMATS = {
		new SimpleDateFormat("yyyy/MM/dd HH:mm:ss.SSS"),
		new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS"),
		DEFAULT_DATETIME_FORMAT,
		new SimpleDateFormat("yyyy-MM-dd HH:mm:ss"),
		new SimpleDateFormat("yyyy/MM/dd HH:mm"),
		new SimpleDateFormat("yyyy-MM-dd HH:mm"),
		new SimpleDateFormat("yyyy/MM/dd HH"),
		new SimpleDateFormat("yyyy-MM-dd HH"),
		new SimpleDateFormat("yyyy/MM/dd"),
		new SimpleDateFormat("yyyy-MM-dd"),
		new SimpleDateFormat("yyyy/MM"),
		new SimpleDateFormat("yyyy-MM"),
		new SimpleDateFormat("HH:mm:ss.SSS"),
		new SimpleDateFormat("HH:mm:ss"),
		new SimpleDateFormat("HH:mm")
	};

	/**
	 * エレメントキャッシュ管理
	 */
	private static final Element getElementFromCashe(Element newElem) {
			// エレメントがキャッシュになければキャッシュに格納する
		if (! _elementCache.containsKey(newElem)) {
			_elementCache.put(newElem, newElem);
		}

		// キャッシュからエレメントを取り出して返す
		// このメソッドが呼ばれる以前からエレメントが存在していたなら、返されるのはキャッシュに格納されているエレメントで、
		// newElemは「呼び水」として利用したのち破棄される
		return _elementCache.get(newElem);
	}

	/**
	 * エレメントの型と値を使ってオブジェクトを初期化します
	 * 値にはエレメントの型により以下を与える必要があります。<br>
	 * <table border='1'>
	 * <tr><th>エレメント型</th><th>セットする値</th></tr>
	 * <tr><td>EMPTY</td><td>nullがセットされます。渡された値は無視されます。</td></tr>
	 * <tr><td>NUMBER</td><td>BigDecimal型オブジェクト</td></tr>
	 * <tr><td>STRING</td><td>String型オブジェクト</td></tr>
	 * <tr><td>DATE	</td><td>1970年1月1日0時0分0秒 GMT からのミリ秒を表すLong型オブジェクトもしくはBigDecimal型オブジェクト</td></tr>
	 * <tr><td>BOOLEAN</td><td>Boolean型オブジェクト</td></tr>
	 * <tr><td>ERROR</td><td>列挙型Element.ErrorTypeの値</td></tr>
	 * <tr><td>FUNCTION</td><td>String型オブジェクト</td></tr>
	 * <tr><td>OPERATOR</td><td>列挙型 OPERATOR</td></tr>
	 * <tr><td>REFERENCE</td><td>String型オブジェクト セル名として適切であること</td></tr>
	 * <tr><td>GROUP_REFERENCE</td><td>String型オブジェクト グループ名として適切であること</td></tr>
	 * <tr><td>TABLE_REFERENCE</td><td>String型オブジェクト テーブル名として適切であること</td></tr>
	 * </table>
	 * オブジェクトのセットが必要なエレメントの型の場合、指定された型のオブジェクト以外の
	 * オブジェクトをセットした場合は例外がスローされます
	 *
	 * @param elementType エレメントタイプを示す列挙型
	 * @param value 値
	 * @exception IllegalArgumentException 指定したエレメントタイプに合わない値を指定した場合
	 */
	public synchronized static Element newElement(ElementType elementType, Object value) {
		Element newElem = new Element(elementType, value);
		return getElementFromCashe(newElem);
	}

	// Element(ElementType, Object) の実装メソッド
	private Element(ElementType elementType, Object value) {

		this._type = elementType;
		switch(elementType) {
		case EMPTY:
			// 引数valueは無視する
			this._value = null;
			break;
		case NUMBER:
			if (value instanceof BigDecimal){
				this._value = value;
			} else {
				throw new IllegalArgumentException("NUMBERの場合、BigDecimal型オブジェクトが必要");
			}
			break;
		case STRING:
			if (value instanceof String){
				this._value = value;
			} else {
				throw new IllegalArgumentException("STRINGの場合、String型オブジェクトが必要");
			}
			break;
		case DATE:
			// 1970年1月1日0時0分0秒 GMTからのミリ秒を表すLong値で保持する
			if (value instanceof Long){
				this._value = value;
			} else if (value instanceof BigDecimal) {
				this._value = new Long(((BigDecimal)value).longValue());
			} else {
				throw new IllegalArgumentException("DATEの場合、Long型もしくはBigDecimal型オブジェクトが必要");
			}
			break;
		case BOOLEAN:
			if (value instanceof Boolean) {
				this._value = value;
			} else if (value instanceof String) {
				String valueStr = ((String) value).toUpperCase();
				if (valueStr.equals("TRUE")) {
					this._value = new Boolean(true);
				} else if (valueStr.equals("FALSE")) {
					this._value = new Boolean(false);
				} else {
					throw new IllegalArgumentException("BOOLEANの場合、Boolean型オブジェクトあるいは\"TRUE\"\"FALSE\"が必要");
				}
			} else {
				throw new IllegalArgumentException("BOOLEANの場合、Boolean型オブジェクトあるいは\"TRUE\"\"FALSE\"が必要");
			}
			break;
		case ERROR:
			if (value instanceof ErrorType){
				this._value = value;
			} else {
				throw new IllegalArgumentException("ERRORの場合、ErrorType型オブジェクトが必要");
			}
			break;
		case FUNCTION:
			if (value instanceof String){
				this._value = value;
			} else {
				throw new IllegalArgumentException("FUNCTIONの場合、String型オブジェクトが必要");
			}
			break;
		case OPERATOR:
			if (value instanceof Operator){
				this._value = value;
			} else {
				throw new IllegalArgumentException("OPERATORの場合、列挙型 Operatorが必要");
			}
			break;
		case REFERENCE:
			if (value instanceof String){
				// valueはアドレス付きテーブル名もしくは完全修飾アドレス付きテーブル名、セル名もしくは
				// 完全修飾セル名に準拠した名前でなければならない
				if (!Table.isValidTableNameWithAddress((String)value) &&
						!Table.isValidFullyQualifiedTableNameWithAddress((String)value) &&
						!Cell.isValidCellName((String)value) &&
						!Cell.isValidFullyQualifiedCellName((String)value)) {
					throw new IllegalArgumentException("不適切なセル名:" + (String)value);
				}
				this._value = value;
			} else {
				throw new IllegalArgumentException("Referenceの場合、String型オブジェクトが必要");
			}
			break;
		case GROUP_REFERENCE:
			// valueはグループ名もしくは完全修飾グループ名に準拠した名前でなければならない
			if (value instanceof String) {
				if (!Group.isValidGroupName((String)value) &&
						!Group.isValidFullyQualifiedGroupName((String)value)&&
						!Table.isValidTableName((String)value) &&
						!Table.isValidFullyQualifiedTableName((String)value)) {
					throw new IllegalArgumentException("不適切なグループ名:" + (String)value);
				}
				this._value = value;
			} else {
				throw new IllegalArgumentException("GroupReferenceの場合、String型オブジェクトが必要");
			}
			break;
		case TABLE_REFERENCE:
			// valueはテーブル名もしくは完全修飾テーブル名、範囲付きテーブル名もしくは
			// 完全修飾範囲付きテーブル名でなければならない
			if (value instanceof String) {
				if (!Table.isValidTableName((String)value) &&
						!Table.isValidFullyQualifiedTableName((String)value) &&
						!Table.isValidTableNameWithRange((String)value) &&
						!Table.isValidFullyQualifiedTableNameWithRange((String)value)) {
					throw new IllegalArgumentException("不適切なテーブル名:" + (String)value);
				}
				this._value = value;
			} else {
				throw new IllegalArgumentException("TableReferenceの場合、String型オブジェクトが必要");
			}
			break;
		default:
			// case文ですべてのTokenTypeについて処理されていなければならない
			throw new IllegalArgumentException("invalid element type: " + this._type);
		}

		// ハッシュ値を生成しておく
		this._hashCode = makeHashCode();
	}

	/**
	 * エレメントの型を使ってオブジェクトを初期化します。<br>
	 * 値にはエレメントの型により以下がセットされます。<br>
	 * <ol>
	 * <li>EMPTY	null
	 * <li>NUMBER	0
	 * <li>STRING	""
	 * <li>DATE	エポック
	 * <li>ERROR	IllegalArgumentExceptionがスローされます
	 * <li>Uncalc	null
	 * <li>OPERATOR	IllegalArgumentExceptionがスローされます
	 * <li>REFERENCE	IllegalArgumentExceptionがスローされます
	 * </ol>
	 *
	 * @param tokenType
	 */
	public static Element newElement(ElementType tokenType) {
		Element newElem = new Element(tokenType);
		return getElementFromCashe(newElem);
	}

	// Element(ElementType) の実装メソッド
	private Element(ElementType tokenType) {
		this._type = tokenType;
		switch(tokenType) {
		case EMPTY:
			this._value = null;
			break;
		case NUMBER:
			this._value = new BigDecimal("0");
			break;
		case STRING:
			this._value = "";
			break;
		case DATE:
			this._value = new Long(0L);
			break;
		case BOOLEAN:
			this._value = new Boolean(false);
		case ERROR:
			throw new IllegalArgumentException("Errorの場合、valueの指定が必要");
		case OPERATOR:
			throw new IllegalArgumentException("Operatorの場合、valueの指定が必要");
		case REFERENCE:
			throw new IllegalArgumentException("Referenceの場合、valueの指定が必要");
		case GROUP_REFERENCE:
			throw new IllegalArgumentException("GroupReferenceの場合、valueの指定が必要");
		default:
			this._value = null;
			// case文ですべてのTokenTypeについて処理されていなければならない
			assert true;
		}
	}

	/**
	 * タイプを返します
	 * @return エレメントの型
	 */
	public ElementType getType() {
		return _type;
	}

	/**
	 * 値を返します
	 * このメソッドはequals(Object obj)の実装のために利用するだけであるため、デフォルトアクセスのメソッドです。
	 * @return エレメントの値オブジェクト
	 */
	Object getValue() {
		return _value;
	}

	/**
	 * このエレメントが数値を返す場合、trueを返します。
	 * @return このエレメントが数値を返す場合true、そうでない場合false
	 */
	public boolean hasNumberValue() {
		switch(_type) {
		case EMPTY:
		case NUMBER:
		case BOOLEAN:
			return true;
		default:
			return false;
		}
	}

	/**
	 * エラーのタイプを返します
	 * @return エレメントのエラーのタイプ
	 * @throws IllegalStateException エレメントの型がエラー以外でこのメソッドを呼び出した場合
	 */
	public ErrorType getErrorType() throws IllegalStateException {
		if (this._type != ElementType.ERROR) {
			throw new IllegalStateException("Element type is not ERROR.");
		}
		return (ErrorType)_value;
	}

	/**
	 * セル参照情報を返します
	 * @return セル参照情報
	 * @throws IllegalStateException エレメントの型が参照以外でこのメソッドを呼び出した場合
	 */
	String getCellReference() throws IllegalStateException {

		switch(this._type) {
		case REFERENCE:
			if (this._value instanceof String) {
				return (String) this._value;
			} else {
				throw new IllegalStateException("value is not CellReference");
			}
		default:
			// 型がEmptyの場合もエラー
			throw new IllegalStateException("invalid element type: " + this._type);
		}
	}

	/**
	 * グループ参照情報を返します
	 * @return グループ参照情報
	 * @throws IllegalStateException エレメントの型がグループ参照以外でこのメソッドを呼び出した場合
	 */
	String getGroupReference() {
		switch(this._type) {
		case GROUP_REFERENCE:
			if (this._value instanceof String) {
				return (String) this._value;
			} else {
				throw new IllegalStateException("value is not GROUP_REFERENCE");
			}
		default:
			// 型がEmptyの場合もエラー
			throw new IllegalStateException("invalid element type: " + this._type);
		}
	}

	/**
	 * テーブル参照情報を返します
	 * @return グループ参照情報
	 * @throws IllegalStateException エレメントの型がテーブル参照以外でこのメソッドを呼び出した場合
	 */
	String getTableReference() {
		switch(this._type) {
		case TABLE_REFERENCE:
			if (this._value instanceof String) {
				return (String) this._value;
			} else {
				throw new IllegalStateException("value is not TABLE_REFERENCE");
			}
		default:
			// 型がEmptyの場合もエラー
			throw new IllegalStateException("invalid element type: " + this._type);
		}
	}

	/**
	 * オペレータ情報を返します
	 * @return オペレータ情報
	 * @throws IllegalStateException エレメントの型がオペレータ以外でこのメソッドを呼び出した場合
	 */
	Operator getOperator() throws IllegalStateException {

		switch(this._type) {
		case OPERATOR:
			if (this._value instanceof Operator) {
				return (Operator) this._value;
			} else {
				throw new IllegalStateException("value is not OPERATOR");
			}
		default:
			// 型がEmptyの場合もエラー
			throw new IllegalStateException("invalid element type: " + this._type);
		}
	}

	/**
	 * 数値情報を返します。<br>
	 * 論理型の場合、trueのとき1、falseのとき0を返します。
	 * @return 数値情報
	 * @throws IllegalStateException エレメントの型が空、論理型もしくは数値情報以外でこのメソッドを呼び出した場合
	 */
	public BigDecimal getNumber() throws IllegalStateException {
		switch(this._type){
		case EMPTY:
			// 0を返す
			return BigDecimal.ZERO;
		case NUMBER:
			if (this._value instanceof BigDecimal) {
				return(BigDecimal)this._value;
			} else {
				throw new IllegalStateException("value is not number");
			}
		case BOOLEAN:
			return ((Boolean)this._value).booleanValue()? BigDecimal.ONE: BigDecimal.ZERO;
		case DATE:
			return new BigDecimal(((Long)this._value).longValue());
		default:
			throw new IllegalStateException("invalid element type: " + this._type);
		}
	}


	/**
	 * 文字列情報を返します
	 * @return 文字列情報
	 */
	public String getString() throws IllegalStateException {

		switch (this._type) {
		case EMPTY:
			// 空文字列を返す
			return "";
		case STRING:
		case FUNCTION:
			if (this._value instanceof String) {
				return (String) this._value;
			} else {
				throw new IllegalStateException("value is not STRING");
			}
		case DATE:
//			return DEFAULT_DATETIME_FORMAT.format(new Date(((Long)this._value).longValue()));
			return DEFAULT_DATETIME_FORMAT.format(new Date(((Long)this._value).longValue() - TimeZone.getDefault().getRawOffset()));
		case NUMBER:
		case BOOLEAN:
		case REFERENCE:
		case ERROR:
		case GROUP_REFERENCE:
		case OPERATOR:
		case TABLE_REFERENCE:
			return this._value.toString();
		default:
			throw new IllegalStateException("invalid element type: " + this._type);
		}
	}

	/**
	 * 論理値を返します
	 * @return 論理値
	 * @throws IllegalStateException エレメントの型がBOOLEAN以外でこのメソッドを呼び出した場合
	 */
	public Boolean getBoolean() throws IllegalStateException {

		switch (this._type) {
		case BOOLEAN:
			return ((Boolean)this._value).booleanValue();
		default:
			throw new IllegalStateException("invalid element type: " + this._type);
		}

	}

	/**
	 * 日時情報を返します
	 * @return 日時情報
	 * @throws IllegalStateException エレメントの型が空もしくは日時情報以外でこのメソッドを呼び出した場合
	 */
	public Date getDate() throws IllegalStateException {

		switch (this._type) {
		case EMPTY:
			// エポックのDateオブジェクトを返す
			return new Date(0);
		case DATE:
			if (this._value instanceof Long) {
				return new Date(((Long)this._value).longValue() - TimeZone.getDefault().getRawOffset());
			} else {
				throw new IllegalStateException("value is not Long");
			}
		default:
			throw new IllegalStateException("invalid element type: " + this._type);
		}
	}

	/**
	 * このエレメントの型が内部利用専用であるか否かを返します。
	 * @return ElementType.OPERATORなど、内部利用専用である場合true、そうでない場合false
	 */
	boolean isInnerElement() {
		switch (this.getType()) {
		case EMPTY:
		case BOOLEAN:
		case NUMBER:
		case STRING:
		case DATE:
		case ERROR:
			return false;
		default:
			return true;
		}
	}

	/* (非 Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {

		switch (_type) {
		case EMPTY:
			// Emptyの場合空文字を返す
			return "";
		case DATE:
			return _type.toString() + ":" + getString();
		default:
			return _type.toString() + ":" + _value.toString();
		}
	}

	/* (非 Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		if (obj == null) {
			return false;
		}

		if (obj instanceof Element) {
			Element target = (Element)obj;

			// エレメントの型が一致しないならfalse
			if (target.getType() != _type) {
				return false;
			}
			// エレメントの型が一致する場合、値を比較する
			switch (target.getType())
			{
				case EMPTY:
					return _type == ElementType.EMPTY;
				case BOOLEAN:
					return target.getBoolean().equals(_value);
				case NUMBER:
					return target.getNumber().equals(_value);
				case OPERATOR:
					return target.getOperator().equals(_value);
				case DATE:
					return target.getNumber().longValue() == ((Long)_value).longValue();
				case ERROR:
					return target.getErrorType().equals(_value);
				case STRING:
				case REFERENCE:
				case FUNCTION:
				case GROUP_REFERENCE:
				case TABLE_REFERENCE:
					return target.getString().equals(_value);
				default:
					assert false : ("invalid element type: " + this._type);
			}
		}
		// objはElement型ではない。falseを返す。
		return false;
	}

	/* (非 Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		return this._hashCode;
	}

	/**
	 * ハッシュ値を生成します。
	 * @return ハッシュ値
	 */
	private int makeHashCode() {
		int typeHash = _type.hashCode();
		int valueHash = 0;

		switch (_type) {
		case EMPTY:
			// do nothing.
			break;
		case BOOLEAN:
			valueHash = ((Boolean)_value).hashCode();
			break;
		case NUMBER:
			valueHash = ((BigDecimal)_value).hashCode();
			break;
		case OPERATOR:
			valueHash = ((Operator)_value).hashCode();
			break;
		case DATE:
			valueHash = ((Long)_value).hashCode();
			break;
		case ERROR:
			valueHash = ((ErrorType)_value).hashCode();
			break;
		case STRING:
		case REFERENCE:
		case FUNCTION:
		case GROUP_REFERENCE:
		case TABLE_REFERENCE:
			valueHash = ((String)_value).hashCode();
			break;
		default:
			assert false : ("invalid element type: " + this._type);
		}

		int result = 17;
		result = 31 * result + typeHash;
		result = 31 * result + valueHash;
		return result;
	}
}
