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

import static de.kiwiwings.poi.visualizer.treemodel.TreeModelUtils.reflectProperties;

import java.io.IOException;

import javax.swing.tree.DefaultMutableTreeNode;

import org.apache.poi.hslf.usermodel.HSLFPictureData;
import org.exbin.utils.binary_data.ByteArrayEditableData;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import de.kiwiwings.poi.visualizer.treemodel.TreeModelEntry;
import de.kiwiwings.poi.visualizer.treemodel.TreeObservable;
import de.kiwiwings.poi.visualizer.treemodel.TreeObservable.SourceType;

@Component(value="HSLFPictureEntry")
@Scope("prototype")
public class HSLFPictureEntry implements TreeModelEntry {

	private final HSLFPictureData picture;
	@SuppressWarnings("unused")
	private final DefaultMutableTreeNode treeNode;

	@Autowired
	TreeObservable treeObservable;

	
	public HSLFPictureEntry(final HSLFPictureData picture, final DefaultMutableTreeNode treeNode) {
		this.picture = picture;
		this.treeNode = treeNode;
	}


	@Override
	public String toString() {
		return "picture_"+picture.getIndex()+"."+picture.getType().extension;
	}

	
	@Override
	public void close() throws IOException {
	}

	@Override
	public void activate() {
		treeObservable.setBinarySource(() -> getData());
		treeObservable.setSourceType(SourceType.octet);
		treeObservable.setFileName(toString());
		treeObservable.setProperties(reflectProperties(picture));
	}

	private ByteArrayEditableData getData() throws IOException {
		return new ByteArrayEditableData(picture.getData());
	}
}
