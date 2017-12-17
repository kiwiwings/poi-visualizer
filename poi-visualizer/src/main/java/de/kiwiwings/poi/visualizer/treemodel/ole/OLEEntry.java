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

import java.io.IOException;
import java.io.InputStream;

import javax.swing.tree.DefaultMutableTreeNode;

import org.apache.poi.poifs.filesystem.DirectoryNode;
import org.apache.poi.poifs.filesystem.DocumentNode;
import org.apache.poi.poifs.filesystem.Entry;
import org.exbin.utils.binary_data.ByteArrayEditableData;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import de.kiwiwings.poi.visualizer.treemodel.TreeModelEntry;
import de.kiwiwings.poi.visualizer.treemodel.TreeObservable;

@Component(value="OLEEntry")
@Scope("prototype")
public class OLEEntry implements TreeModelEntry {
	final Entry entry;
	final DefaultMutableTreeNode treeNode;
	final TreeModelEntry surrugateEntry;

	@Autowired
	TreeObservable treeObservable;
	
	public OLEEntry(final Entry entry, final DefaultMutableTreeNode treeNode) {
		this.entry = entry;
		this.treeNode = treeNode;
		Object oldUserObject = treeNode.getUserObject();
		surrugateEntry = (oldUserObject instanceof TreeModelEntry) ? (TreeModelEntry)oldUserObject : null;
	}
	@Override
	public String toString() {
		final String name = escapeString(entry.getName());
		return (treeNode.getParent() == null || surrugateEntry == null)
				? name : surrugateEntry+" ("+name+")";
	}

	@Override
	public void activate() {
		treeObservable.setBinarySource(() -> getData());
		treeObservable.setBinaryFileName(escapeString(entry.getName()));
		treeObservable.setStructuredSource(null);
		treeObservable.notifyObservers();
	}

	private ByteArrayEditableData getData() throws IOException {
		final DocumentNode dn = (DocumentNode)entry;
		final DirectoryNode parent = (DirectoryNode)dn.getParent();
		final ByteArrayEditableData data;
		try (final InputStream is = parent.createDocumentInputStream(dn)) {
			data = new ByteArrayEditableData();
			data.loadFromStream(is);
			return data;
		}
	}


	@Override
	public void close() throws IOException {

	}
}
