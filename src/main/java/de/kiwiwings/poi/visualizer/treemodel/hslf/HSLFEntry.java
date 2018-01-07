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

import static de.kiwiwings.poi.visualizer.treemodel.TreeModelUtils.escapeString;
import static de.kiwiwings.poi.visualizer.treemodel.TreeModelUtils.reflectProperties;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import javax.swing.tree.DefaultMutableTreeNode;

import org.apache.poi.hslf.record.Record;
import org.apache.poi.hslf.record.UnknownRecordPlaceholder;
import org.apache.poi.poifs.filesystem.FileMagic;
import org.apache.poi.util.BoundedInputStream;
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

@Component(value="HSLFEntry")
@Scope("prototype")
public class HSLFEntry implements TreeModelEntry {

	private final Record record;
	@SuppressWarnings("unused")
	private final DefaultMutableTreeNode treeNode;
	File opcFile;

	@Autowired
	TreeObservable treeObservable;

	@Autowired
	private ApplicationContext appContext;

	
	public HSLFEntry(final Record record, final DefaultMutableTreeNode treeNode) {
		this.record = record;
		this.treeNode = treeNode;
	}


	@Override
	public String toString() {
		return escapeString(record.getClass().getSimpleName());
	}

	
	@Override
	public void close() throws IOException {
	}

	@Override
	public void activate() {
		treeObservable.setBinarySource(() -> getData());
		treeObservable.setSourceType(SourceType.octet);
		treeObservable.setFileName(toString()+".rec");
		treeObservable.setProperties(reflectProperties(record));
	}

	private ByteArrayEditableData getData() throws IOException, TreeModelLoadException {
		final ByteArrayEditableData data = new ByteArrayEditableData();
		try (final OutputStream os = data.getDataOutputStream()) {
			record.writeOut(os);
		}

		if (record instanceof UnknownRecordPlaceholder && data.getDataSize() > 16) {
			final byte[] magic = new byte[8];
			// skip header
			data.copyToArray(8, magic, 0, 8);
			FileMagic fm = FileMagic.valueOf(magic);
			if (fm == FileMagic.OOXML) {
				if (opcFile == null) {
					try (InputStream is = new BoundedInputStream(data.getDataInputStream(),data.getDataSize())) {
						opcFile = copyToTempFile(is);
					}
					OPCTreeModel opcNode = appContext.getBean(OPCTreeModel.class, treeNode);
					opcNode.load(opcFile);
					((TreeModelEntry)treeNode.getUserObject()).activate();
				}
			}
		}

		return data;

		
	}

	private File copyToTempFile(InputStream is) throws IOException {
		final String prefix = record.getClass().getSimpleName() + "-";
		final String suffix = ".zip";

		final File of = TempFile.createTempFile(prefix, suffix);
		try (FileOutputStream fos = new FileOutputStream(of)) {
			IOUtils.copy(is, fos);
		}
		of.deleteOnExit();
		return of;
	}


}
