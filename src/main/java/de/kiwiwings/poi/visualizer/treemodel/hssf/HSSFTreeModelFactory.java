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

import javax.swing.tree.DefaultMutableTreeNode;

import org.apache.poi.hssf.model.InternalWorkbook;
import org.apache.poi.poifs.filesystem.POIFSFileSystem;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import de.kiwiwings.poi.visualizer.treemodel.TreeModelSource;
import de.kiwiwings.poi.visualizer.treemodel.ole.OLETreeModelFactory;

@Component
public class HSSFTreeModelFactory implements OLETreeModelFactory {

	@Autowired
	private ApplicationContext appContext;

	@Override
	public TreeModelSource create(final POIFSFileSystem poifs, final DefaultMutableTreeNode parent) {
		for (final String wbName : InternalWorkbook.WORKBOOK_DIR_ENTRY_NAMES) {
			if (poifs.getRoot().hasEntry(wbName)) {
				return appContext.getBean(HSSFTreeModel.class, parent);
			}
		}
		
		return null;
	}
}
