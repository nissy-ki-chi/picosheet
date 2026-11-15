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

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;

import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.TransformerFactoryConfigurationError;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import junit.framework.TestCase;

import org.w3c.dom.Document;

import com.nissy_ki_chi.picosheet.core.Book;
import com.nissy_ki_chi.picosheet.util.XMLReader;
import com.nissy_ki_chi.picosheet.util.XMLWriter;

public class XMLReaderTest extends TestCase {

	protected void setUp() throws Exception {
		super.setUp();
	}

	public void testReadWrite() {
		// Read
		XMLReader xmlReader = new XMLReader();
		Book book = null;
		try {
			book = xmlReader.read(new FileInputStream("src/test/resources/XMLReaderTest_input1.xml"));
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			fail();
		} catch (Exception e) {
			e.printStackTrace();
			fail();
		}

		// Write
		XMLWriter writer = new XMLWriter();
		Document document = null;
		try {
			document = writer.write(book);
		} catch (Exception e1) {
			e1.printStackTrace();
			fail();
		}
		Transformer transformer = null;
		try {
			transformer = TransformerFactory.newInstance().newTransformer();
		} catch (TransformerConfigurationException e) {
			e.printStackTrace();
			fail();
		} catch (TransformerFactoryConfigurationError e) {
			e.printStackTrace();
			fail();
		}
		transformer.setOutputProperty(OutputKeys.INDENT, "yes");
		transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
		try {
			transformer.transform(new DOMSource(document), new StreamResult(new FileOutputStream("target/XMLReaderTest_output1.xml")));
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			fail();
		} catch (TransformerException e) {
			e.printStackTrace();
			fail();
		}
		System.out.println(document);

	}

}
