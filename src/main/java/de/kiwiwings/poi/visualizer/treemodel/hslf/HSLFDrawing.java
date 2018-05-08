/* ====================================================================
   Copyright 2017 Andreas Beeker (kiwiwings@apache.org)

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
==================================================================== */

package de.kiwiwings.poi.visualizer.treemodel.hslf;

import de.kiwiwings.poi.visualizer.treemodel.TreeModelEntry;
import de.kiwiwings.poi.visualizer.treemodel.TreeObservable;
import de.kiwiwings.poi.visualizer.treemodel.TreeObservable.SourceType;
import javafx.scene.control.TreeItem;
import org.apache.poi.hslf.record.PPDrawing;
import org.apache.poi.hslf.record.Record;
import org.exbin.utils.binary_data.ByteArrayEditableData;

import java.io.IOException;
import java.io.OutputStream;

import static de.kiwiwings.poi.visualizer.treemodel.TreeModelUtils.escapeString;
import static de.kiwiwings.poi.visualizer.treemodel.TreeModelUtils.reflectProperties;

public class HSLFDrawing implements TreeModelEntry {

	private final PPDrawing drawing;
	@SuppressWarnings("unused")
	private final TreeItem<TreeModelEntry> treeNode;

    final TreeObservable treeObservable = TreeObservable.getInstance();

	
	public HSLFDrawing(final Record drawing, final TreeItem<TreeModelEntry> treeNode) {
		this.drawing = (PPDrawing)drawing;
		this.treeNode = treeNode;
	}


	@Override
	public String toString() {
		return escapeString(drawing.getClass().getSimpleName());
	}

	
	@Override
	public void close() throws IOException {
	}

	@Override
	public void activate() {
		treeObservable.setBinarySource(() -> getData());
		treeObservable.setSourceType(SourceType.octet);
		treeObservable.setFileName(toString()+".rec");
		treeObservable.setProperties(reflectProperties(drawing));
	}

	private ByteArrayEditableData getData() throws IOException {
		final ByteArrayEditableData data = new ByteArrayEditableData();
		try (final OutputStream os = data.getDataOutputStream()) {
			drawing.writeOut(os);
		}
		return data;
	}
}
