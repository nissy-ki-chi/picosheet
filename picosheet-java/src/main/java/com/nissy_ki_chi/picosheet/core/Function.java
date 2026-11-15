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
package com.nissy_ki_chi.picosheet.core;

import java.math.MathContext;



/**
 * @author yusuke nishikawa
 *
 */
public abstract class Function {

	public abstract Element call(Element[] elements, MathContext mc, Resolver resolver);

	protected String getCellReference(Element elem) {
		return elem.getCellReference();
	}
	protected String getGroupReference(Element elem) {
		return elem.getGroupReference();
	}

	protected String getTableReference(Element elem) {
		return elem.getTableReference();
	}
}
