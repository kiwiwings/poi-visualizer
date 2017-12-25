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

import javax.swing.tree.DefaultMutableTreeNode;

import org.apache.poi.poifs.filesystem.DirectoryNode;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@Component(value="OLERootEntry")
@Scope("prototype")
public class OLERootEntry extends OLEDirEntry {
	public OLERootEntry(final DirectoryNode dirEntry, final DefaultMutableTreeNode treeNode) {
		super(dirEntry, treeNode);
	}

	@Override
	public void close() throws IOException {
		((DirectoryNode)this.entry).getFileSystem().close();
	}
	
	@Override
	public void activate() {
		if (surrugateEntry != null) {
			surrugateEntry.activate();
			setProperties();
		} else {
			super.activate();
		}
	}
}