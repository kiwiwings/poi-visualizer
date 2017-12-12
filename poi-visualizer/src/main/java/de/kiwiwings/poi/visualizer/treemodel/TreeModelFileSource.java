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

import javax.swing.JOptionPane;
import javax.swing.tree.DefaultMutableTreeNode;

import org.apache.poi.poifs.filesystem.FileMagic;
import org.apache.poi.poifs.filesystem.POIFSFileSystem;

public class TreeModelFileSource implements TreeModelSource {
	
	final DefaultMutableTreeNode parent;
	final TreeObservable treeObservable;
	
	public TreeModelFileSource(final DefaultMutableTreeNode parent, final TreeObservable treeObservable) {
		this.parent = parent;
		this.treeObservable = treeObservable;
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

		switch (fm) {
		case OLE2:
			new TreeModelPOIFSSource(parent, treeObservable).load(source);
			break;
		case OOXML:
			new TreeModelOPCSource(parent, treeObservable).load(source);
			break;
		default:
			throw new TreeModelLoadException("File with file magic '"+fm+"' can't be processed.");
		}
	}
}
