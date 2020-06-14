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

import com.nissy_ki_chi.jpicosheet.core.Book;
import com.nissy_ki_chi.jpicosheet.util.XMLReader;
import com.nissy_ki_chi.jpicosheet.util.XMLWriter;

public class XMLReaderTest extends TestCase {

	protected void setUp() throws Exception {
		super.setUp();
	}

	public void testReadWrite() {
		// Read
		XMLReader xmlReader = new XMLReader();
		Book book = null;
		try {
			book = xmlReader.read(new FileInputStream("src/test/XMLReaderTest_input1.xml"));
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
			transformer.transform(new DOMSource(document), new StreamResult(new FileOutputStream("src\\test\\XMLReaderTest_output1.xml")));
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
