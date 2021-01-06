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
import org.apache.poi.hslf.record.Record;
import org.apache.poi.hslf.record.UnknownRecordPlaceholder;
import org.apache.poi.poifs.filesystem.FileMagic;
import org.apache.poi.util.BoundedInputStream;
import org.apache.poi.util.IOUtils;
import org.apache.poi.util.TempFile;
import org.exbin.utils.binary_data.ByteArrayEditableData;

import java.io.*;

import static de.kiwiwings.poi.visualizer.treemodel.TreeModelUtils.escapeString;
import static de.kiwiwings.poi.visualizer.treemodel.TreeModelUtils.reflectProperties;

public class HSLFEntry implements TreeModelEntry {

	private final Record record;
	@SuppressWarnings("unused")
	private final TreeItem<TreeModelEntry> treeNode;
	final TreeModelEntry surrugateEntry;
	File opcFile;

	public HSLFEntry(final Record record, final TreeItem<TreeModelEntry> treeNode) {
		this.record = record;
		this.treeNode = treeNode;
		this.surrugateEntry = treeNode.getValue();
	}


	@Override
	public String toString() {
		CountingOS cnt = new CountingOS();
		try {
			record.writeOut(cnt);
		} catch (IOException ignored) {
		}

		String name = (record instanceof UnknownRecordPlaceholder)
			? ((UnknownRecordPlaceholder) record).getRecordTypeEnum().name()
			: record.getClass().getSimpleName();
		return escapeString(name)+" ("+cnt.size()+" b)";
	}


	@Override
	public void close() throws IOException {
	}

	@Override
	public void activate(final DocumentFragment fragment) {
		fragment.setBinarySource(() -> getData(fragment));
		fragment.setSourceType(SourceType.octet);
		fragment.setFileName(toString()+".rec");
		fragment.setProperties(reflectProperties(record));
	}

	private ByteArrayEditableData getData(final DocumentFragment fragment) throws IOException, TreeModelLoadException {
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
					OPCTreeModel opcNode = new OPCTreeModel();
					opcNode.load(treeNode, opcFile);
					treeNode.getValue().activate(fragment);
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

	static class CountingOS extends OutputStream {
		private int count;

		@Override
		public void write(int b) {
			count++;
		}

		@Override
		public void write(byte[] b) {
			count += b.length;
		}

		@Override
		public void write(byte[] b, int off, int len) {
			count += len;
		}

		public int size() {
			return count;
		}
	}
}