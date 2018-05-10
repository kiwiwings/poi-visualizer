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

import de.kiwiwings.poi.visualizer.DocumentFragment;
import de.kiwiwings.poi.visualizer.treemodel.TreeModelEntry;
import de.kiwiwings.poi.visualizer.treemodel.TreeModelLoadException;
import de.kiwiwings.poi.visualizer.DocumentFragment.SourceType;
import de.kiwiwings.poi.visualizer.treemodel.ole.OLETreeModel;
import javafx.scene.control.TreeItem;
import org.apache.poi.hslf.record.ExOleObjStg;
import org.apache.poi.poifs.filesystem.FileMagic;
import org.apache.poi.util.IOUtils;
import org.apache.poi.util.TempFile;
import org.exbin.utils.binary_data.ByteArrayEditableData;

import java.io.*;

public class HSLFOleEmbed implements TreeModelEntry {

	private final ExOleObjStg embed;
	@SuppressWarnings("unused")
	private final TreeItem<TreeModelEntry> treeNode;

	private File oleFile;

	
	HSLFOleEmbed(final ExOleObjStg embed, final TreeItem<TreeModelEntry> treeNode) {
		this.embed = embed;
		this.treeNode = treeNode;
	}

	

	@Override
	public String toString() {
		return "data";
	}

	
	@Override
	public void close() throws IOException {
	}

	@Override
	public void activate(final DocumentFragment fragment) {
		fragment.setBinarySource(() -> getData(fragment));
		fragment.setSourceType(SourceType.octet);
		fragment.setFileName(toString()+".rec");
		fragment.setProperties(null);
	}

	private ByteArrayEditableData getData(final DocumentFragment fragment) throws IOException, TreeModelLoadException {
		FileMagic fm;
		try (InputStream is = FileMagic.prepareToCheckMagic(embed.getData())) {
			final ByteArrayEditableData data = new ByteArrayEditableData();
			fm = FileMagic.valueOf(is);
			if (fm == FileMagic.OLE2) {
				if (oleFile == null) {
					oleFile = copyToTempFile(is);
					OLETreeModel poifsNode = new OLETreeModel();
					poifsNode.load(treeNode, oleFile);
					treeNode.getValue().activate(fragment);
				}
				
				try (InputStream is2 = new FileInputStream(oleFile)) {
					data.loadFromStream(is2);
				}
			} else {
				data.loadFromStream(is);
			}
			return data;
		}
	}

	private File copyToTempFile(InputStream is) throws IOException {
		final String prefix = "embed-"+embed.getPersistId()+"-";
		final String suffix = ".dat";

		final File of = TempFile.createTempFile(prefix, suffix);
		try (FileOutputStream fos = new FileOutputStream(of)) {
			IOUtils.copy(is, fos);
		}
		of.deleteOnExit();
		return of;
	}

	
}
