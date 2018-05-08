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

import de.kiwiwings.poi.visualizer.treemodel.TreeModelEntry;
import javafx.scene.control.TreeItem;
import org.apache.poi.poifs.filesystem.DirectoryNode;
import org.apache.poi.poifs.filesystem.Entry;

import java.io.IOException;

public class OLERootEntry extends OLEDirEntry {
	public OLERootEntry(final Entry dirEntry, final TreeItem<TreeModelEntry> treeNode) {
		super(dirEntry, treeNode);
	}

	@Override
	public void close() throws IOException {
		((DirectoryNode)this.entry).getFileSystem().close();
	}
	
	@Override
	public void activate() {
		if (surrugateEntry != null) {
			surrugateEntry.activate();
			setProperties();
		} else {
			super.activate();
		}
	}
}