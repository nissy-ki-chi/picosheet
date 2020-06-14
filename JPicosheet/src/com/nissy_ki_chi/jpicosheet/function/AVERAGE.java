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
/**
 *
 */
package com.nissy_ki_chi.jpicosheet.function;

import java.math.BigDecimal;
import java.math.MathContext;
import java.util.Collection;

import com.nissy_ki_chi.jpicosheet.core.Cell;
import com.nissy_ki_chi.jpicosheet.core.Element;
import com.nissy_ki_chi.jpicosheet.core.Function;
import com.nissy_ki_chi.jpicosheet.core.ReferenceNotFoundException;
import com.nissy_ki_chi.jpicosheet.core.Resolver;
import com.nissy_ki_chi.jpicosheet.core.Element.ElementType;
import com.nissy_ki_chi.jpicosheet.core.Element.ErrorType;


/**
 * @author yusuke nishikawa
 *
 */
public class AVERAGE extends Function {

	/* (非 Javadoc)
	 * @see com.nissy_ki_chi.jpicosheet.function.Function#call(com.nissy_ki_chi.jpicosheet.core.Element[], java.math.MathContext, com.nissy_ki_chi.jpicosheet.core.Resolver)
	 */
	@Override
	public Element call(Element[] elements, MathContext mc, Resolver resolver) {

		int summaryCnt = 0;

		// elementsの値の平均を返す
		// 加算の対象とするのはNUMBER、BOOLEANのセルのみ
		BigDecimal summary = BigDecimal.ZERO;
		for(Element elem: elements) {
			try {
				switch(elem.getType()) {
				case NUMBER:
				case BOOLEAN:
					summary = summary.add(elem.getNumber(), mc);
					summaryCnt++;
					break;
				case GROUP_REFERENCE:
				case TABLE_REFERENCE:
					// Elementがグループ参照またはテーブル参照だった場合、参照に含まれるセルを取得の上、合算する
					Collection<Cell> collection = null;
					if (elem.getType() == ElementType.GROUP_REFERENCE) {
						collection = resolver.getCellsFromGroup(getGroupReference(elem));
					} else if (elem.getType() == ElementType.TABLE_REFERENCE) {
						collection = resolver.getCellsFromTable(getTableReference(elem));
					}
					for (Cell cell: collection) {
						if (cell.getValueType() == ElementType.NUMBER ||
								cell.getValueType() == ElementType.BOOLEAN) {
							summary = summary.add(cell.getValue().getNumber());
							summaryCnt++;
						}
					}
				}
			} catch (ReferenceNotFoundException e) {
				return Element.newElement(ElementType.ERROR, ErrorType.INVALID_REFERENCES);
			}
		}

		return Element.newElement(ElementType.NUMBER, summary.divide(new BigDecimal(summaryCnt), mc));
	}


}
