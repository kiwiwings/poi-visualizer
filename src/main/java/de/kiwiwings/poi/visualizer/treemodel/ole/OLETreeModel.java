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

import de.kiwiwings.poi.visualizer.treemodel.TreeModelDirNodeSource;
import de.kiwiwings.poi.visualizer.treemodel.TreeModelEntry;
import de.kiwiwings.poi.visualizer.treemodel.TreeModelFileSource;
import de.kiwiwings.poi.visualizer.treemodel.TreeModelLoadException;
import javafx.scene.control.TreeItem;
import org.apache.poi.poifs.filesystem.DirectoryNode;
import org.apache.poi.poifs.filesystem.Entry;
import org.apache.poi.poifs.filesystem.FileMagic;
import org.apache.poi.poifs.filesystem.POIFSFileSystem;

import java.io.File;
import java.io.IOException;
import java.util.ServiceLoader;
import java.util.function.BiFunction;

public class OLETreeModel implements TreeModelFileSource {

	private TreeItem<TreeModelEntry> parent;

	private POIFSFileSystem poifs;

	@Override
	public void load(TreeItem<TreeModelEntry> parent, File source) throws TreeModelLoadException {
		this.parent = parent;

		try {
			final FileMagic fm = FileMagic.valueOf(source);
			if (fm != FileMagic.OLE2) {
				throw new TreeModelLoadException("File with file magic '"+fm+"' can't be processed.");
			}

			poifs = new POIFSFileSystem(source);
			traverseFileSystem(poifs.getRoot(), parent);
			handleInnerModel(poifs, parent);
		} catch (IOException ex) {
			throw new TreeModelLoadException("Error in opening '"+((File)source).getPath()+"'");
		}
	}

	private void traverseFileSystem(final Entry poifsNode, final TreeItem<TreeModelEntry> parent) throws TreeModelLoadException {
		final BiFunction<Entry,TreeItem<TreeModelEntry>,TreeModelEntry> newTME;
		if (poifsNode.getParent() == null) {
			newTME = OLERootEntry::new;
		} else if (poifsNode instanceof DirectoryNode) {
			newTME = OLEDirEntry::new;
		} else {
			newTME = OLEEntry::new;
		}

		parent.setValue(newTME.apply(poifsNode, parent));

		if (poifsNode instanceof DirectoryNode) {
			for (Entry poifsChild : ((DirectoryNode)poifsNode)) {
				TreeItem<TreeModelEntry> treeChild = new TreeItem<>();
				parent.getChildren().add(treeChild);
				traverseFileSystem(poifsChild, treeChild);
			}
		}
	}

	private void handleInnerModel(final POIFSFileSystem poifs, final TreeItem<TreeModelEntry> treeNode) throws TreeModelLoadException {
		final DirectoryNode root = poifs.getRoot();
		final ServiceLoader<TreeModelDirNodeSource> sl = ServiceLoader.load(TreeModelDirNodeSource.class);
		for (TreeModelDirNodeSource src : sl) {
			try {
				src.load(treeNode, root);
			} catch (TreeModelLoadException ex) {
				// TODO: log
			}
		}
	}
}





