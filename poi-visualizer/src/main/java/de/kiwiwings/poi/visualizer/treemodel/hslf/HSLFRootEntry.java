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

package de.kiwiwings.poi.visualizer.treemodel.hslf;

import java.io.IOException;

import javax.swing.tree.DefaultMutableTreeNode;

import org.apache.poi.hslf.usermodel.HSLFSlideShow;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import de.kiwiwings.poi.visualizer.treemodel.TreeModelEntry;


@Component(value="HSLFRootEntry")
@Scope("prototype")
public class HSLFRootEntry implements TreeModelEntry {
	HSLFSlideShow ppt;
	final DefaultMutableTreeNode treeNode;
	final TreeModelEntry surrugateEntry;
	
	public HSLFRootEntry(HSLFSlideShow ppt, DefaultMutableTreeNode treeNode) {
		this.ppt = ppt;
		this.treeNode = treeNode;
		Object oldUserObject = treeNode.getUserObject();
		surrugateEntry = (oldUserObject instanceof TreeModelEntry) ? (TreeModelEntry)oldUserObject : null;
	}

	@Override
	public String toString() {
		final String name = "SlideShow";
		return (treeNode.getParent() == null || surrugateEntry == null)
				? name : surrugateEntry+" ("+name+")";
	}

	
	@Override
	public void close() throws IOException {
	}

	@Override
	public void activate() {
	}

}