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
package test;

import com.nissy_ki_chi.jpicosheet.util.HogeCacheShell;

import junit.framework.TestCase;

public class HogeCacheShellTest extends TestCase {


	private HogeCacheShell _cache;

	protected void setUp() throws Exception {
		super.setUp();
		_cache = new HogeCacheShell();
	}

	public void testPutString() {
		_cache.putString("key1", "value1");
		_cache.putString("key2", "value2");
		_cache.putString("key3", "value3");
		_cache.putString("key4", "value4");
		_cache.putString("key5", "value5");
		_cache.putString("key6", "value6");
		_cache.putString("key7", "value7");
	}

	public void testPutNumber() {
		_cache.putString("key1", "1");
		_cache.putString("key2", "123");
		_cache.putString("key3", "-12345");
		_cache.putString("key4", "12345678901234567890");
		_cache.putString("key5", "-1.234567");
	}

	public void testGetString() {

		_cache.putString("key1", "value1");
		_cache.putString("key2", "value2");
		_cache.putString("key3", "value3");
		_cache.putString("key4", "value4");
		_cache.putString("key5", "value5");
		_cache.putString("key6", "value6");
		_cache.putString("key7", "value7");
		assertEquals("value1", _cache.getString("key1"));

	}



	public void testGetNumber() {

		_cache.putString("key1", "1");
		_cache.putString("key2", "123");
		_cache.putString("key3", "-12345");
		_cache.putString("key4", "12345678901234567890");
		_cache.putString("key5", "-1.234567");
		assertEquals("12345678901234567890", _cache.getString("key4"));

	}
}
