package com.nissy_ki_chi.jpicosheet.core;

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
