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

import static de.kiwiwings.poi.visualizer.treemodel.TreeModelUtils.getNamedTreeNode;

import java.io.IOException;

import javax.swing.tree.DefaultMutableTreeNode;

import org.apache.poi.hpsf.DocumentSummaryInformation;
import org.apache.poi.hpsf.NoPropertySetStreamException;
import org.apache.poi.hpsf.Property;
import org.apache.poi.hpsf.PropertySet;
import org.apache.poi.hpsf.PropertySetFactory;
import org.apache.poi.hpsf.Section;
import org.apache.poi.hpsf.SummaryInformation;
import org.apache.poi.poifs.filesystem.DirectoryNode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import de.kiwiwings.poi.visualizer.treemodel.TreeModelLoadException;
import de.kiwiwings.poi.visualizer.treemodel.TreeModelSource;

@Component
@Scope("prototype")
public class HPSFTreeModel implements TreeModelSource {

	private final DefaultMutableTreeNode parent;

	@Autowired
	private ApplicationContext appContext;

	public HPSFTreeModel(final DefaultMutableTreeNode parent) {
		this.parent = parent;
	}

	@Override
	public void load(Object source) throws TreeModelLoadException {
		DirectoryNode dn = (DirectoryNode)source;
		addPropertySet(dn, SummaryInformation.DEFAULT_STREAM_NAME);
		addPropertySet(dn, DocumentSummaryInformation.DEFAULT_STREAM_NAME);
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
		final DefaultMutableTreeNode slNode = getNamedTreeNode(parent, psName);
		final HPSFPropertySet psModel = appContext.getBean(HPSFPropertySet.class, ps, slNode);
		slNode.setUserObject(psModel);
		
		for (final Section section : ps.getSections()) {
			addSection(section, slNode, ps);
		}
	}
	
	private void addSection(final Section section, final DefaultMutableTreeNode parent, final PropertySet ps) {
		DefaultMutableTreeNode secNode = new DefaultMutableTreeNode();
		final HPSFSection secModel = appContext.getBean(HPSFSection.class, section, secNode);
		secNode.setUserObject(secModel);
		parent.add(secNode);
		
		for (final Property p : section.getProperties()) {
			addProperty(p, secNode, ps);
		}
	}

	private void addProperty(final Property property, final DefaultMutableTreeNode parent, final PropertySet ps) {
		DefaultMutableTreeNode propNode = new DefaultMutableTreeNode();
		final HPSFProperty propModel = appContext.getBean(HPSFProperty.class, property, propNode);
		propModel.setPropertySet(ps);
		propNode.setUserObject(propModel);
		parent.add(propNode);
	}
}
