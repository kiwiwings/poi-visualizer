/* ====================================================================
   Licensed to the Apache Software Foundation (ASF) under one or more
   contributor license agreements.  See the NOTICE file distributed with
   this work for additional information regarding copyright ownership.
   The ASF licenses this file to You under the Apache License, Version 2.0
   (the "License"); you may not use this file except in compliance with
   the License.  You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
==================================================================== */

package de.kiwiwings.poi.visualizer.treemodel.hssf;

import static de.kiwiwings.poi.visualizer.treemodel.TreeModelUtils.getNamedTreeNode;

import java.io.IOException;
import java.util.List;

import javax.swing.tree.DefaultMutableTreeNode;

import org.apache.poi.hssf.model.InternalWorkbook;
import org.apache.poi.hssf.record.Record;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.poifs.filesystem.DirectoryNode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import de.kiwiwings.poi.visualizer.treemodel.TreeModelLoadException;
import de.kiwiwings.poi.visualizer.treemodel.TreeModelSource;

@Component
@Scope("prototype")
public class HSSFTreeModel implements TreeModelSource {

	private final DefaultMutableTreeNode parent;

	private HSSFWorkbook wb;

	@Autowired
	private ApplicationContext appContext;

	public HSSFTreeModel(final DefaultMutableTreeNode parent) {
		this.parent = parent;
	}

	@Override
	public void load(Object source) throws TreeModelLoadException {
		try {
			wb = new HSSFWorkbook((DirectoryNode)source, true);
			final DefaultMutableTreeNode wbNode = getNamedTreeNode(parent, InternalWorkbook.WORKBOOK_DIR_ENTRY_NAMES);
			HSSFRootEntry rootNode = appContext.getBean(HSSFRootEntry.class, wb, wbNode);
			wbNode.setUserObject(rootNode);
			
			loadRecords(wbNode, wb.getInternalWorkbook().getRecords());
		} catch (IOException e) {
			throw new TreeModelLoadException("Can't load HSSF workbook",e);
		}
	}

	private void loadRecords(final DefaultMutableTreeNode parentNode, final List<Record> records) {
		for (final Record r : records) {
			final DefaultMutableTreeNode rNode = new DefaultMutableTreeNode();
			final HSSFEntry entry = appContext.getBean(HSSFEntry.class, r, rNode);
			rNode.setUserObject(entry);
			parentNode.add(rNode);
		}
	}
}
