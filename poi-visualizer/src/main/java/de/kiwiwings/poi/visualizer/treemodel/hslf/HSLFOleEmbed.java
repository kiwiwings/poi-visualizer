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

package de.kiwiwings.poi.visualizer.treemodel.hslf;

import static de.kiwiwings.poi.visualizer.treemodel.TreeModelUtils.reflectProperties;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import javax.swing.tree.DefaultMutableTreeNode;

import org.apache.poi.hslf.record.ExOleObjStg;
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
import de.kiwiwings.poi.visualizer.treemodel.ole.OLETreeModel;

@Component(value="HSLFOleEmbed")
@Scope("prototype")
public class HSLFOleEmbed implements TreeModelEntry {

	private final ExOleObjStg embed;
	@SuppressWarnings("unused")
	private final DefaultMutableTreeNode treeNode;

	@Autowired
	TreeObservable treeObservable;

	@Autowired
	private ApplicationContext appContext;

	File oleFile;

	
	public HSLFOleEmbed(final ExOleObjStg embed, final DefaultMutableTreeNode treeNode) {
		this.embed = embed;
		this.treeNode = treeNode;
	}

	

	@Override
	public String toString() {
		return embed.getClass().getSimpleName() + " (embed-"+embed.getPersistId()+")";
	}

	
	@Override
	public void close() throws IOException {
	}

	@Override
	public void activate() {
		treeObservable.setBinarySource(() -> getData());
		treeObservable.setSourceType(SourceType.octet);
		treeObservable.setFileName(toString()+".rec");
		treeObservable.setProperties(reflectProperties(embed));
	}

	private ByteArrayEditableData getData() throws IOException, TreeModelLoadException {
		FileMagic fm;
		try (InputStream is = FileMagic.prepareToCheckMagic(embed.getData())) {
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
