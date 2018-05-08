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

package de.kiwiwings.poi.visualizer.treemodel.opc;

import de.kiwiwings.poi.visualizer.treemodel.TreeModelEntry;
import de.kiwiwings.poi.visualizer.treemodel.TreeModelFileSource;
import de.kiwiwings.poi.visualizer.treemodel.TreeModelLoadException;
import javafx.scene.control.TreeItem;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.openxml4j.opc.OPCPackage;
import org.apache.poi.openxml4j.opc.PackageAccess;
import org.apache.poi.openxml4j.opc.PackagePart;
import org.apache.poi.poifs.filesystem.FileMagic;
import org.apache.poi.util.IOUtils;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class OPCTreeModel implements TreeModelFileSource {
	
	TreeItem<TreeModelEntry> parent;

	@Override
	public void load(final TreeItem<TreeModelEntry> parent, final File source) throws TreeModelLoadException {
		this.parent = parent;

		OPCPackage opc = null;
		try {
			final FileMagic fm = FileMagic.valueOf(source);
			if (fm != FileMagic.OOXML) {
				throw new TreeModelLoadException("File with file magic '"+fm+"' can't be processed.");
			}

			opc = OPCPackage.open(source, PackageAccess.READ_WRITE);
			OPCRootEntry opcRoot = new OPCRootEntry(opc, parent);
			parent.setValue(opcRoot);

			final Map<String,TreeItem<TreeModelEntry>> mapFolders = new HashMap<>();
			final Map<String,List<TreeItem<TreeModelEntry>>> mapFiles = new HashMap<>();
			mapFolders.put("/", parent);

			// first create the folders, so we don't have folders and files mixed in the tree
			for (final PackagePart pp : opc.getParts()) {
				final String uri = pp.getPartName().toString();
				TreeItem<TreeModelEntry> parDir = parent;
				for (int idx=1;(idx=uri.indexOf('/',idx)) != -1;idx++) {
					final String path = uri.substring(0,idx);
					final TreeItem<TreeModelEntry> dir;
					if (mapFolders.containsKey(path)) {
						dir = mapFolders.get(path);
					} else {
						dir = new TreeItem<>();
						final OPCDirEntry entry = new OPCDirEntry(path, dir);
						dir.setValue(entry);
						mapFolders.put(path, dir);
						parDir.getChildren().add(dir);
					}
					parDir = dir;
				}

				// temporarily store the entries
				final String parPath = ((OPCDirEntry)parDir.getValue()).getPath();
				final List<TreeItem<TreeModelEntry>> listFiles;
				if (mapFiles.containsKey(parPath)) {
					listFiles = mapFiles.get(parPath);
				} else {
					listFiles = new ArrayList<>();
					mapFiles.put(parPath, listFiles);
				}
				final TreeItem<TreeModelEntry> node = new TreeItem<>();
				node.setValue(new OPCEntry(pp, node));
				listFiles.add(node);
			}


			// then add the items
			mapFiles.entrySet().stream().forEach(me -> {
				final TreeItem<TreeModelEntry> parDir = mapFolders.get(me.getKey());
				me.getValue().forEach(n -> parDir.getChildren().add(n));
			});

			// and at last map the content type
			final TreeItem<TreeModelEntry> node = new TreeItem<>();
			final OPCContentType entry = new OPCContentType(source, node);
			node.setValue(entry);
			parent.getChildren().add(node);
		} catch (InvalidFormatException|IOException ex) {
			IOUtils.closeQuietly(opc);
			throw new TreeModelLoadException("Error in opening '"+((File)source).getPath()+"'");
		}

	
	}
}
