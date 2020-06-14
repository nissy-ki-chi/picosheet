package com.nissy_ki_chi.jpicosheet.util;

import java.text.SimpleDateFormat;
import java.util.Arrays;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.nissy_ki_chi.jpicosheet.core.Book;
import com.nissy_ki_chi.jpicosheet.core.Cell;
import com.nissy_ki_chi.jpicosheet.core.Cell.CellType;
import com.nissy_ki_chi.jpicosheet.core.Group;
import com.nissy_ki_chi.jpicosheet.core.Sheet;
import com.nissy_ki_chi.jpicosheet.core.Table;

/**
 * BookオブジェクトをXML形式のデータとして出力します。
 * @author Yusuke
 *
 */
public class XMLWriter {

	/**
	 * Dateタイプセルの出力フォーマット
	 */
	private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss.SSSS");

	/**
	 * このオブジェクトを初期化します。
	 */
	public XMLWriter() {

	}

	/**
	 * 渡されたBookオブジェクトからXML Documentオブジェクトを作成します
	 * @param book 出力対象のBookオブジェクト
	 * @return 渡されたBookオブジェクトのXML Documentオブジェクト表現
	 * @throws Exception データの出力時にエラーが発生した場合
	 */
	public Document write(Book book) throws Exception {

		if (book == null) {
			throw new NullPointerException();
		}

		Document document = null;
		try {
			document = DocumentBuilderFactory.newInstance()
					.newDocumentBuilder().newDocument();
		} catch (ParserConfigurationException e) {
			throw new Exception(e);
		}

		// Book
		Element bookElem = document.createElement("book");
		bookElem.setAttribute("name", book.getName());
		document.appendChild(bookElem);

		// Sheet
		Sheet[] sortedSheets = book.getSheets().toArray(new Sheet[0]);
		Arrays.sort(sortedSheets);
		for (Sheet sheet : sortedSheets) {
			Element sheetElem = document.createElement("sheet");
			sheetElem.setAttribute("name", sheet.getName());
			sheetElem.setAttribute("precision",
					Integer.toString(sheet.getMathContext().getPrecision()));
			sheetElem.setAttribute("roundingMode", sheet.getMathContext()
					.getRoundingMode().toString());
			bookElem.appendChild(sheetElem);

			// Cell
			Cell[] sortedCells = sheet.getCells().toArray(new Cell[0]);
			Arrays.sort(sortedCells);
			for (Cell cell : sortedCells) {
				// テーブル用のセルは後で処理するのでスキップ
				if (Table.isValidTableNameWithAddress(cell.getName())) {
					continue;
				}
				Element cellElem = document.createElement("cell");
				cellElem.setAttribute("name", cell.getName());
				setCellElement(document, cellElem, cell);
				sheetElem.appendChild(cellElem);
			}

			// Table
			Table tables[] = sheet.getTables().toArray(new Table[0]);
			Arrays.sort(tables);
			for (Table table : tables) {
				Element tableElem = document.createElement("table");
				tableElem.setAttribute("name", table.getName());
				int rowSize = table.getRowSize();
				tableElem.setAttribute("row", Integer.toString(rowSize));
				int colSize = table.getColSize();
				tableElem.setAttribute("col", Integer.toString(colSize));
				sheetElem.appendChild(tableElem);

				// テーブルの行
				for (int rowPos = 1; rowPos < rowSize; rowPos++) {
					Element rowElem = null;
					Cell[] cells = table.getRange(
							new StringBuilder("R").append(rowPos).append("Cx")
									.toString()).getRowArray(1);
					for (int colPos = 1; colPos < colSize; colPos++) {
						// 空のセルは保存不要
						if (cells[colPos].getCellType() == CellType.EMPTY) {
							continue;
						}
						if (rowElem == null) {
							rowElem = document.createElement("row");
							rowElem.setAttribute("num", Integer.toString(rowPos));
						}
						Element colElem = document.createElement("col");
						colElem.setAttribute("num", Integer.toString(colPos));
						setCellElement(document, colElem, cells[colPos]);
						rowElem.appendChild(colElem);
					}
					if (rowElem != null) {
						tableElem.appendChild(rowElem);
					}
				}
			}

			// Group
			Group[] groups = sheet.getGroups().toArray(new Group[0]);
			Arrays.sort(groups);
			for (Group group : groups) {
				Element groupElem = document.createElement("group");
				groupElem.setAttribute("name", group.getName());
				for (Cell cell : group.getCells()) {
					Element cellElem = document.createElement("cell");
					cellElem.setAttribute("name", cell.getName());
					groupElem.appendChild(cellElem);
				}
				sheetElem.appendChild(groupElem);
			}
		}

		return document;

	}

	/**
	 * 指定したセルを表すElementに、nameプロパティ以外のCellオブジェクトの情報をセットします。<br>
	 * nameプロパティはこのメソッドを呼び出す前にElementにセットしておいてください。
	 *
	 * @param document
	 *            Elementオブジェクトを作成したDocumentオブジェクト
	 * @param cellElement
	 *            情報をセットするセルを表すElementオブジェクト
	 * @param cell
	 *            セットする情報を持つCellオブジェクト
	 */
	private void setCellElement(Document document, Element cellElement,
			Cell cell) {

		String cellType = null;
		String cellValue = null;
		switch (cell.getCellType()) {
		case BOOLEAN:
			cellType = "bool";
			cellValue = cell.getValue().getBoolean().toString();
			break;
		case EMPTY:
			cellType = "empty";
			cellValue = "";
			break;
		case NUMBER:
			cellType = "num";
			cellValue = cell.getValue().getNumber().toString();
			break;
		case STRING:
			cellType = "str";
			cellValue = cell.getValue().getString();
			break;
		case DATE:
			cellType = "date";
			cellValue = DATE_FORMAT.format(cell.getValue().getDate());
			break;
		case FORMULA:
			cellType = "formula";
			cellValue = cell.getFormula();
			break;
		default:
			break;
		}
		cellElement.setAttribute("type", cellType);
		cellElement.appendChild(document.createCDATASection(cellValue));
	}
}
