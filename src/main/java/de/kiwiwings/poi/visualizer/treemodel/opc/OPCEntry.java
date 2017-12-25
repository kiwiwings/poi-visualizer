/* ====================================================================
   Licensed to the Apache Software Foundation (ASF) under one or more
   contributor license agreements.  See the NOTICE file distributed with
   this work for additional information regarding copyright ownership.
   The ASF licenses this file to You under the Apache License, Version 2.0
   (the "License"); you may not use this file except in compliance with
   the License.  You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
==================================================================== */

package de.kiwiwings.poi.visualizer.treemodel.opc;

import static de.kiwiwings.poi.visualizer.treemodel.TreeModelUtils.escapeString;
import static de.kiwiwings.poi.visualizer.treemodel.TreeObservable.SourceOrigin.MENU_EDIT_APPLY;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.util.Observable;

import javax.swing.tree.DefaultMutableTreeNode;

import org.apache.poi.openxml4j.opc.PackagePart;
import org.apache.poi.openxml4j.opc.PackagePartName;
import org.apache.poi.openxml4j.opc.internal.PackagePropertiesPart;
import org.apache.poi.poifs.filesystem.FileMagic;
import org.apache.poi.util.IOUtils;
import org.apache.poi.util.LocaleUtil;
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
import de.kiwiwings.poi.visualizer.treemodel.ole.OLETreeModel;

@Component(value="OPCEntry")
@Scope("prototype")
public class OPCEntry implements TreeModelEntry {
	PackagePart packagePart;
	final DefaultMutableTreeNode treeNode;
	File oleFile;
	
	@Autowired
	TreeObservable treeObservable;

	@Autowired
	private ApplicationContext appContext;
	
	
	public OPCEntry(final PackagePart packagePart, final DefaultMutableTreeNode treeNode) {
		this.packagePart = packagePart;
		this.treeNode = treeNode;
	}
	
	@Override
	public String toString() {
		return escapeString(packagePart.getPartName().getName().replaceAll(".*/", ""));
	}
	
	@Override
	public void activate() {
		final String fileName = toString();
		treeObservable.setFileName(fileName);
		final SourceType sourceType;
		if (fileName.matches(".*(\\.xml|\\.rels)$")) {
			sourceType = SourceType.text_xml;
		} else {
			sourceType = SourceType.octet;
		}
		treeObservable.setSourceType(sourceType);
		treeObservable.setProperties(null);
		treeObservable.setTreeEntryListener(this);

		// need to cache data, otherwise the switch from opc to ole doesn't work
		// furthermore the data part needs to be set last, because of a side-effect in getData() 
		ByteArrayEditableData dataTmp;
		try {
			dataTmp = getData();
		} catch (TreeModelLoadException|IOException e) {
			dataTmp = new ByteArrayEditableData(e.getMessage().getBytes(Charset.forName("UTF-8")));
		}
		final ByteArrayEditableData data = dataTmp;
		
		treeObservable.setBinarySource(() -> data);
	}

	@Override
	public void update(Observable o, Object arg) {
		if (MENU_EDIT_APPLY.equals(arg)) {
			PackagePartName pn = packagePart.getPartName();
			packagePart.clear();
			try (OutputStream os = packagePart.getOutputStream()) {
				treeObservable.getBinarySource().getBinaryData().saveToStream(os);
				// packagePart reference might have internally changed, so we need to update ours too
				packagePart = packagePart.getPackage().getPart(pn);
			} catch (IOException | TreeModelLoadException e) {
				// TODO: error message
				e.printStackTrace();
			}
		}
	}

	
	private ByteArrayEditableData getData() throws IOException, TreeModelLoadException {
		if (packagePart instanceof PackagePropertiesPart) {
			return new ByteArrayEditableData("Property parts can't be exported.".getBytes(LocaleUtil.CHARSET_1252));
		}
		
		FileMagic fm;
		try (InputStream is = FileMagic.prepareToCheckMagic(packagePart.getInputStream())) {
			final ByteArrayEditableData data = new ByteArrayEditableData();
			fm = FileMagic.valueOf(is);
			if (fm == FileMagic.OLE2) {
				if (oleFile == null) {
					oleFile = copyToTempFile(is);
					OLETreeModel poifsNode = appContext.getBean(OLETreeModel.class, treeNode);
					poifsNode.load(oleFile);
					((TreeModelEntry)treeNode.getUserObject()).activate();
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
