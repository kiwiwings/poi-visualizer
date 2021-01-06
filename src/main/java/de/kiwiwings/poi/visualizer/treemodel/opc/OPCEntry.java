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

import de.kiwiwings.poi.visualizer.DocumentFragment;
import de.kiwiwings.poi.visualizer.DocumentFragment.SourceType;
import de.kiwiwings.poi.visualizer.treemodel.TreeModelEntry;
import de.kiwiwings.poi.visualizer.treemodel.TreeModelLoadException;
import de.kiwiwings.poi.visualizer.treemodel.ole.OLETreeModel;
import javafx.scene.control.TreeItem;
import org.apache.poi.EmptyFileException;
import org.apache.poi.openxml4j.opc.PackagePart;
import org.apache.poi.openxml4j.opc.internal.PackagePropertiesPart;
import org.apache.poi.poifs.filesystem.FileMagic;
import org.apache.poi.util.IOUtils;
import org.apache.poi.util.LocaleUtil;
import org.apache.poi.util.TempFile;
import org.exbin.utils.binary_data.ByteArrayEditableData;

import java.io.*;
import java.nio.charset.Charset;

import static de.kiwiwings.poi.visualizer.treemodel.TreeModelUtils.escapeString;

public class OPCEntry implements TreeModelEntry {
	private PackagePart packagePart;
	private final TreeItem<TreeModelEntry> treeNode;
	private final TreeModelEntry surrugateEntry;
	private File oleFile;

	public OPCEntry(final PackagePart packagePart, final TreeItem<TreeModelEntry> treeNode) {
		this.packagePart = packagePart;
		this.treeNode = treeNode;
		surrugateEntry = treeNode.getValue();
	}

	@Override
	public String toString() {
		final String name = escapeString(packagePart.getPartName().getName().replaceAll(".*/", ""));
		return (treeNode.getParent() == null || surrugateEntry == null)
				? name : surrugateEntry+" ("+name+")";
	}

	@Override
	public void activate(final DocumentFragment fragment) {
		final String fileName = toString();
		fragment.setFileName(fileName);
		final SourceType sourceType;
		if (fileName.matches(".*\\.(xml|rels|vml)$")) {
			sourceType = SourceType.text_xml;
		} else {
			sourceType = SourceType.octet;
		}
		fragment.setSourceType(sourceType);
		fragment.setProperties(null);

		// need to cache data, otherwise the switch from opc to ole doesn't work
		// furthermore the data part needs to be set last, because of a side-effect in getData()
		ByteArrayEditableData dataTmp;
		try {
			dataTmp = getData(fragment);
		} catch (TreeModelLoadException|IOException e) {
			dataTmp = new ByteArrayEditableData(e.getMessage().getBytes(Charset.forName("UTF-8")));
		}
		final ByteArrayEditableData data = dataTmp;

		fragment.setBinarySource(() -> data);
	}

//	@Override
//	public void update(Observable o, Object arg) {
//		if (MENU_EDIT_APPLY.equals(arg)) {
//			PackagePartName pn = packagePart.getPartName();
//			packagePart.clear();
//			try (OutputStream os = packagePart.getOutputStream()) {
//				fragment.getBinarySource().getBinaryData().saveToStream(os);
//				// packagePart reference might have internally changed, so we need to update ours too
//				packagePart = packagePart.getPackage().getPart(pn);
//			} catch (IOException | TreeModelLoadException e) {
//				// TODO: error message
//				e.printStackTrace();
//			}
//		}
//	}


	private ByteArrayEditableData getData(final DocumentFragment fragment) throws IOException, TreeModelLoadException {
		if (packagePart instanceof PackagePropertiesPart) {
			return new ByteArrayEditableData("Property parts can't be exported.".getBytes(LocaleUtil.CHARSET_1252));
		}

		FileMagic fm;
		try (InputStream is = FileMagic.prepareToCheckMagic(packagePart.getInputStream())) {
			final ByteArrayEditableData data = new ByteArrayEditableData();
			try {
				fm = FileMagic.valueOf(is);
			} catch (EmptyFileException e) {
				return data;
			}

			switch (fm) {
				case OLE2:
					if (oleFile == null) {
						oleFile = copyToTempFile(is);
						new OLETreeModel().load(treeNode, oleFile);
						treeNode.getValue().activate(fragment);
					}

					try (InputStream is2 = new FileInputStream(oleFile)) {
						data.loadFromStream(is2);
					}
					break;
				case OOXML:
					if (oleFile == null) {
						oleFile = copyToTempFile(is);
						new OPCTreeModel().load(treeNode, oleFile);
						treeNode.getValue().activate(fragment);
					}

					try (InputStream is2 = new FileInputStream(oleFile)) {
						data.loadFromStream(is2);
					}
					break;
				default:
					data.loadFromStream(is);
					break;
			}
			return data;
		}

	}

	private File copyToTempFile(InputStream is) throws IOException {
		String partName = packagePart.getPartName().getName();
		partName = partName.substring(partName.lastIndexOf('/')+1);
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
