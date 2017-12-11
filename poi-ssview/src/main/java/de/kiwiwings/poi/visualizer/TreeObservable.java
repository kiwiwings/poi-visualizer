package de.kiwiwings.poi.visualizer;

import java.util.Observable;

public class TreeObservable extends Observable {
	private BinarySource binarySource;
	private StructuredSource structuredSource;

	public BinarySource getBinarySource() {
		return binarySource;
	}

	public void setBinarySource(BinarySource binarySource) {
		this.binarySource = binarySource;
		setChanged();
	}

	public StructuredSource getStructuredSource() {
		return structuredSource;
	}

	public void setStructuredSource(StructuredSource structuredSource) {
		this.structuredSource = structuredSource;
		setChanged();
	}

}
