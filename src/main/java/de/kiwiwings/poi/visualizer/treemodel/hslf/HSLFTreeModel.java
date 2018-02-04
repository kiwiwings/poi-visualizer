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

import static de.kiwiwings.poi.visualizer.treemodel.TreeModelUtils.getNamedTreeNode;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import javax.swing.tree.DefaultMutableTreeNode;

import org.apache.poi.ddf.EscherContainerRecord;
import org.apache.poi.ddf.EscherRecord;
import org.apache.poi.ddf.EscherTextboxRecord;
import org.apache.poi.hslf.model.textproperties.TextProp;
import org.apache.poi.hslf.model.textproperties.TextPropCollection;
import org.apache.poi.hslf.record.CurrentUserAtom;
import org.apache.poi.hslf.record.EscherTextboxWrapper;
import org.apache.poi.hslf.record.ExOleObjStg;
import org.apache.poi.hslf.record.HSLFEscherClientDataRecord;
import org.apache.poi.hslf.record.PPDrawing;
import org.apache.poi.hslf.record.PPDrawingGroup;
import org.apache.poi.hslf.record.Record;
import org.apache.poi.hslf.record.RecordContainer;
import org.apache.poi.hslf.record.TxMasterStyleAtom;
import org.apache.poi.hslf.usermodel.HSLFPictureData;
import org.apache.poi.hslf.usermodel.HSLFSlideShow;
import org.apache.poi.hslf.usermodel.HSLFTextBox;
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
			final DefaultMutableTreeNode slNode = getNamedTreeNode(parent, HSLFSlideShow.POWERPOINT_DOCUMENT);
			HSLFRootEntry rootNode = appContext.getBean(HSLFRootEntry.class, ppt, slNode);
			slNode.setUserObject(rootNode);
			loadRecords(slNode,ppt.getSlideShowImpl().getRecords());
			final DefaultMutableTreeNode picNode = getNamedTreeNode(parent, "Pictures");
			loadPictures(picNode);
			loadCurrentUser(parent);
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
			} else if (r instanceof PPDrawingGroup) {
				loadEscherRecords(childNode, Collections.singletonList(((PPDrawingGroup)r).getDggContainer()));
			} else if (r instanceof ExOleObjStg) {
				loadOleEmbed(childNode, (ExOleObjStg)r);
			} else if (r instanceof TxMasterStyleAtom) {
				loadTextProp(childNode, "characterStyles", ((TxMasterStyleAtom)r).getCharacterStyles());
				loadTextProp(childNode, "paragraphStyles", ((TxMasterStyleAtom)r).getParagraphStyles());
			}
		}
	}

	private void loadTextProp(final DefaultMutableTreeNode parentNode, String name, List<TextPropCollection> props) {
		final DefaultMutableTreeNode childNode = new DefaultMutableTreeNode();
		final HSLFNamedEntry childNE = appContext.getBean(HSLFNamedEntry.class, name, childNode);
		childNode.setUserObject(childNE);
		parentNode.add(childNode);
		int textBegin = 0;
		for (TextPropCollection tpc : props) {
			final int textEnd = textBegin+tpc.getCharactersCovered();
			final DefaultMutableTreeNode textNode = new DefaultMutableTreeNode();
			final HSLFNamedEntry textNE = appContext.getBean(HSLFNamedEntry.class, textBegin+"-"+textEnd+" (i"+tpc.getIndentLevel()+")", textNode);
			textNode.setUserObject(textNE);
			childNode.add(textNode);
			textBegin = textEnd;
			for (TextProp tp : tpc.getTextPropList()) {
				final DefaultMutableTreeNode propNode = new DefaultMutableTreeNode();
				final HSLFTextPropEntry propEntry = appContext.getBean(HSLFTextPropEntry.class, tp, propNode);
				propNode.setUserObject(propEntry);
				textNode.add(propNode);
			}
		}
	}
	
	private void loadOleEmbed(final DefaultMutableTreeNode parentNode, ExOleObjStg record) {
		final DefaultMutableTreeNode childNode = new DefaultMutableTreeNode();
		final TreeModelEntry oleEntry = appContext.getBean(HSLFOleEmbed.class, record, childNode);
		childNode.setUserObject(oleEntry);
		parentNode.add(childNode);
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
			} else if (r instanceof HSLFEscherClientDataRecord) {
				final List<? extends Record> hslfRecords = ((HSLFEscherClientDataRecord)r).getHSLFChildRecords();
				loadRecords(childNode, hslfRecords.toArray(new Record[hslfRecords.size()]));
			} else if (r instanceof EscherTextboxRecord) {
				final EscherTextboxWrapper wrapper = new EscherTextboxWrapper((EscherTextboxRecord)r);
				loadRecords(childNode, wrapper.getChildRecords());
			}
		}
	}
	
	private void loadCurrentUser(final DefaultMutableTreeNode parentNode) {
		final DefaultMutableTreeNode cuNode = getNamedTreeNode(parentNode, "Current User");
		final CurrentUserAtom cu = ppt.getSlideShowImpl().getCurrentUserAtom();
		final HSLFCurrentUser cuModel = appContext.getBean(HSLFCurrentUser.class, cu, cuNode);
		cuNode.setUserObject(cuModel);
	}
}
