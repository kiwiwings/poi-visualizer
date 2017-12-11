package de.kiwiwings.poi.visualizer.treemodel;

public class TreeModelLoadException extends Exception {

	private static final long serialVersionUID = 1L;

	public TreeModelLoadException() {
		super();
	}

	public TreeModelLoadException(String message, Throwable cause, boolean enableSuppression,
			boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}

	public TreeModelLoadException(String message, Throwable cause) {
		super(message, cause);
	}

	public TreeModelLoadException(String message) {
		super(message);
	}

	public TreeModelLoadException(Throwable cause) {
		super(cause);
	}

}
