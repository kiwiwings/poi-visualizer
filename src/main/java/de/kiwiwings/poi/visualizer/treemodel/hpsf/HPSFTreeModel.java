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

package de.kiwiwings.poi.visualizer.treemodel.hpsf;

import de.kiwiwings.poi.visualizer.treemodel.TreeModelDirNodeSource;
import de.kiwiwings.poi.visualizer.treemodel.TreeModelEntry;
import de.kiwiwings.poi.visualizer.treemodel.TreeModelLoadException;
import javafx.scene.control.TreeItem;
import org.apache.poi.hpsf.*;
import org.apache.poi.poifs.filesystem.DirectoryNode;

import java.io.IOException;

import static de.kiwiwings.poi.visualizer.treemodel.TreeModelUtils.getNamedTreeNode;

public class HPSFTreeModel implements TreeModelDirNodeSource {

	private TreeItem<TreeModelEntry> parent;

	@Override
	public void load(final TreeItem<TreeModelEntry> parent, final DirectoryNode source) throws TreeModelLoadException {
		if (!(source.hasEntry(DocumentSummaryInformation.DEFAULT_STREAM_NAME) ||
			source.hasEntry(SummaryInformation.DEFAULT_STREAM_NAME))) {
			throw new TreeModelLoadException("not a HPSF model");
		}

		this.parent = parent;
		addPropertySet(source, SummaryInformation.DEFAULT_STREAM_NAME);
		addPropertySet(source, DocumentSummaryInformation.DEFAULT_STREAM_NAME);
	}
	
	private void addPropertySet(final DirectoryNode dn, final String psName) throws TreeModelLoadException {
		if (!dn.hasEntry(psName)) {
			return;
		}

		final PropertySet ps;
		try {
			ps = PropertySetFactory.create(dn, psName);
		} catch (NoPropertySetStreamException | IOException e) {
			throw new TreeModelLoadException("Can't load property set", e);
		}
		final TreeItem<TreeModelEntry> slNode = getNamedTreeNode(parent, psName);
		final HPSFPropertySet psModel = new HPSFPropertySet(ps, slNode);
		slNode.setValue(psModel);
		
		for (final Section section : ps.getSections()) {
			addSection(section, slNode, ps);
		}
	}
	
	private void addSection(final Section section, final TreeItem<TreeModelEntry> parent, final PropertySet ps) {
		TreeItem<TreeModelEntry> secNode = new TreeItem<>();
		final HPSFSection secModel = new HPSFSection(section, secNode);
		secNode.setValue(secModel);
		parent.getChildren().add(secNode);
		
		for (final Property p : section.getProperties()) {
			addProperty(p, secNode, ps);
		}
	}

	private void addProperty(final Property property, final TreeItem<TreeModelEntry> parent, final PropertySet ps) {
		TreeItem<TreeModelEntry> propNode = new TreeItem<TreeModelEntry>();
		final HPSFProperty propModel = new HPSFProperty(property, propNode);
		propModel.setPropertySet(ps);
		propNode.setValue(propModel);
		parent.getChildren().add(propNode);
	}
}
