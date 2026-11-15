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


import com.nissy_ki_chi.picosheet.core.Book;
import com.nissy_ki_chi.picosheet.core.ElasticBookWrapper;
import com.nissy_ki_chi.picosheet.core.ElasticCellWrapper;
import com.nissy_ki_chi.picosheet.core.ElasticSheetWrapper;

import junit.framework.TestCase;

public class ElasticBookWrapperTest extends TestCase{

	protected void setUp() throws Exception {
		super.setUp();
	}

	public void testBookWrapper() {
		ElasticBookWrapper ebw = new ElasticBookWrapper(new Book("elasticWrapperTest"));
	}

	public void testGetSheet() {
		ElasticBookWrapper ebw = new ElasticBookWrapper(new Book("elasticWrapperTest"));
		ElasticSheetWrapper esw = ebw.addSheet("ElasSheet1");
		ElasticCellWrapper ecw = esw.addCell("ElasticCell");
		ecw.setValue("12345");
		assertEquals("12345", ecw.getValue().getString());
	}


}
