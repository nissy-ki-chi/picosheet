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


import java.math.MathContext;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock.ReadLock;
import java.util.concurrent.locks.ReentrantReadWriteLock.WriteLock;

/**
 * 1つのBookオブジェクトを複数のスレッドで同時に利用するためのラッパークラスです。<br>
 * Bookオブジェクト、およびBookオブジェクトから得られるSheetなどのオブジェクトはスレッドセーフではありません。
 * 複数のスレッドから同時にアクセスされるような場合、適切な同期を行うためにこのクラスを利用してください。<br>
 * このクラスは同期化の制御にjava.util.concurrent.locksパッケージのReentrantReadWriteLockを利用しています。
 * このためBookオブジェクトおよび配下のSheet、Cell等のオブジェクトに対する参照を複数のスレッドから同時に行えます。
 * シートの追加、セルへの値セットなど、Bookおよび配下のオブジェクトに対する変更の操作を行えるのはこのオブジェクトの
 * 書き込みロックを取得した1つのスレッドだけです。<br>
 * <br>
 * このクラスを使ってBookオブジェクトに対する同時処理を行う場合、クラスのオブジェクトを1つだけ作成した上で
 * 各スレッドはこのオブジェクトのメソッドのみを使ってBookおよび配下のオブジェクトを操作する必要があります。
 * もしBookオブジェクト自体が持つ<code>getSheet(String sheetName)</code>メソッドを使ってSheetオブジェクトを取得し
 * セルの追加や参照などの操作を行おうとした場合、Bookおよび配下のオブジェクトの整合性は保障されません。<br>
 * <br>
 * このクラスは、通常はこのクラスを利用するスレッドにBookおよび配下のオブジェクトを渡してしまわないように
 * メソッドが調整されています。Bookオブジェクト等のオブジェクトを直接操作場合と比べ、以下の例のような違いがあります。<br>
 * <ul>
 * <li><code>addSheet()</code>や<code>addCell()</code>などのメソッドがSheetオブジェクトやCellオブジェクトを返さない</li>
 * <li><code>setCellValue()</code>などのメソッドを使用する際、対象となるSheetやCellが存在しなければ自動的に作成される</li>
 * <li>コンストラクタに渡すBookオブジェクトの中にSheetが1つもない場合、Sheet1という名前のSheetが自動的に作成される</li>
 * </ul>
 * <br>
 *
 *
 * @author yusuke nishikawa
 *
 */
public class ConcurrentBookWrapper {

	private static final String DEFAULT_SHEET_NAME = "Sheet1";

	private Book _book;

	private ReadLock _readLock;
	private WriteLock _writeLock;

	/**
	 * 引数なしのコンストラクタは使用禁止
	 */
	@SuppressWarnings("unused")
	private ConcurrentBookWrapper() {
	}

	/**
	 * Bookオブジェクトを利用してオブジェクトを初期化します。
	 * @param book Bookオブジェクト
	 */
	public ConcurrentBookWrapper (Book book) {
		_book = book;
		// Bookオブジェクトにシートが1つも作成されていない場合、シートを作成する
		if (_book.getResolver().getCurrentSheet() == null) {
			_book.addSheet(DEFAULT_SHEET_NAME);
		}

		ReentrantReadWriteLock lock = new ReentrantReadWriteLock();
		_readLock = lock.readLock();
		_writeLock = lock.writeLock();
	}

	/**
	 * ブック名を変更します。
	 * @param bookName 変更するブック名
	 */
	public void setBookName(String bookName) {
		try {
			_writeLock.lock();
			_book.setName(bookName);
		} finally {
			_writeLock.unlock();
		}
	}

	/**
	 * ブック名を返します。<br>
	 * このメソッドは読み込みロックを使用しているため、複数のスレッドから同時に実行することができます。
	 * @return ブック名
	 */
	public String getBookName() {
		try {
			_readLock.lock();
			return _book.getName();
		} finally {
			_readLock.unlock();
		}
	}

	/**
	 * シートを追加します。<br>
	 * 指定したシート名がすでに存在する場合は何もしません。
	 * @param sheetName 新しく追加するシートの名前
	 */
	public void addSheet(String sheetName) {
		try {
			_writeLock.lock();
			_book.addSheet(sheetName);
		} finally {
			_writeLock.unlock();
		}
	}

	/**
	 * シートを削除します。<br>
	 * 指定したシートが存在しない場合、ReferenceNotFoundExceptionが発生します。
	 * @param sheetName 削除対象のシート名
	 */
	public void deleteSheet(String sheetName) {
		try {
			_writeLock.lock();
			_book.deleteSheet(sheetName);
		} finally {
			_writeLock.unlock();
		}
	}

	/**
	 * テーブルを追加します。<br>
	 * 指定したテーブル名がすでに存在する場合は何もしません。
	 * @param sheetName シート名。存在しない場合は新しく作成する。
	 * @param tableName テーブル名
	 * @param rowSize テーブルの行数
	 * @param colSize　テーブルの列数
	 */
	public void addTable(String sheetName, String tableName, int rowSize, int colSize) {
		try {
			_writeLock.lock();
			_book.addSheet(sheetName).addTable(tableName, rowSize, colSize);
		} finally {
			_writeLock.unlock();
		}
	}
	
	/**
	 * テーブルを削除します。<br>
	 * 指定したシートが存在しない場合、ReferenceNotFoundExceptionが発生します。指定したテーブル名が存在しない場合は何もしません。
	 * @param sheetName シート名
	 * @param tableName 削除対象のテーブル名
	 */
	public void deleteTable(String sheetName, String tableName) {
		try {
			_writeLock.lock();
			_book.getSheet(sheetName).deleteTable(tableName);
		} finally {
			_writeLock.unlock();
		}
	}

	/**
	 * グループを追加します。<br>
	 * 指定したグループ名がすでに存在する場合は何もしません。
	 * @param sheetName シート名。存在しない場合は新しく作成する。
	 * @param groupName グループ名
	 */
	public void addGroup(String sheetName, String groupName) {
		try {
			_writeLock.lock();
			_book.addSheet(sheetName).addGroup(groupName);
		} finally {
			_writeLock.unlock();
		}
	}

	/**
	 * グループを削除します。<br>
	 * 指定したシートが存在しない場合、ReferenceNotFoundExceptionが発生します。指定したグループ名が存在しない場合は何もしません。
	 * @param sheetName シート名
	 * @param groupName 削除対象のグループ名
	 */
	public void deleteGroup(String sheetName, String groupName) {
		try {
			_writeLock.lock();
			_book.getSheet(sheetName).deleteGroup(groupName);
		} finally {
			_writeLock.unlock();
		}
	}
	
	/**
	 * 指定したグループにセルを追加します。
	 * @param sheetName シート名。存在しない場合は新しく作成する。
	 * @param groupName グループ名
	 * @param cellNames 追加するセル名
	 */
	public void addCellsToGroup(String sheetName, String groupName, String[] cellNames) {
		try {
			_writeLock.lock();
			_book.addSheet(sheetName).getGroup(groupName).addCells(cellNames);
		} finally {
			_writeLock.unlock();
		}
	}
	
	/**
	 * 指定したグループからセルを削除します。
	 * 指定したシート、グループが存在しない場合、ReferenceNotFoundExceptionが発生します。指定したセル名が存在しない場合は何もしません。
	 * @param sheetName シート名
	 * @param groupName グループ名
	 * @param cellNames 削除対象のセル名の配列
	 */
	public void deleteCellsToGroup(String sheetName, String groupName, String[] cellNames) {
		try {
			_writeLock.lock();
			_book.getSheet(sheetName).getGroup(groupName).removeCells(cellNames);
		} finally {
			_writeLock.unlock();
		}
	}
	
	/**
	 * 既存のシートのシート名を変更します。<br>
	 * 変更前のシートが存在しない場合、ReferenceNotFoundExceptionが発生します。<br>
	 * 変更後のシート名を持つシートがすでに存在していた場合、そのシートは削除されます。
	 * @param sheetName 変更対象のシート名
	 * @param newSheetName 変更後のシート名
	 */
	public void renameSheet(String sheetName, String newSheetName) {
		try {
			_writeLock.lock();
			_book.renameSheet(sheetName, newSheetName);
		} finally {
			_writeLock.unlock();
		}
	}

	/**
	 * 指定したシートのMathContextをセットします。<br>
	 * 指定したシートが存在しない場合、ReferenceNotFoundExceptionが発生します。
	 * @param sheetName MachContextをセットするシート名
	 * @param mc MathContextオブジェクト
	 */
	public void setMathContext(String sheetName, MathContext mc) {
		try {
			_writeLock.lock();
			_book.getSheet(sheetName).setMathContext(mc);
		} finally {
			_writeLock.unlock();
		}
	}
	
	/**
	 * 指定したシートのMathContextを返します。<br>
	 * 指定したシートが存在しない場合、ReferenceNotFoundExceptionが発生します。
	 * @param sheetName MathContextを取得するシート名
	 * @return シートのMathContextオブジェクト
	 */
	public MathContext getMathContext(String sheetName) {
		try {
			_readLock.lock();
			return _book.getSheet(sheetName).getMathContext();
		} finally {
			_readLock.unlock();
		}
	}
	
	/**
	 * セル値の変更による自動再計算を有効化します。
	 */
	public void recalcEnable() {
		try {
			_writeLock.lock();
			_book.recalcEnable();
		} finally {
			_writeLock.unlock();
		}
	}
	
	/**
	 * セル値の変更による自動再計算を無効化します。
	 */
	public void recalcDisable() {
		try {
			_writeLock.lock();
			_book.recalcDisable();
		} finally {
			_writeLock.unlock();
		}
	}
	
	/**
	 * セルの値をセットします。<br>
	 * 値として渡された文字列をチェックし、適切な型の値として保存します。<br>
	 * セル名が シート名!セル名 の形式で渡された場合、指定されたシートの指定されたセルに対して値をセットします。
	 * セル名のみが渡された場合、このメソッドが呼ばれた時点のデフォルトシートの指定されたセルに対して値をセットします。
	 * シート名もしくはセル名が存在しなかった場合はそれぞれ新しく作成されます。
	 * @param cellName 値をセットするセル名
	 * @param value 値の文字列表現
	 */
	public void setCellValue(String cellName, String value) {
		try {
			_writeLock.lock();
			addCell(cellName);
			_book.getResolver().getCell(cellName).setValue(value);
		} finally {
			_writeLock.unlock();
		}
	}

	/**
	 * セルの値を取得します。<br>
	 * 指定したセルが存在しない場合、ReferenceNotFoundExceptionが発生します。<br>
	 * シート名の指定がなく、セル名のみが渡された場合はこのメソッドが呼ばれた時点のデフォルトシートに対してセルの値を
	 * 取得しようとします。<br>
	 * このメソッドは読み込みロックを使用しているため、複数のスレッドから同時に実行することができます。
	 * 返されるElementオブジェクトは不変(immutable)のオブジェクトであるため、複数のスレッドがこれを同時に取得しても
	 * 問題ありません。
	 * @param cellName 値を取得するセル名
	 * @return セルの値
	 */
	public Element getCellValue(String cellName) {
		try {
			_readLock.lock();
			return _book.getResolver().getCell(cellName).getValue();
		} finally {
			_readLock.unlock();
		}
	}
	
	/**
	 * セルの式を取得します。<br>
	 * 指定したセルが存在しない場合、ReferenceNotFoundExceptionが発生します。<br>
	 * シート名の指定がなく、セル名のみが渡された場合はこのメソッドが呼ばれた時点のデフォルトシートに対してセルの値を
	 * 取得しようとします。<br>
	 * このメソッドは読み込みロックを使用しているため、複数のスレッドから同時に実行することができます。
	 * @param cellName 値を取得するセル名
	 * @return 式の文字列
	 */
	public String getCellFormula(String cellName) {
		try {
			_readLock.lock();
			return _book.getResolver().getCell(cellName).getFormula();
		} finally {
			_readLock.unlock();
		}
	}

	/**
	 * セルを追加します。セル値には空を意味するElementType.EMPTYがセットされます。<br>
	 * セル名が シート名!セル名 の形式で渡された場合、指定されたシートの指定されたセルに対して値をセットします。
	 * セル名のみが渡された場合、このメソッドが呼ばれた時点のデフォルトシートの指定されたセルに対して値をセットします。
	 * @param cellName 追加するセル名
	 */
	public void addCell(String cellName) {
		// テーブルに対するセル追加は不要
		if (Table.isValidFullyQualifiedTableNameWithAddress(cellName)) {
			return;
		}

		try {
			_writeLock.lock();
			Resolver resolver = _book.getResolver();
			String sheetName;
			try {
				sheetName = resolver.getSheetNameFromFullyQualifiedName(cellName);
			} catch (Exception e) {
				sheetName = resolver.getCurrentSheet().getName();
			}
			addSheet(sheetName);
			_book.getSheet(sheetName).addCell(cellName);				
		} finally {
			_writeLock.unlock();
		}
	}

	/**
	 * セルを削除します。<br>
	 * セル名が シート名!セル名 の形式で渡された場合、指定されたシートの指定されたセルに対して削除を行います。
	 * セル名のみが渡された場合、このメソッドが呼ばれた時点のデフォルトシートの指定されたセルに対して削除を行います。<br>
	 * シート名!セル名 の形式で指定した場合で、指定したシートが存在しない場合、ReferenceNotFoundExceptionがスローされます。
	 * シートが存在し、セルが存在しない場合、何もしません。
	 * @param cellName 削除するセル名
	 */
	public void deleteCell(String cellName) {
		try {
			_writeLock.lock();
			Resolver resolver = _book.getResolver();
			if (resolver.cellExists(cellName)) {
				resolver.getCell(cellName).getSheet().deleteCell(cellName);
			}
		} finally {
			_writeLock.unlock();
		}
	}

	/**
	 * セル名を変更します。<br>
	 * 変更対象のセルが存在しない場合、ReferenceNotFoundExceptionが発生します。<br>
	 * 変更後のセル名を持つシートがすでに存在していた場合、そのセルは削除されます。<br>
	 * シート名!セル名 の形式で指定した場合で、指定したシートが存在しない場合、ReferenceNotFoundExceptionがスローされます。
	 * @param cellName 変更対象のセル名
	 * @param newCellName 変更後のセル名
	 */
	public void renameCell(String cellName, String newCellName) {
		try {
			_writeLock.lock();
			Resolver resolver = _book.getResolver();
			if (resolver.cellExists(cellName)) {
				resolver.getCell(cellName).getSheet().renameCell(cellName, newCellName);
			}
		} finally {
			_writeLock.unlock();
		}
	}

	/**
	 * セルのラベルをセットします。<br>
	 * セル名が シート名!セル名 の形式で渡された場合、指定されたシートの指定されたセルに対して値をセットします。
	 * セル名のみが渡された場合、このメソッドが呼ばれた時点のデフォルトシートの指定されたセルに対して値をセットします。
	 * シート名もしくはセル名が存在しなかった場合はそれぞれ新しく作成されます。
	 * @param cellName ラベルをセットするセル名
	 * @param label ラベル
	 */
	public void setCellLabel(String cellName, String label) {
		try {
			_writeLock.lock();
			addCell(cellName);
			_book.getResolver().getCell(cellName).setLabel(label);
		} finally {
			_writeLock.unlock();
		}
	}

	/**
	 * セルのラベルを返します。<br>
	 * 指定したセルが存在しない場合、ReferenceNotFoundExceptionが発生します。<br>
	 * シート名の指定がなく、セル名のみが渡された場合はこのメソッドが呼ばれた時点のデフォルトシートに対してセルの値を
	 * 取得しようとします。<br>
	 * このメソッドは読み込みロックを使用しているため、複数のスレッドから同時に実行することができます。
	 * @param cellName ラベルを取得するセル名
	 * @return ラベル
	 */
	public String getCellLabel(String cellName) {
		try {
			_readLock.lock();
			return _book.getResolver().getCell(cellName).getLabel();
		} finally {
			_readLock.unlock();
		}
	}
	

	/**
	 * 書き込みロックを得ます。<br>
	 * このロックを取得した場合、必ず<code>releaseWriteLock()</code>でロックの開放を行わなければなりません。
	 */
	public WriteLock getWriteLock() {
		_writeLock.lock();
		return _writeLock;
	}

	/**
	 * <code>getWriteLock()</code>で取得した書き込みロックを開放します。
	 */
	public void releaseWriteLock() {
		if (_writeLock.isHeldByCurrentThread()) {
			_writeLock.unlock();
		} else {
			_writeLock.unlock();
		}
	}

	/**
	 * このオブジェクトが保護しているBookオブジェクトを返します。<br>
	 * このメソッドを使ってBookオブジェクトを得るには、事前に<code>getWriteLock()</code>によって書き込みロックオブジェクトを
	 * 得ておかなければなりません。
	 * @param writeLock <code>getWriteLock()</code>によって得た書き込みロックオブジェクト
	 * @return Bookオブジェクト
	 */
	public Book getBook(WriteLock writeLock) {
		// 書き込みロックを持っていないならエラー
		if (writeLock == null ||
				writeLock != _writeLock) {
			throw new IllegalArgumentException("invalid WriteLock Object");
		} else {
			return _book;
		}
	}
}
