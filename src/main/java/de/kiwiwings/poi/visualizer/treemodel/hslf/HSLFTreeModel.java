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

import de.kiwiwings.poi.visualizer.treemodel.TreeModelDirNodeSource;
import de.kiwiwings.poi.visualizer.treemodel.TreeModelEntry;
import de.kiwiwings.poi.visualizer.treemodel.TreeModelLoadException;
import de.kiwiwings.poi.visualizer.treemodel.generic.GenericRecordEntry;
import de.kiwiwings.poi.visualizer.treemodel.generic.GenericRootEntry;
import javafx.collections.ObservableList;
import javafx.scene.control.TreeItem;
import org.apache.poi.ddf.EscherContainerRecord;
import org.apache.poi.ddf.EscherRecord;
import org.apache.poi.ddf.EscherTextboxRecord;
import org.apache.poi.hslf.model.textproperties.TextProp;
import org.apache.poi.hslf.model.textproperties.TextPropCollection;
import org.apache.poi.hslf.record.Record;
import org.apache.poi.hslf.record.*;
import org.apache.poi.hslf.usermodel.HSLFPictureData;
import org.apache.poi.hslf.usermodel.HSLFSlideShow;
import org.apache.poi.poifs.filesystem.DirectoryNode;

import java.util.Collections;
import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Predicate;

import static de.kiwiwings.poi.visualizer.treemodel.TreeModelUtils.getNamedTreeNode;

public class HSLFTreeModel implements TreeModelDirNodeSource {

	private TreeItem<TreeModelEntry> parent;

	private HSLFSlideShow ppt;

	@Override
	public void load(final TreeItem<TreeModelEntry> parent, final DirectoryNode source) throws TreeModelLoadException {
		this.parent = parent;

		if (!source.hasEntry(HSLFSlideShow.POWERPOINT_DOCUMENT)) {
			throw new TreeModelLoadException("not a HSLF model");
		}

		try {
			final TreeItem<TreeModelEntry> slNode = getNamedTreeNode(parent, HSLFSlideShow.POWERPOINT_DOCUMENT);
			if (slNode.getValue() instanceof GenericRootEntry) {
				GenericRootEntry ge = (GenericRootEntry)slNode.getValue();
				ppt = (HSLFSlideShow)ge.getRoot();
			} else {
				ppt = new HSLFSlideShow(source);
			}
			HSLFRootEntry rootNode = new HSLFRootEntry(ppt, slNode);
			slNode.setValue(rootNode);
			loadRecords(slNode,ppt.getSlideShowImpl().getRecords());
			final TreeItem<TreeModelEntry> picNode = getNamedTreeNode(parent, "Pictures");
			loadPictures(picNode);
			loadCurrentUser(parent);
		} catch (Exception e) {
			throw new TreeModelLoadException("Can't load HSLF slideshow",e);
		}
	}

	private void loadRecords(final TreeItem<TreeModelEntry> parentNode, final Record[] records) {
		int parentTextSize = 0;
		for (final Record r : records) {
			final BiFunction<Record,TreeItem<TreeModelEntry>,TreeModelEntry> newTME;
			if (r instanceof Slide) {
				newTME = HSLFSlideEntry::new;
			}else if (r instanceof RecordContainer) {
				newTME = HSLFDirEntry::new;
			} else if (r instanceof PPDrawing) {
				newTME = HSLFDrawing::new;
			} else {
				newTME = HSLFEntry::new;
			}

			ObservableList<TreeItem<TreeModelEntry>> children = parentNode.getChildren();
			final TreeItem<TreeModelEntry> oldItem = children.stream().filter(matchGenericEntry(r)).findFirst().orElse(null);
			final TreeItem<TreeModelEntry> childNode = (oldItem != null) ? oldItem : new TreeItem<>();
			childNode.setValue(newTME.apply(r, childNode));
			if (oldItem == null) {
				parentNode.getChildren().add(childNode);
			}

			// need to store text size, in case we need to parse a styletextproperties atom later
			if (r instanceof TextBytesAtom) {
				parentTextSize = ((TextBytesAtom)r).getText().length();
			} else if (r instanceof TextCharsAtom) {
				parentTextSize = ((TextCharsAtom)r).getText().length();
			}


			if (r instanceof RecordContainer) {
				loadRecords(childNode, r.getChildRecords());
			} else if (r instanceof PPDrawing) {
				loadEscherRecords(childNode, ((PPDrawing)r).getEscherRecords());
			} else if (r instanceof PPDrawingGroup) {
				loadEscherRecords(childNode, Collections.singletonList(((PPDrawingGroup)r).getDggContainer()));
			} else if (r instanceof ExOleObjStg) {
				loadOleEmbed(childNode, (ExOleObjStg)r);
			} else if (r instanceof TxMasterStyleAtom) {
				final TxMasterStyleAtom tmsa = (TxMasterStyleAtom)r;
				loadTextProp(childNode, "characterStyles", tmsa.getCharacterStyles());
				loadTextProp(childNode, "paragraphStyles", tmsa.getParagraphStyles());
			} else if (r instanceof StyleTextPropAtom) {
				final StyleTextPropAtom stpa = (StyleTextPropAtom)r;
				stpa.setParentTextSize(parentTextSize);
				loadTextProp(childNode, "characterStyles", stpa.getCharacterStyles());
				loadTextProp(childNode, "paragraphStyles", stpa.getParagraphStyles());
			}
		}
	}

	private static Predicate<TreeItem<TreeModelEntry>> matchGenericEntry(Record r) {
		return (item) -> {
			TreeModelEntry entry = item.getValue();
			return entry instanceof GenericRecordEntry &&
			   ((GenericRecordEntry)entry).getRecord() == r;
		};
	}

	private void loadTextProp(final TreeItem<TreeModelEntry> parentNode, String name, List<TextPropCollection> props) {
		final TreeItem<TreeModelEntry> childNode = new TreeItem<>();
		final HSLFNamedEntry childNE = new HSLFNamedEntry(name, childNode);
		childNode.setValue(childNE);
		parentNode.getChildren().add(childNode);
		int textBegin = 0;
		for (TextPropCollection tpc : props) {
			final int textEnd = textBegin+tpc.getCharactersCovered();
			final TreeItem<TreeModelEntry> textNode = new TreeItem<>();
			final HSLFNamedEntry textNE = new HSLFNamedEntry(textBegin+"-"+textEnd+" (i"+tpc.getIndentLevel()+")", textNode);
			textNode.setValue(textNE);
			childNode.getChildren().add(textNode);
			textBegin = textEnd;
			for (TextProp tp : tpc.getTextPropList()) {
				final TreeItem<TreeModelEntry> propNode = new TreeItem<>();
				final HSLFTextPropEntry propEntry = new HSLFTextPropEntry(tp, propNode);
				propNode.setValue(propEntry);
				textNode.getChildren().add(propNode);
			}
		}
	}

	private void loadOleEmbed(final TreeItem<TreeModelEntry> parentNode, ExOleObjStg record) {
		final TreeItem<TreeModelEntry> childNode = new TreeItem<>();
		final TreeModelEntry oleEntry = new HSLFOleEmbed(record, childNode);
		childNode.setValue(oleEntry);
		parentNode.getChildren().add(childNode);
	}

	private void loadPictures(final TreeItem<TreeModelEntry> parentNode) {
		for (HSLFPictureData p : ppt.getPictureData()) {
			final TreeItem<TreeModelEntry> childNode = new TreeItem<>();
			final HSLFPictureEntry pic = new HSLFPictureEntry(p, childNode);
			childNode.setValue(pic);
			parentNode.getChildren().add(childNode);
		}

	}

	private void loadEscherRecords(final TreeItem<TreeModelEntry> parentNode, List<EscherRecord> records) {
		for (EscherRecord r : records) {
			final TreeItem<TreeModelEntry> childNode = new TreeItem<>();
			final HSLFEscherRecord escher = new HSLFEscherRecord(r, childNode);
			childNode.setValue(escher);
			parentNode.getChildren().add(childNode);
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

	private void loadCurrentUser(final TreeItem<TreeModelEntry> parentNode) {
		final TreeItem<TreeModelEntry> cuNode = getNamedTreeNode(parentNode, "Current User");
		final CurrentUserAtom cu = ppt.getSlideShowImpl().getCurrentUserAtom();
		final HSLFCurrentUser cuModel = new HSLFCurrentUser(cu, cuNode);
		cuNode.setValue(cuModel);
	}
}
