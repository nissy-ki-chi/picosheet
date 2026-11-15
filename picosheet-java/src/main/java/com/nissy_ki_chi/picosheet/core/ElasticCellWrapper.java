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
package com.nissy_ki_chi.picosheet.core;

public class ElasticCellWrapper {

	private final Cell _cell;
	private final ConcurrentBookWrapper _cbw;

	@SuppressWarnings("unused")
	private ElasticCellWrapper() {
		_cell = null;
		_cbw = null;
	}

	ElasticCellWrapper(Cell cell, ConcurrentBookWrapper cbw) {
		if (cell == null) {
			throw new NullPointerException("cell is null");
		}
		_cell = cell;
		_cbw = cbw;
	}

	public Element getValue() {
		return _cell.getValue();
	}

	public void setValue(String value) {
		_cell.setValue(value);
	}


}
