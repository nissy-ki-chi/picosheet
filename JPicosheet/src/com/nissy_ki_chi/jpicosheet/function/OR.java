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

import java.math.MathContext;

import com.nissy_ki_chi.jpicosheet.core.Element;
import com.nissy_ki_chi.jpicosheet.core.Function;
import com.nissy_ki_chi.jpicosheet.core.Resolver;
import com.nissy_ki_chi.jpicosheet.core.Element.ElementType;
import com.nissy_ki_chi.jpicosheet.core.Element.ErrorType;


public class OR extends Function {

	@Override
	public Element call(Element[] elements, MathContext mc, Resolver resolver) {

		boolean isFirsttime = true;
		boolean result = false;
		for (Element e: elements) {
			if (e.getType() != ElementType.BOOLEAN) {
				return Element.newElement(ElementType.ERROR, ErrorType.INVALID_PARAMETER);
			} else {
				if (isFirsttime) {
					result = e.getBoolean().booleanValue();
					isFirsttime = false;
				} else {
					result = result || e.getBoolean().booleanValue();
				}
			}
		}

		return Element.newElement(ElementType.BOOLEAN, new Boolean(result));
	}

}
