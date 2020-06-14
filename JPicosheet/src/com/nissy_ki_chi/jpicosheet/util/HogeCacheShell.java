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
package com.nissy_ki_chi.jpicosheet.util;

import java.math.BigDecimal;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import com.nissy_ki_chi.jpicosheet.core.Book;
import com.nissy_ki_chi.jpicosheet.core.ReferenceNotFoundException;
import com.nissy_ki_chi.jpicosheet.core.Sheet;


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
