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
package test;

import com.nissy_ki_chi.picosheet.util.HogeCacheShell;

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
