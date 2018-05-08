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

package de.kiwiwings.poi.visualizer.treemodel.hssf;

import de.kiwiwings.poi.visualizer.treemodel.TreeModelDirNodeSource;
import de.kiwiwings.poi.visualizer.treemodel.TreeModelEntry;
import de.kiwiwings.poi.visualizer.treemodel.TreeModelLoadException;
import javafx.scene.control.TreeItem;
import org.apache.poi.hssf.model.InternalWorkbook;
import org.apache.poi.hssf.record.Record;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.poifs.filesystem.DirectoryNode;

import java.io.IOException;
import java.util.List;

import static de.kiwiwings.poi.visualizer.treemodel.TreeModelUtils.getNamedTreeNode;

public class HSSFTreeModel implements TreeModelDirNodeSource {

	private TreeItem<TreeModelEntry> parent;

	private HSSFWorkbook wb;

	@Override
	public void load(final TreeItem<TreeModelEntry> parent, final DirectoryNode source) throws TreeModelLoadException {
		boolean found = false;
		for (final String wbName : InternalWorkbook.WORKBOOK_DIR_ENTRY_NAMES) {
			if (source.hasEntry(wbName)) {
				found = true;
				break;
			}
		}

		if (!found) {
			throw new TreeModelLoadException("not a HSSF model");
		}


		try {
			wb = new HSSFWorkbook(source, true);
			final TreeItem<TreeModelEntry> wbNode = getNamedTreeNode(parent, InternalWorkbook.WORKBOOK_DIR_ENTRY_NAMES);
			HSSFRootEntry rootNode = new HSSFRootEntry(wb, wbNode);
			wbNode.setValue(rootNode);
			
			loadRecords(wbNode, wb.getInternalWorkbook().getRecords());
		} catch (IOException e) {
			throw new TreeModelLoadException("Can't load HSSF workbook",e);
		}
	}

	private void loadRecords(final TreeItem<TreeModelEntry> parentNode, final List<Record> records) {
		for (final Record r : records) {
			final TreeItem<TreeModelEntry> rNode = new TreeItem<>();
			final HSSFEntry entry = new HSSFEntry(r, rNode);
			rNode.setValue(entry);
			parentNode.getChildren().add(rNode);
		}
	}
}
