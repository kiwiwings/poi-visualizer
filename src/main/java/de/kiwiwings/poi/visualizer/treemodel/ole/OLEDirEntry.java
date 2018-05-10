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
import de.kiwiwings.poi.visualizer.treemodel.TreeModelEntry;
import de.kiwiwings.poi.visualizer.DocumentFragment.SourceType;
import javafx.scene.control.TreeItem;
import org.apache.poi.hpsf.ClassID;
import org.apache.poi.poifs.filesystem.DirectoryNode;
import org.apache.poi.poifs.filesystem.DocumentNode;
import org.apache.poi.poifs.filesystem.Entry;
import org.exbin.utils.binary_data.ByteArrayEditableData;

import javax.json.Json;
import javax.json.JsonObjectBuilder;

import static de.kiwiwings.poi.visualizer.treemodel.TreeModelUtils.escapeString;

public class OLEDirEntry extends OLEEntry {
	OLEDirEntry(final Entry entry, final TreeItem<TreeModelEntry> treeNode) {
		super(entry, treeNode);
		if (!(entry instanceof DirectoryNode)) {
			throw new IllegalArgumentException("not a DirectoryNode");
		}
	}

	@Override
	public void activate(final DocumentFragment fragment) {
		fragment.setBinarySource(() -> new ByteArrayEditableData());
		fragment.setSourceType(SourceType.empty);
		setProperties(fragment);
	}
	
	@Override
	protected void setProperties(final DocumentFragment fragment) {
		final DirectoryNode dirNode = (DirectoryNode)entry;

		final JsonObjectBuilder jsonBuilder = Json.createObjectBuilder();
		ClassID storageClsid = dirNode.getStorageClsid();
		if (storageClsid == null) {
			jsonBuilder.addNull("storage_clsid");
		} else {
			jsonBuilder.add("storage_clsid", storageClsid.toString());
		}
		
		dirNode.forEach(e -> {
			if (e instanceof DocumentNode) {
				DocumentNode dn = (DocumentNode)e;
				jsonBuilder.add(escapeString(dn.getName()), "Size: "+dn.getSize());
			}
		});
		
		final String props = jsonBuilder.build().toString();
		if (surrugateEntry != null) {
			fragment.mergeProperties(props);
		} else {
			fragment.setProperties(props);
		}
	}
}