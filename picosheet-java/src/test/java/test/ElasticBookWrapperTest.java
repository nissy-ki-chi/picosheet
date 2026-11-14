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
