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
import de.kiwiwings.poi.visualizer.DocumentFragment.SourceType;
import de.kiwiwings.poi.visualizer.treemodel.TreeModelEntry;
import de.kiwiwings.poi.visualizer.treemodel.TreeModelLoadException;
import de.kiwiwings.poi.visualizer.treemodel.opc.OPCTreeModel;
import javafx.scene.control.TreeItem;
import org.apache.poi.ddf.*;
import org.apache.poi.util.TempFile;
import org.exbin.utils.binary_data.ByteArrayEditableData;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import static de.kiwiwings.poi.visualizer.treemodel.TreeModelUtils.reflectProperties;

public class HSLFEscherRecord implements TreeModelEntry {

	private final EscherRecord escher;
	@SuppressWarnings("unused")
	private final TreeItem<TreeModelEntry> treeNode;

	private File opcFile;


	public HSLFEscherRecord(final EscherRecord escher, final TreeItem<TreeModelEntry> treeNode) {
		this.escher = escher;
		this.treeNode = treeNode;
	}


	@Override
	public String toString() {
		return escher.getClass().getSimpleName()+" ("+escher.getRecordSize()+" b)";
	}

	
	@Override
	public void close() throws IOException {
	}

	@Override
	public void activate(final DocumentFragment fragment) {
		fragment.setBinarySource(() -> getData(fragment));
		fragment.setSourceType(SourceType.octet);
		fragment.setFileName(toString());
		fragment.setProperties(reflectProperties(escher));
	}

	private ByteArrayEditableData getData(final DocumentFragment fragment) throws IOException, TreeModelLoadException {
		final byte[] data = escher.serialize();
		if (escher instanceof EscherTertiaryOptRecord && opcFile == null) {
			final EscherTertiaryOptRecord opt = (EscherTertiaryOptRecord)escher;
			for (final EscherProperty ep : opt.getEscherProperties()) {
				if (EscherProperties.GROUPSHAPE__METROBLOB == ep.getPropertyNumber()) {
					opcFile = copyToTempFile(((EscherComplexProperty)ep).getComplexData());
					OPCTreeModel poifsNode = new OPCTreeModel();
					poifsNode.load(treeNode, opcFile);
					treeNode.getValue().activate(fragment);
				}
			}
		}
		return new ByteArrayEditableData(data);
	}

	private File copyToTempFile(byte[] data) throws IOException {
		final String prefix = "metro-"+escher.getRecordId()+"-";
		final String suffix = ".dat";

		final File of = TempFile.createTempFile(prefix, suffix);
		try (FileOutputStream fos = new FileOutputStream(of)) {
			fos.write(data);
		}
		of.deleteOnExit();
		return of;
	}
}
