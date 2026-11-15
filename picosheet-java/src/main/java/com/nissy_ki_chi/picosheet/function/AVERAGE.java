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
/**
 *
 */
package com.nissy_ki_chi.picosheet.function;

import java.math.BigDecimal;
import java.math.MathContext;
import java.util.Collection;

import com.nissy_ki_chi.picosheet.core.Cell;
import com.nissy_ki_chi.picosheet.core.Element;
import com.nissy_ki_chi.picosheet.core.Function;
import com.nissy_ki_chi.picosheet.core.ReferenceNotFoundException;
import com.nissy_ki_chi.picosheet.core.Resolver;
import com.nissy_ki_chi.picosheet.core.Element.ElementType;
import com.nissy_ki_chi.picosheet.core.Element.ErrorType;


/**
 * @author yusuke nishikawa
 *
 */
public class AVERAGE extends Function {

	/* (非 Javadoc)
	 * @see com.nissy_ki_chi.picosheet.function.Function#call(com.nissy_ki_chi.picosheet.core.Element[], java.math.MathContext, com.nissy_ki_chi.picosheet.core.Resolver)
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
