package de.kiwiwings.poi.ssview;

import java.io.IOException;

import org.exbin.utils.binary_data.ByteArrayEditableData;

import de.kiwiwings.poi.ssview.treemodel.TreeModelLoadException;

public interface BinarySource {
	ByteArrayEditableData getBinaryData() throws IOException, TreeModelLoadException;
}
