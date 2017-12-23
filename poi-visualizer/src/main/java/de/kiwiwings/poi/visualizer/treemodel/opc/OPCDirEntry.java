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

import static de.kiwiwings.poi.visualizer.treemodel.TreeModelUtils.escapeString;

import java.io.IOException;

import javax.swing.tree.DefaultMutableTreeNode;

import org.exbin.utils.binary_data.ByteArrayEditableData;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import de.kiwiwings.poi.visualizer.treemodel.TreeModelEntry;
import de.kiwiwings.poi.visualizer.treemodel.TreeObservable;
import de.kiwiwings.poi.visualizer.treemodel.TreeObservable.SourceType;

@Component(value="OPCDirEntry")
@Primary
@Scope("prototype")
public class OPCDirEntry implements TreeModelEntry {
	final String path;
	@SuppressWarnings("unused")
	final DefaultMutableTreeNode treeNode;

	@Autowired
	TreeObservable treeObservable;
	
	public OPCDirEntry(final String path, final DefaultMutableTreeNode treeNode) {
		this.path = path;
		this.treeNode = treeNode;
	}

	@Override
	public String toString() {
		return escapeString(path.substring(path.lastIndexOf('/')+1));
	}

	@Override
	public void activate() {
		treeObservable.setBinarySource(() -> new ByteArrayEditableData());
		treeObservable.setSourceType(SourceType.empty);
		setProperties();
	}

	protected void setProperties() {
		treeObservable.setProperties(null);
	}
	
	@Override
	public void close() throws IOException {
	}
	
	public String getPath() {
		return path;
	}
}

