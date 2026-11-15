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

import junit.framework.Test;
import junit.framework.TestSuite;

public class AllTests {

	public static Test suite() {
		TestSuite suite = new TestSuite("Test for test");
		//$JUnit-BEGIN$
		suite.addTestSuite(BookTest.class);
		suite.addTestSuite(SheetTest.class);
		suite.addTestSuite(ElementTest.class);
		suite.addTestSuite(CellTest.class);
		suite.addTestSuite(ApplicationTest.class);
		suite.addTestSuite(CalculationTest.class);
		suite.addTestSuite(FunctionTest.class);
		suite.addTestSuite(GroupTest.class);
		suite.addTestSuite(TableTest.class);
		suite.addTestSuite(RangeTest.class);
		suite.addTestSuite(TextLoaderTest.class);
		suite.addTestSuite(ConcurrentBookWrapperTest.class);
		suite.addTestSuite(SimpleWriterTest.class);
		suite.addTestSuite(XMLReaderTest.class);
		//$JUnit-END$
		return suite;
	}

}
