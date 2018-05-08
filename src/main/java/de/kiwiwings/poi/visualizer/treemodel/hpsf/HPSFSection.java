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

package de.kiwiwings.poi.visualizer.treemodel.hpsf;

import de.kiwiwings.poi.visualizer.treemodel.TreeModelEntry;
import de.kiwiwings.poi.visualizer.treemodel.TreeObservable;
import de.kiwiwings.poi.visualizer.treemodel.TreeObservable.SourceType;
import javafx.scene.control.TreeItem;
import org.apache.poi.hpsf.Section;
import org.apache.poi.hpsf.WritingNotSupportedException;
import org.exbin.utils.binary_data.ByteArrayEditableData;

import java.io.IOException;
import java.io.OutputStream;

import static de.kiwiwings.poi.visualizer.treemodel.TreeModelUtils.reflectProperties;

public class HPSFSection implements TreeModelEntry {

	private final Section section;
	@SuppressWarnings("unused")
	private final TreeItem<TreeModelEntry> treeNode;

    TreeObservable treeObservable = TreeObservable.getInstance();

	public HPSFSection(final Section section, final TreeItem<TreeModelEntry> treeNode) {
		this.section = section;
		this.treeNode = treeNode;
	}

	@Override
	public String toString() {
		return "Section";
	}

	
	@Override
	public void close() throws IOException {
	}

	@Override
	public void activate() {
		treeObservable.setBinarySource(() -> getData());
		treeObservable.setSourceType(SourceType.octet);
		treeObservable.setFileName(toString()+".rec");
		treeObservable.setProperties(reflectProperties(section));
	}

	private ByteArrayEditableData getData() throws IOException {
		final ByteArrayEditableData data = new ByteArrayEditableData();
		try (final OutputStream os = data.getDataOutputStream()) {
			section.write(os);
		} catch (WritingNotSupportedException e) {
			throw new IOException(e);
		}
		return data;
	}
}
