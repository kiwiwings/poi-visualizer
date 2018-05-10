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

import de.kiwiwings.poi.visualizer.DocumentFragment;
import de.kiwiwings.poi.visualizer.treemodel.TreeModelEntry;
import de.kiwiwings.poi.visualizer.DocumentFragment.SourceType;
import javafx.scene.control.TreeItem;
import org.apache.poi.hpsf.PropertySet;
import org.apache.poi.hpsf.WritingNotSupportedException;
import org.exbin.utils.binary_data.ByteArrayEditableData;

import java.io.IOException;
import java.io.OutputStream;

import static de.kiwiwings.poi.visualizer.treemodel.TreeModelUtils.reflectProperties;

public class HPSFPropertySet implements TreeModelEntry {

	private final PropertySet propertySet;
	@SuppressWarnings("unused")
	private final TreeItem<TreeModelEntry> treeNode;
	final TreeModelEntry surrugateEntry;

	public HPSFPropertySet(final PropertySet propertySet, final TreeItem<TreeModelEntry> treeNode) {
		this.propertySet = propertySet;
		this.treeNode = treeNode;
		surrugateEntry = treeNode.getValue();
	}

	@Override
	public String toString() {
		return surrugateEntry.toString();
	}

	
	@Override
	public void close() throws IOException {
	}

	@Override
	public void activate(final DocumentFragment fragment) {
		fragment.setBinarySource(() -> getData());
		fragment.setSourceType(SourceType.octet);
		fragment.setFileName(toString()+".rec");
		fragment.setProperties(reflectProperties(propertySet));
	}

	private ByteArrayEditableData getData() throws IOException {
		final ByteArrayEditableData data = new ByteArrayEditableData();
		try (final OutputStream os = data.getDataOutputStream()) {
			propertySet.write(os);
		} catch (WritingNotSupportedException e) {
			throw new IOException(e);
		}
		return data;
	}
}
