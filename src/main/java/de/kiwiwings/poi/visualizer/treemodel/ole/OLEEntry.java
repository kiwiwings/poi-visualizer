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

package de.kiwiwings.poi.visualizer.treemodel.ole;

import static de.kiwiwings.poi.visualizer.treemodel.TreeModelUtils.escapeString;
import static de.kiwiwings.poi.visualizer.treemodel.TreeObservable.SourceOrigin.MENU_EDIT_APPLY;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Observable;

import javax.swing.tree.DefaultMutableTreeNode;

import org.apache.poi.poifs.filesystem.DirectoryNode;
import org.apache.poi.poifs.filesystem.DocumentNode;
import org.apache.poi.poifs.filesystem.Entry;
import org.apache.poi.poifs.filesystem.FileMagic;
import org.apache.poi.util.IOUtils;
import org.apache.poi.util.TempFile;
import org.exbin.utils.binary_data.ByteArrayEditableData;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import de.kiwiwings.poi.visualizer.treemodel.TreeModelEntry;
import de.kiwiwings.poi.visualizer.treemodel.TreeModelLoadException;
import de.kiwiwings.poi.visualizer.treemodel.TreeObservable;
import de.kiwiwings.poi.visualizer.treemodel.TreeObservable.SourceType;
import de.kiwiwings.poi.visualizer.treemodel.opc.OPCTreeModel;

@Component(value="OLEEntry")
@Scope("prototype")
public class OLEEntry implements TreeModelEntry {
	Entry entry;
	final DefaultMutableTreeNode treeNode;
	final TreeModelEntry surrugateEntry;
	File opcFile;

	@Autowired
	TreeObservable treeObservable;

	@Autowired
	private ApplicationContext appContext;

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
		treeObservable.setSourceType(SourceType.octet);
		treeObservable.setFileName(escapeString(entry.getName()));
		setProperties();
	}

	protected void setProperties() {
		treeObservable.setProperties(null);
	}

	@Override
	public void update(Observable o, Object arg) {
		if (surrugateEntry != null) {
			surrugateEntry.update(o, arg);
			return;
		}

		if (MENU_EDIT_APPLY.equals(arg)) {
			if (entry instanceof DocumentNode) {
				try (InputStream is = treeObservable.getBinarySource().getBinaryData().getDataInputStream()) {
					entry = entry.getParent().createDocument(entry.getName(), is);
				} catch (TreeModelLoadException|IOException e) {
					// TODO: error message
					e.printStackTrace();
				}
			}
		}
	}

	private ByteArrayEditableData getData() throws IOException, TreeModelLoadException {
		final DocumentNode dn = (DocumentNode)entry;
		final DirectoryNode parent = (DirectoryNode)dn.getParent();

		FileMagic fm;
		try (final InputStream is = FileMagic.prepareToCheckMagic(parent.createDocumentInputStream(dn))) {
			final ByteArrayEditableData data = new ByteArrayEditableData();
			fm = FileMagic.valueOf(is);
			if (fm == FileMagic.OOXML) {
				if (opcFile == null) {
					opcFile = copyToTempFile(is);
					OPCTreeModel opcNode = appContext.getBean(OPCTreeModel.class, treeNode);
					opcNode.load(opcFile);
					((TreeModelEntry)treeNode.getUserObject()).activate();
				}

				try (InputStream is2 = new FileInputStream(opcFile)) {
					data.loadFromStream(is2);
				}
			} else {
				data.loadFromStream(is);
			}
			return data;
		}
	}

	private File copyToTempFile(InputStream is) throws IOException {
		final String partName = entry.getName();
		final int idx = partName.lastIndexOf('.');
		final String prefix = ((idx == -1) ? partName : partName.substring(0, idx)) + "-";
		final String suffix = (idx == -1 || idx == partName.length()-1) ? "" : partName.substring(idx);

		final File of = TempFile.createTempFile(prefix, suffix);
		try (FileOutputStream fos = new FileOutputStream(of)) {
			IOUtils.copy(is, fos);
		}
		of.deleteOnExit();
		return of;
	}
	
	@Override
	public void close() throws IOException {

	}
}