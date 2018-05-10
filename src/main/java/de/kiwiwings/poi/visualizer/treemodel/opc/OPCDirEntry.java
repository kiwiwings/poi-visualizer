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

package de.kiwiwings.poi.visualizer.treemodel.opc;

import de.kiwiwings.poi.visualizer.DocumentFragment;
import de.kiwiwings.poi.visualizer.treemodel.TreeModelEntry;
import de.kiwiwings.poi.visualizer.DocumentFragment.SourceType;
import javafx.scene.control.TreeItem;
import org.exbin.utils.binary_data.ByteArrayEditableData;

import java.io.IOException;

import static de.kiwiwings.poi.visualizer.treemodel.TreeModelUtils.escapeString;

public class OPCDirEntry implements TreeModelEntry {
	final String path;
	final TreeItem<TreeModelEntry> treeNode;
	final TreeModelEntry surrugateEntry;

	public OPCDirEntry(final String path, final TreeItem<TreeModelEntry> treeNode) {
		this.path = path;
		this.treeNode = treeNode;
		surrugateEntry = treeNode.getValue();
	}

	@Override
	public String toString() {
		final String name = escapeString(path.substring(path.lastIndexOf('/')+1));
		return (treeNode.getParent() == null || surrugateEntry == null)
				? name : surrugateEntry+" ("+name+")";
	}

	@Override
	public void activate(final DocumentFragment fragment) {
		if (surrugateEntry != null) {
			surrugateEntry.activate(fragment);
		} else {
			fragment.setBinarySource(() -> new ByteArrayEditableData());
			fragment.setSourceType(SourceType.empty);
		}
		
		setProperties(fragment);
	}

	protected void setProperties(final DocumentFragment fragment) {
		fragment.setProperties(null);
	}
	
	@Override
	public void close() throws IOException {
	}
	
	public String getPath() {
		return path;
	}
}

