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

package de.kiwiwings.poi.visualizer.treemodel.hpsf;

import static de.kiwiwings.poi.visualizer.treemodel.TreeModelUtils.reflectProperties;

import java.io.IOException;
import java.io.OutputStream;

import javax.swing.tree.DefaultMutableTreeNode;

import org.apache.poi.hpsf.PropertySet;
import org.apache.poi.hpsf.WritingNotSupportedException;
import org.exbin.utils.binary_data.ByteArrayEditableData;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import de.kiwiwings.poi.visualizer.treemodel.TreeModelEntry;
import de.kiwiwings.poi.visualizer.treemodel.TreeObservable;
import de.kiwiwings.poi.visualizer.treemodel.TreeObservable.SourceType;

@Component(value="HPSFPropertySet")
@Scope("prototype")
public class HPSFPropertySet implements TreeModelEntry {

	private final PropertySet propertySet;
	@SuppressWarnings("unused")
	private final DefaultMutableTreeNode treeNode;
	final TreeModelEntry surrugateEntry;

	@Autowired
	TreeObservable treeObservable;

	
	public HPSFPropertySet(final PropertySet propertySet, final DefaultMutableTreeNode treeNode) {
		this.propertySet = propertySet;
		this.treeNode = treeNode;
		Object oldUserObject = treeNode.getUserObject();
		surrugateEntry = (oldUserObject instanceof TreeModelEntry) ? (TreeModelEntry)oldUserObject : null;
	}


	@Override
	public String toString() {
		return surrugateEntry.toString();
	}

	
	@Override
	public void close() throws IOException {
	}

	@Override
	public void activate() {
		treeObservable.setBinarySource(() -> getData());
		treeObservable.setSourceType(SourceType.octet);
		treeObservable.setFileName(toString()+".rec");
		treeObservable.setProperties(reflectProperties(propertySet));
	}

	private ByteArrayEditableData getData() throws IOException {
		final ByteArrayEditableData data = new ByteArrayEditableData();
		try (final OutputStream os = data.getDataOutputStream()) {
			propertySet.write(os);
		} catch (WritingNotSupportedException e) {
			throw new IOException(e);
		}
		return data;
	}
}
