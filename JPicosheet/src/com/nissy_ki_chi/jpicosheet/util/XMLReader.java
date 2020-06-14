package com.nissy_ki_chi.jpicosheet.util;

import java.io.IOException;
import java.io.InputStream;
import java.math.MathContext;
import java.math.RoundingMode;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import com.nissy_ki_chi.jpicosheet.core.Book;
import com.nissy_ki_chi.jpicosheet.core.Cell;
import com.nissy_ki_chi.jpicosheet.core.Group;
import com.nissy_ki_chi.jpicosheet.core.Sheet;
import com.nissy_ki_chi.jpicosheet.core.Table;

/**
 * XML形式のファイルを読み込んでBookオブジェクトを作成します。
 * @author Yusuke
 *
 */
public class XMLReader {

	private XPath _xpath;

	/**
	 * このオブジェクトを初期化します。
	 */
	public XMLReader() {
		_xpath = XPathFactory.newInstance().newXPath();
	}

	/**
	 * 渡されたストリームからXMLデータを読み込んでBookオブジェクトを作成します。
	 * @param is XMLデータのストリーム
	 * @return XMLデータから作成したBookオブジェクト
	 * @throws Exception データの読み込み時にエラーが発生した場合
	 */
	public Book read(InputStream is) throws Exception {

		Document document = null;
		try {
			DocumentBuilder builder = DocumentBuilderFactory.newInstance()
					.newDocumentBuilder();
			document = builder.parse(is);
		} catch (ParserConfigurationException e) {
			throw new Exception(e);
		} catch (SAXException e) {
			throw new Exception(e);
		} catch (IOException e) {
			throw new Exception(e);
		}

		Book book = null;
		try {
			// bookノード
			Node bookNode = getNode(document, "/book");
			book = createBook(bookNode);

			// sheetノード
			NodeList sheetNodes = getSubnodes(bookNode, "./sheet");
			for (int i = 0; i < sheetNodes.getLength(); i++) {
				Node sheetNode = sheetNodes.item(i);
				Sheet sheet = createSheet(book, sheetNode);

				// cellノード
				NodeList cellNodes = getSubnodes(sheetNode, "./cell");
				for (int j = 0; j < cellNodes.getLength(); j++) {
					Node cellNode = cellNodes.item(j);
					createCell(sheet, cellNode);
				}

				// tableノード
				NodeList tableNodes = getSubnodes(sheetNode, "./table");
				for (int k = 0; k < tableNodes.getLength(); k++) {
					Node tableNode = tableNodes.item(k);
					createTable(sheet, tableNode);
				}

				// groupノード
				NodeList groupNodes = getSubnodes(sheetNode, "./group");
				for (int l = 0; l < groupNodes.getLength(); l++) {
					Node groupNode = groupNodes.item(l);
					createGroup(sheet, groupNode);
				}
			}

		} catch (XPathExpressionException e) {
			throw new Exception(e);
		}

		return book;
	}

	/**
	 * 渡されたBookノードからBookオブジェクトを作成します。
	 * @param bookNode XMLドキュメントのBookノード
	 * @return Bookオブジェクト
	 */
	private Book createBook(Node bookNode) {
		if (bookNode == null) {
			throw new NullPointerException("bookノードがnullです。");
		}

		Book book = null;
		String bookName = getAttribute(bookNode, "name");
		if (bookName != null) {
			book = new Book(bookName);
		}
		return book;
	}

	/**
	 * 渡されたsheetNodeノードからSheetオブジェクトを作成し、Bookオブジェクトに追加します。
	 * @param book シートを追加するBookオブジェクト
	 * @param sheetNode XMLドキュメントのSheetノード
	 * @return Sheetオブジェクト
	 * @throws Exception シートの作成時にエラーが発生した場合
	 */
	private Sheet createSheet(Book book, Node sheetNode) throws Exception {

		if (book == null) {
			throw new NullPointerException("bookがnullです。");
		}
		if (sheetNode == null) {
			throw new NullPointerException("sheetNodeがnullです。");
		}

		// シート情報を取得
		String sheetName = getAttribute(sheetNode, "name");
		String precision = getAttribute(sheetNode, "precision");
		String rounding = getAttribute(sheetNode, "roundingMode");
		if (sheetName == null) {
			throw new Exception("シート名がありません：" + sheetNode.toString());
		}

		// シートオブジェクト作成
		Sheet sheet = book.addSheet(sheetName);

		// 精度情報指定のチェック
		boolean validPrecision = true;
		int intPrecision = 0;
		if (precision != null) {
			try {
				intPrecision = Integer.parseInt(precision);
			} catch (NumberFormatException e) {
				validPrecision = false;
			}
		} else {
			validPrecision = false;
		}

		// 丸めモード指定のチェック
		boolean validRounding = true;
		RoundingMode roundingMode = null;
		if (rounding != null) {
			String upperRounding = rounding.toUpperCase();
			if (upperRounding.equals("UP")) {
				roundingMode = RoundingMode.UP;
			} else if (upperRounding.equals("DOWN")) {
				roundingMode = RoundingMode.DOWN;
			} else if (upperRounding.equals("CEILING")) {
				roundingMode = RoundingMode.CEILING;
			} else if (upperRounding.equals("FLOOR")) {
				roundingMode = RoundingMode.FLOOR;
			} else if (upperRounding.equals("HALF_UP")) {
				roundingMode = RoundingMode.HALF_UP;
			} else if (upperRounding.equals("HALF_DOWN")) {
				roundingMode = RoundingMode.HALF_DOWN;
			} else if (upperRounding.equals("HALF_EVEN")) {
				roundingMode = RoundingMode.HALF_EVEN;
			} else {
				validRounding = false;
			}
		}

		// 制度情報、丸めモード情報のSheetオブジェクトへのセット
		MathContext mc = Sheet.DEFAULT_MATH_CONTEXT;
		if (validPrecision && validRounding) {
			mc = new MathContext(intPrecision, roundingMode);
		} else if (validPrecision) {
			mc = new MathContext(intPrecision,
					Sheet.DEFAULT_MATH_CONTEXT.getRoundingMode());
		} else if (validRounding) {
			mc = new MathContext(Sheet.DEFAULT_MATH_CONTEXT.getPrecision(),
					roundingMode);
		}
		sheet.setMathContext(mc);
		return sheet;
	}

	/**
	 * 渡されたcellNodeノードからCellオブジェクトを作成し、Sheetオブジェクトに追加します。
	 * @param sheet セルを追加するSheetオブジェクト
	 * @param cellNode XMLドキュメントのCellノード
	 * @return Cellオブジェクト
	 * @throws Exception セル作成時にエラーが発生した場合
	 */
	private Cell createCell(Sheet sheet, Node cellNode) throws Exception {

		if (sheet == null) {
			throw new NullPointerException("sheetがnullです");
		}
		if (cellNode == null) {
			throw new NullPointerException("cellNodeがnullです");
		}

		String cellName = getAttribute(cellNode, "name");
		String type = getAttribute(cellNode, "type");
		String content = getContent(cellNode);
		if (cellName == null) {
			throw new Exception("セル名がありません" + cellNode.toString());
		}
		Cell cell = sheet.addCell(cellName);
		setCellValue(cell, type, content);
		return cell;
	}

	/**
	 * 渡されたtableNodeノードからテーブル情報をSheetオブジェクトに設定します。
	 * @param sheet テーブルを追加するSheetオブジェクト
	 * @param tableNode XMLドキュメントのTableノード
	 * @throws Exception テーブル作成時にエラーが発生した場合
	 */
	private void createTable(Sheet sheet, Node tableNode) throws Exception {

		if (sheet == null) {
			throw new NullPointerException("sheetがnullです");
		}
		if (tableNode == null) {
			throw new NullPointerException("tableNodeがnullです");
		}

		String tableName = getAttribute(tableNode, "name");
		String rowNum = getAttribute(tableNode, "row");
		String colNum = getAttribute(tableNode, "col");
		if (tableName == null) {
			throw new Exception("テーブル名がありません");
		}
		int rowSize = 0;
		int colSize = 0;
		try {
			rowSize = Integer.parseInt(rowNum);
			colSize = Integer.parseInt(colNum);
		} catch (NumberFormatException nfe) {
			throw new Exception("テーブルの行列指定が正しくありません。行：" + rowSize + "列："
					+ colSize);
		}
		Table table = sheet.addTable(tableName, rowSize, colSize);

		// tableノード内 rowノード
		NodeList tableRowNodes = getSubnodes(tableNode, "./row");
		for (int row = 0; row < tableRowNodes.getLength(); row++) {
			Node rowNode = tableRowNodes.item(row);
			String rowNodeNum = getAttribute(rowNode, "num");
			int rowNodeNumInt = 0;
			try {
				rowNodeNumInt = Integer.parseInt(rowNodeNum);
			} catch (NumberFormatException nfe) {
				throw new Exception("テーブルデータの行指定が正しくありません：" + rowNodeNum);
			}

			// tableノード内 colノード
			NodeList tableColNodes = getSubnodes(rowNode, "./col");
			for (int col = 0; col < tableColNodes.getLength(); col++) {
				Node colNode = tableColNodes.item(col);
				String colNodeNum = getAttribute(colNode, "num");
				int colNodeNumInt = 0;
				try {
					colNodeNumInt = Integer.parseInt(colNodeNum);
				} catch (NumberFormatException nfe) {
					throw new Exception("テーブルデータの列指定が正しくありません：" + colNodeNum);
				}
				String type = getAttribute(colNode, "type");
				String colNodeContent = getContent(colNode);
				setCellValue(table.getCell(rowNodeNumInt, colNodeNumInt), type,
						colNodeContent);
			}
		}
	}

	/**
	 * 渡されたgroupNodeノードからグループ情報をSheetオブジェクトに設定します。
	 * @param sheet テーブルを追加するSheetオブジェクト
	 * @param groupNode XMLドキュメントのGroupノード
	 * @throws XPathExpressionException
	 */
	private void createGroup(Sheet sheet, Node groupNode)
			throws XPathExpressionException {

		if (sheet == null) {
			throw new NullPointerException("sheetがnullです");
		}
		if (groupNode == null) {
			throw new NullPointerException("groupNodeがnullです");
		}

		String groupName = getAttribute(groupNode, "name");
		NodeList groupNameNodes = getSubnodes(groupNode, "./cell");
		Group group = sheet.addGroup(groupName);
		for (int i = 0; i < groupNameNodes.getLength(); i++) {
			Node groupCellNode = groupNameNodes.item(i);
			String groupCellName = getAttribute(groupCellNode, "name");
			group.addCell(groupCellName);
		}
	}

	/**
	 * 指定したセルに、指定したタイプで内容をセットします。<br>
	 * <code>type</code>がnullもしくは不正な値の場合、<code>content</code>の内容からタイプを判断します。
	 *
	 * @param cell
	 *            内容をセットするセル
	 * @param type
	 *            タイプ。nullを渡した場合contentの内容で判断して値をセット
	 * @param content
	 *            セットする内容
	 * @return 引数で渡され、値セットされたCellオブジェクト
	 */
	private Cell setCellValue(Cell cell, String type, String content) {

		if (cell == null) {
			throw new NullPointerException("cellがnullです");
		}

		if (type != null) {
			if ("STR".equalsIgnoreCase(type)) {
				cell.setStringValue(content);
			} else if ("NUM".equalsIgnoreCase(type)) {
				cell.setNumberValue(content);
			} else if ("bool".equalsIgnoreCase(type)) {
				cell.setBooleanValue(Boolean.parseBoolean(content));
			} else if ("FORMULA".equalsIgnoreCase(type)) {
				cell.setFormula(content);
			} else if ("DATE".equalsIgnoreCase(type)) {
				cell.setValue(content);
			} else {
				cell.setValue(content);
			}
		} else {
			cell.setValue(content);
		}
		return cell;
	}

	/**
	 * 指定したドキュメント配下の、Xpathで指定したノードを返します。
	 * @param document ノードを取得するドキュメント
	 * @param xpathString ノードを指定するXpath
	 * @return Xpath指定に一致するノード
	 * @throws XPathExpressionException Xpath指定が誤っている場合
	 */
	private Node getNode(Document document, String xpathString)
			throws XPathExpressionException {
		return (Node) _xpath.evaluate(xpathString, document,
				XPathConstants.NODE);
	}

	private Node getNode(Document document, XPathExpression expr)
			throws XPathExpressionException {
		return (Node) expr.evaluate(document, XPathConstants.NODE);
	}

	private NodeList getNodes(Document document, String xpathString)
			throws XPathExpressionException {
		return (NodeList) _xpath.evaluate(xpathString, document,
				XPathConstants.NODESET);
	}

	private NodeList getNodes(Document document, XPathExpression expr)
			throws XPathExpressionException {
		return (NodeList) expr.evaluate(document, XPathConstants.NODESET);
	}

	/**
	 * 指定したノード配下の、xpathで指定したサブノードのリストを返します。
	 * @param node サブノードを取得する親ノード
	 * @param xpathString サブノードを指定するXpath
	 * @return Xpath指定に一致するノードのリスト
	 * @throws XPathExpressionException Xpath指定が誤っている場合
	 */
	private NodeList getSubnodes(Node node, String xpathString)
			throws XPathExpressionException {
		return (NodeList) _xpath.evaluate(xpathString, node,
				XPathConstants.NODESET);
	}

	/**
	 * 指定したノードから、指定した属性の値を取得します。<br>
	 * ノードに指定した属性が存在しない場合、nullを返します。
	 * @param node 属性を取得するノード
	 * @param attrName 属性名
	 * @return 属性の値
	 */
	private String getAttribute(Node node, String attrName) {
		if (node == null) {
			throw new NullPointerException("nodeがnullです。");
		}

		if (!node.hasAttributes()
				|| node.getAttributes().getNamedItem(attrName) == null) {
			return null;
		}

		return node.getAttributes().getNamedItem(attrName).getNodeValue();
	}

	private String getContent(Node node) {
		return node.getTextContent();
	}
}
