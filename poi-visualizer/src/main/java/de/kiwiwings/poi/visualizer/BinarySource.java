package de.kiwiwings.poi.visualizer;

import java.io.IOException;

import org.exbin.utils.binary_data.ByteArrayEditableData;

import de.kiwiwings.poi.visualizer.treemodel.TreeModelLoadException;

public interface BinarySource {
	ByteArrayEditableData getBinaryData() throws IOException, TreeModelLoadException;
}
