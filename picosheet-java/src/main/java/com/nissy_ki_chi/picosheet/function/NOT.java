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

import java.math.MathContext;

import com.nissy_ki_chi.picosheet.core.Element;
import com.nissy_ki_chi.picosheet.core.Function;
import com.nissy_ki_chi.picosheet.core.Resolver;
import com.nissy_ki_chi.picosheet.core.Element.ElementType;
import com.nissy_ki_chi.picosheet.core.Element.ErrorType;


public class NOT extends Function {

	@Override
	public Element call(Element[] elements, MathContext mc, Resolver resolver) {
		if (elements.length != 1) {
			return Element.newElement(ElementType.ERROR, ErrorType.INVALID_PARAMETER);
		}

		if (elements[0].getType() != ElementType.BOOLEAN) {
			return Element.newElement(ElementType.ERROR, ErrorType.INVALID_PARAMETER);
		}

		return Element.newElement(ElementType.BOOLEAN, new Boolean(!elements[0].getBoolean().booleanValue()));
	}

}
