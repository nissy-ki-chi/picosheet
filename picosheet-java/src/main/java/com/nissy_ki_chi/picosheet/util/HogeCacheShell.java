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
package com.nissy_ki_chi.picosheet.util;

import java.math.BigDecimal;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import com.nissy_ki_chi.picosheet.core.Book;
import com.nissy_ki_chi.picosheet.core.ReferenceNotFoundException;
import com.nissy_ki_chi.picosheet.core.Sheet;


public class HogeCacheShell {


	Book _book;
	Sheet _sheet;

	/**
	 * このブックのロックオブジェクト
	 */
	private final ReadWriteLock _readWriteLock = new ReentrantReadWriteLock(true);


	public HogeCacheShell() {
		_book = new Book("HogeShellBook");
		_sheet = _book.addSheet("HogeSheet");

	}


	public void putString(String key, String valueStr) {
		_readWriteLock.writeLock().lock();
		try {
			_sheet.addCell(key).setStringValue(valueStr);
		} finally {
			_readWriteLock.writeLock().unlock();
		}
	}

	public String getString(String key) {
		_readWriteLock.readLock().lock();
		try {
			return _sheet.getCell(key).getValueString();
		} catch (ReferenceNotFoundException e) {
			return null;
		} finally {
			_readWriteLock.readLock().unlock();
		}
	}

	public void putNumber(String key, String numberStr) {
		_readWriteLock.writeLock().lock();
		try {
			_sheet.addCell(key).setNumberValue(numberStr);
		} finally {
			_readWriteLock.writeLock().unlock();
		}
	}

	public BigDecimal getNumber(String key) {
		_readWriteLock.readLock().lock();
		try {
			return _sheet.getCell(key).getValue().getNumber();
		} catch (ReferenceNotFoundException e) {
			return null;
		} finally {
			_readWriteLock.readLock().unlock();
		}
	}


}
