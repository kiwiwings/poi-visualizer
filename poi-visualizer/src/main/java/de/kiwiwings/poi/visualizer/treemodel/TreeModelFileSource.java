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

package de.kiwiwings.poi.visualizer.treemodel;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import javax.swing.tree.DefaultMutableTreeNode;

import org.apache.poi.poifs.filesystem.FileMagic;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import de.kiwiwings.poi.visualizer.treemodel.ole.OLETreeModel;
import de.kiwiwings.poi.visualizer.treemodel.opc.OPCTreeModel;


@Component
@Scope("prototype")
public class TreeModelFileSource implements TreeModelSource {
	
	final DefaultMutableTreeNode parent;

	@Autowired
	private ApplicationContext appContext;
	
	public TreeModelFileSource(final DefaultMutableTreeNode parent) {
		this.parent = parent;
	}
	
	public void load(final Object source) throws TreeModelLoadException {
		if (!(source instanceof File)) {
			throw new TreeModelLoadException("source isn't a file.");
		}
		
		final File file = (File)source;
		
		final FileMagic fm;
		try (InputStream is = FileMagic.prepareToCheckMagic(new FileInputStream(file))) {
			fm = FileMagic.valueOf(is);
		} catch (IOException ex) {
			throw new TreeModelLoadException("File Magic can't be determined.", ex);
		}

		final Class<? extends TreeModelSource> treeModelCls;
		switch (fm) {
		case OLE2:
			treeModelCls = OLETreeModel.class;
			break;
		case OOXML:
			treeModelCls = OPCTreeModel.class;
			break;
		default:
			throw new TreeModelLoadException("File with file magic '"+fm+"' can't be processed.");
		}

		appContext.getBean(treeModelCls, parent).load(source);
	}
}
