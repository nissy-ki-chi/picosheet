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
package com.nissy_ki_chi.picosheet.function;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;

import com.nissy_ki_chi.picosheet.core.Element;
import com.nissy_ki_chi.picosheet.core.Function;
import com.nissy_ki_chi.picosheet.core.Resolver;
import com.nissy_ki_chi.picosheet.core.Element.ElementType;
import com.nissy_ki_chi.picosheet.core.Element.ErrorType;


public class MOD extends Function {

	MathContext roundMc = new MathContext(1, RoundingMode.FLOOR);

	@Override
	public Element call(Element[] elements, MathContext mc, Resolver resolver) {

		if (elements.length != 2) {
			return Element.newElement(ElementType.ERROR, ErrorType.INVALID_FORMULA);
		}

		if (elements[0].getType() != ElementType.NUMBER ||
				elements[1].getType() != ElementType.NUMBER) {
			return Element.newElement(ElementType.ERROR, ErrorType.INVALID_FORMULA);
		}

		// ExcelのMODと同じ結果を返す
		BigDecimal target = elements[0].getNumber();
		BigDecimal divisor = elements[1].getNumber();
		return Element.newElement(ElementType.NUMBER, target.subtract(target.divide(divisor, mc).round(roundMc).multiply(divisor, mc), mc));
	}

}
