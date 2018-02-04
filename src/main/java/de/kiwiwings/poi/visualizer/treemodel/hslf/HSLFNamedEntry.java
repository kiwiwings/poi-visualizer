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

package de.kiwiwings.poi.visualizer.treemodel.hslf;

import static de.kiwiwings.poi.visualizer.treemodel.TreeModelUtils.escapeString;

import java.io.IOException;

import javax.swing.tree.DefaultMutableTreeNode;

import org.exbin.utils.binary_data.ByteArrayEditableData;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import de.kiwiwings.poi.visualizer.treemodel.TreeModelEntry;
import de.kiwiwings.poi.visualizer.treemodel.TreeObservable;
import de.kiwiwings.poi.visualizer.treemodel.TreeObservable.SourceType;

@Component(value="HSLFNamedEntry")
@Scope("prototype")
public class HSLFNamedEntry implements TreeModelEntry {

	private final String name;
	@SuppressWarnings("unused")
	private final DefaultMutableTreeNode treeNode;

	@Autowired
	TreeObservable treeObservable;

	
	public HSLFNamedEntry(final String name, final DefaultMutableTreeNode treeNode) {
		this.name = name;
		this.treeNode = treeNode;
	}


	@Override
	public String toString() {
		return escapeString(name);
	}

	
	@Override
	public void close() throws IOException {
	}

	@Override
	public void activate() {
		treeObservable.setBinarySource(() -> new ByteArrayEditableData());
		treeObservable.setSourceType(SourceType.empty);
		treeObservable.setFileName(null);
		treeObservable.setProperties("");
	}
}
