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

package de.kiwiwings.poi.visualizer.treemodel.opc;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.tree.DefaultMutableTreeNode;

import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.openxml4j.opc.OPCPackage;
import org.apache.poi.openxml4j.opc.PackageAccess;
import org.apache.poi.openxml4j.opc.PackagePart;
import org.apache.poi.util.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import de.kiwiwings.poi.visualizer.treemodel.TreeModelLoadException;
import de.kiwiwings.poi.visualizer.treemodel.TreeModelSource;

@Component
@Scope("prototype")
public class OPCTreeModel implements TreeModelSource {
	
	final DefaultMutableTreeNode parent;

	@Autowired
	private ApplicationContext appContext;
	
	public OPCTreeModel(final DefaultMutableTreeNode parent) {
		this.parent = parent;
	}
	
	public void load(Object source) throws TreeModelLoadException {
		if (!(source instanceof File)) {
			throw new TreeModelLoadException("source isn't a file.");
		}
		
		OPCPackage opc = null;
		try {
			opc = OPCPackage.open((File)source, PackageAccess.READ);
			OPCRootEntry opcRoot = appContext.getBean(OPCRootEntry.class, opc, parent);
			parent.setUserObject(opcRoot);

			final Map<String,DefaultMutableTreeNode> mapFolders = new HashMap<>();
			final Map<String,List<DefaultMutableTreeNode>> mapFiles = new HashMap<>();
			mapFolders.put("/", parent);
			
			// first create the folders, so we don't have folders and files mixed in the tree
			for (final PackagePart pp : opc.getParts()) {
				final String uri = pp.getPartName().toString();
				DefaultMutableTreeNode parDir = parent;
				for (int idx=1;(idx=uri.indexOf('/',idx)) != -1;idx++) {
					final String path = uri.substring(0,idx);
					final DefaultMutableTreeNode dir;
					if (mapFolders.containsKey(path)) {
						dir = mapFolders.get(path);
					} else {
						dir = new DefaultMutableTreeNode();
						final OPCDirEntry entry = appContext.getBean(OPCDirEntry.class, path, dir);
						dir.setUserObject(entry);
						mapFolders.put(path, dir);
						parDir.add(dir);
					}
					parDir = dir;
				}

				// temporarily store the entries
				final String parPath = ((OPCDirEntry)parDir.getUserObject()).getPath();
				final List<DefaultMutableTreeNode> listFiles;
				if (mapFiles.containsKey(parPath)) {
					listFiles = mapFiles.get(parPath);
				} else {
					listFiles = new ArrayList<DefaultMutableTreeNode>();
					mapFiles.put(parPath, listFiles);
				}
				final DefaultMutableTreeNode node = new DefaultMutableTreeNode();
				node.setUserObject(appContext.getBean(OPCEntry.class, pp, node));
				listFiles.add(node);
			}


			// then add the items
			mapFiles.entrySet().stream().forEach(me -> {
				final DefaultMutableTreeNode parDir = mapFolders.get(me.getKey());
				me.getValue().forEach(n -> parDir.add(n));
			});
		} catch (InvalidFormatException ex) {
			IOUtils.closeQuietly(opc);
			throw new TreeModelLoadException("Error in opening '"+((File)source).getPath()+"'");
		}

	
	}
}
