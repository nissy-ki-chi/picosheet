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
package com.nissy_ki_chi.jpicosheet.function;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;

import com.nissy_ki_chi.jpicosheet.core.Element;
import com.nissy_ki_chi.jpicosheet.core.Function;
import com.nissy_ki_chi.jpicosheet.core.Resolver;
import com.nissy_ki_chi.jpicosheet.core.Element.ElementType;
import com.nissy_ki_chi.jpicosheet.core.Element.ErrorType;


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
