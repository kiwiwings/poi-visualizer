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

package de.kiwiwings.poi.visualizer.treemodel.hpsf;

import javax.swing.tree.DefaultMutableTreeNode;

import org.apache.poi.hpsf.DocumentSummaryInformation;
import org.apache.poi.hpsf.SummaryInformation;
import org.apache.poi.poifs.filesystem.DirectoryNode;
import org.apache.poi.poifs.filesystem.POIFSFileSystem;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import de.kiwiwings.poi.visualizer.treemodel.TreeModelSource;
import de.kiwiwings.poi.visualizer.treemodel.ole.OLETreeModelFactory;

@Component
public class HPSFTreeModelFactory implements OLETreeModelFactory {

	@Autowired
	private ApplicationContext appContext;

	@Override
	public TreeModelSource create(final POIFSFileSystem poifs, final DefaultMutableTreeNode parent) {
		final DirectoryNode dn = poifs.getRoot();
		if (dn.hasEntry(DocumentSummaryInformation.DEFAULT_STREAM_NAME) ||
			dn.hasEntry(SummaryInformation.DEFAULT_STREAM_NAME)) {
			return appContext.getBean(HPSFTreeModel.class, parent);
		}
		return null;
	}
}
