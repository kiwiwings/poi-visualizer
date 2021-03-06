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

package de.kiwiwings.poi.visualizer.treemodel.ole;

import de.kiwiwings.poi.visualizer.DocumentFragment;
import de.kiwiwings.poi.visualizer.DocumentFragment.SourceType;
import de.kiwiwings.poi.visualizer.treemodel.TreeModelEntry;
import de.kiwiwings.poi.visualizer.treemodel.TreeModelLoadException;
import javafx.scene.control.TreeItem;
import org.apache.poi.hpsf.NoPropertySetStreamException;
import org.apache.poi.hpsf.PropertySet;
import org.apache.poi.hpsf.PropertySetFactory;
import org.apache.poi.poifs.filesystem.DirectoryNode;
import org.apache.poi.poifs.filesystem.DocumentNode;
import org.apache.poi.poifs.filesystem.Entry;
import org.exbin.utils.binary_data.ByteArrayEditableData;

import java.io.IOException;
import java.io.InputStream;

import static de.kiwiwings.poi.visualizer.treemodel.TreeModelUtils.escapeString;
import static de.kiwiwings.poi.visualizer.treemodel.TreeModelUtils.reflectProperties;

public class OLEPropertySet implements TreeModelEntry {
	Entry entry;
	PropertySet propertySet;
	final TreeItem<TreeModelEntry> treeNode;

	public OLEPropertySet(final Entry entry, final TreeItem<TreeModelEntry> treeNode) throws TreeModelLoadException {
		this.entry = entry;
		this.treeNode = treeNode;
		try {
			this.propertySet = PropertySetFactory.create(entry.getParent(), entry.getName());
		} catch (NoPropertySetStreamException | IOException e) {
			throw new TreeModelLoadException("Can't load property set", e);
		}
	}

	@Override
	public String toString() {
		return escapeString(entry.getName());
	}

	@Override
	public void activate(final DocumentFragment fragment) {
		fragment.setBinarySource(() -> getData());
		fragment.setSourceType(SourceType.octet);
		fragment.setFileName(escapeString(entry.getName()));
		setProperties(fragment);
	}

	protected void setProperties(final DocumentFragment fragment) {
		fragment.setProperties(reflectProperties(propertySet));
	}

	private ByteArrayEditableData getData() throws IOException {
		final DocumentNode dn = (DocumentNode)entry;
		final DirectoryNode parent = (DirectoryNode)dn.getParent();

		final ByteArrayEditableData data;
		try (final InputStream is = parent.createDocumentInputStream(dn)) {
			data = new ByteArrayEditableData();
			data.loadFromStream(is);
			return data;
		}
	}


	@Override
	public void close() throws IOException {

	}
}
