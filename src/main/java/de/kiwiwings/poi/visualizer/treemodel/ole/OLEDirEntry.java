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

import static de.kiwiwings.poi.visualizer.treemodel.TreeModelUtils.escapeString;

import javax.json.Json;
import javax.json.JsonObjectBuilder;
import javax.swing.tree.DefaultMutableTreeNode;

import org.apache.poi.hpsf.ClassID;
import org.apache.poi.poifs.filesystem.DirectoryNode;
import org.apache.poi.poifs.filesystem.DocumentNode;
import org.exbin.utils.binary_data.ByteArrayEditableData;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import de.kiwiwings.poi.visualizer.treemodel.TreeObservable.SourceType;

@Component(value="OLEDirEntry")
@Scope("prototype")
public class OLEDirEntry extends OLEEntry {
	public OLEDirEntry(final DirectoryNode dirEntry, final DefaultMutableTreeNode treeNode) {
		super(dirEntry, treeNode);
	}

	@Override
	public void activate() {
		treeObservable.setBinarySource(() -> new ByteArrayEditableData());
		treeObservable.setSourceType(SourceType.empty);
		setProperties();
	}
	
	@Override
	protected void setProperties() {
		final DirectoryNode dirNode = (DirectoryNode)entry;
		treeObservable.setProperties(null);

		final JsonObjectBuilder jsonBuilder = Json.createObjectBuilder();
		ClassID storageClsid = dirNode.getStorageClsid();
		if (storageClsid != null) {
			jsonBuilder.add("storage_clsid", storageClsid.toString());
		}
		
		dirNode.forEach(e -> {
			if (e instanceof DocumentNode) {
				DocumentNode dn = (DocumentNode)e;
				jsonBuilder.add(escapeString(dn.getName()), "Size: "+dn.getSize());
			}
		});
		
		treeObservable.setProperties(jsonBuilder.build().toString());
	}
}