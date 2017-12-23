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

package de.kiwiwings.poi.visualizer.treemodel.ole;

import java.io.File;
import java.io.IOException;
import java.util.Set;

import javax.swing.tree.DefaultMutableTreeNode;

import org.apache.poi.hslf.usermodel.HSLFSlideShow;
import org.apache.poi.poifs.filesystem.DirectoryNode;
import org.apache.poi.poifs.filesystem.Entry;
import org.apache.poi.poifs.filesystem.POIFSFileSystem;
import org.apache.poi.util.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import de.kiwiwings.poi.visualizer.treemodel.TreeModelLoadException;
import de.kiwiwings.poi.visualizer.treemodel.TreeModelSource;
import de.kiwiwings.poi.visualizer.treemodel.hslf.HSLFTreeModel;

@Component
@Scope("prototype")
public class OLETreeModel implements TreeModelSource {

	final DefaultMutableTreeNode parent;

	@Autowired
	private ApplicationContext appContext;
	
	public OLETreeModel(final DefaultMutableTreeNode parent) {
		this.parent = parent;
	}

	@SuppressWarnings("resource")
	public void load(Object source) throws TreeModelLoadException {
		if (!(source instanceof File)) {
			throw new TreeModelLoadException("source isn't a file.");
		}
		
		POIFSFileSystem poifs = null;
		try {
			poifs = new POIFSFileSystem((File)source);
			traverseFileSystem(poifs.getRoot(), parent);
		} catch (IOException ex) {
			IOUtils.closeQuietly(poifs);
			throw new TreeModelLoadException("Error in opening '"+((File)source).getPath()+"'");
		}
	}

	private void traverseFileSystem(Entry poifsNode, DefaultMutableTreeNode treeNode) throws TreeModelLoadException {
		final String qualifier;
		if (poifsNode.getParent() == null) {
			qualifier = "OLERootEntry";
		} else if (poifsNode instanceof DirectoryNode) {
			qualifier = "OLEDirEntry";
		} else {
			qualifier = "OLEEntry";
		}

		treeNode.setUserObject(appContext.getBean(qualifier, poifsNode, treeNode));
		if (poifsNode instanceof DirectoryNode) {
			for (Entry poifsChild : ((DirectoryNode)poifsNode)) {
				DefaultMutableTreeNode treeChild = new DefaultMutableTreeNode();
				treeNode.add(treeChild);
				traverseFileSystem(poifsChild, treeChild);
			}
		}

		if (poifsNode.getParent() != null) {
			return;
		}

		
		Set<String> entryNames = ((DirectoryNode)poifsNode).getEntryNames();
		final Class<? extends TreeModelSource> innerModelCls;
		if (entryNames.contains(HSLFSlideShow.POWERPOINT_DOCUMENT)) {
			innerModelCls = HSLFTreeModel.class;
		} else {
			return;
		}
		
		TreeModelSource innerModel = appContext.getBean(innerModelCls, treeNode);
		innerModel.load(poifsNode);
	}
}





