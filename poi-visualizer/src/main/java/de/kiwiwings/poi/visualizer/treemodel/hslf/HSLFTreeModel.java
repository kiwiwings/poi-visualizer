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
import java.util.List;

import javax.swing.tree.DefaultMutableTreeNode;

import org.apache.poi.ddf.EscherContainerRecord;
import org.apache.poi.ddf.EscherRecord;
import org.apache.poi.hslf.record.PPDrawing;
import org.apache.poi.hslf.record.Record;
import org.apache.poi.hslf.record.RecordContainer;
import org.apache.poi.hslf.usermodel.HSLFPictureData;
import org.apache.poi.hslf.usermodel.HSLFSlideShow;
import org.apache.poi.poifs.filesystem.DirectoryNode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import de.kiwiwings.poi.visualizer.treemodel.TreeModelEntry;
import de.kiwiwings.poi.visualizer.treemodel.TreeModelLoadException;
import de.kiwiwings.poi.visualizer.treemodel.TreeModelSource;

@Component
@Scope("prototype")
public class HSLFTreeModel implements TreeModelSource {

	private final DefaultMutableTreeNode parent;

	private HSLFSlideShow ppt;

	@Autowired
	private ApplicationContext appContext;

	public HSLFTreeModel(final DefaultMutableTreeNode parent) {
		this.parent = parent;
	}

	@Override
	public void load(Object source) throws TreeModelLoadException {
		try {
			ppt = new HSLFSlideShow((DirectoryNode)source);
			final DefaultMutableTreeNode slNode = getNamedTreeNode(HSLFSlideShow.POWERPOINT_DOCUMENT);
			HSLFRootEntry rootNode = appContext.getBean(HSLFRootEntry.class, ppt, slNode);
			slNode.setUserObject(rootNode);
			loadRecords(slNode,ppt.getSlideShowImpl().getRecords());
			final DefaultMutableTreeNode picNode = getNamedTreeNode("Pictures");
			loadPictures(picNode);
		} catch (IOException e) {
			throw new TreeModelLoadException("Can't load HSLF slideshow",e);
		}
	}

	private void loadRecords(final DefaultMutableTreeNode parentNode, final Record[] records) {
		for (final Record r : records) {
			final String qualifier;
			if (r instanceof RecordContainer) {
				qualifier = "HSLFDirEntry";
			} else if (r instanceof PPDrawing) {
				qualifier = "HSLFDrawing";
			} else {
				qualifier = "HSLFEntry";
			}

			final DefaultMutableTreeNode childNode = new DefaultMutableTreeNode();
			final TreeModelEntry dirEntry = (TreeModelEntry)appContext.getBean(qualifier, r, childNode);
			childNode.setUserObject(dirEntry);
			parentNode.add(childNode);

			if (r instanceof RecordContainer) {
				loadRecords(childNode, ((RecordContainer)r).getChildRecords());
			} else if (r instanceof PPDrawing) {
				loadEscherRecords(childNode, ((PPDrawing)r).getEscherRecords());
			}
		}
	}

	private void loadPictures(final DefaultMutableTreeNode parentNode) {
		for (HSLFPictureData p : ppt.getPictureData()) {
			final DefaultMutableTreeNode childNode = new DefaultMutableTreeNode();
			final HSLFPictureEntry pic = appContext.getBean(HSLFPictureEntry.class, p, childNode);
			childNode.setUserObject(pic);
			parentNode.add(childNode);
		}

	}

	private void loadEscherRecords(final DefaultMutableTreeNode parentNode, List<EscherRecord> records) {
		for (EscherRecord r : records) {
			final DefaultMutableTreeNode childNode = new DefaultMutableTreeNode();
			final HSLFEscherRecord escher = appContext.getBean(HSLFEscherRecord.class, r, childNode);
			childNode.setUserObject(escher);
			parentNode.add(childNode);
			if (r instanceof EscherContainerRecord) {
				loadEscherRecords(childNode, ((EscherContainerRecord)r).getChildRecords());
			}
		}
	}

	private DefaultMutableTreeNode getNamedTreeNode(String name) {
		final int cnt = parent.getChildCount();
		for (int i=0; i<cnt; i++) {
			final DefaultMutableTreeNode c = (DefaultMutableTreeNode)parent.getChildAt(i);
			final TreeModelEntry poifsEntry = (TreeModelEntry)c.getUserObject();
			if (name.equals(poifsEntry.toString())) {
				return c;
			}
		}
		return null;
	}
}
