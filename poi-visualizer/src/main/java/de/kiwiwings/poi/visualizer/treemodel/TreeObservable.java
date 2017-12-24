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


package de.kiwiwings.poi.visualizer.treemodel;

import java.io.IOException;
import java.util.Observable;
import java.util.Observer;

import org.exbin.utils.binary_data.ByteArrayEditableData;
import org.springframework.stereotype.Component;

import de.kiwiwings.poi.visualizer.BinarySource;

@Component(value="treeObserver")
public class TreeObservable extends Observable {
	public enum SourceType {
		empty, octet, text_xml, text_plain, image_jpeg, image_png
	}

	public enum SourceOrigin {
		MENU_EDIT_APPLY;
	}
	
	private BinarySource binarySource;
	private ByteArrayEditableData cachedBinary;
	private String fileName;
	private String properties;
	private SourceType sourceType;

	public BinarySource getBinarySource() {
		return () -> getCachedBinary();
	}

	public void setBinarySource(BinarySource binarySource) {
		this.binarySource = binarySource;
		this.cachedBinary = null;
		setChanged();
	}

	public SourceType getSourceType() {
		return sourceType;
	}
	
	public void setSourceType(SourceType sourceType) {
		this.sourceType = sourceType;
		setChanged();
	}
	
	public String getFileName() {
		return fileName;
	}
	
	public void setFileName(final String fileName) {
		this.fileName = fileName;
		setChanged();
	}
	
	public String getProperties() {
		return properties;
	}

	public void setProperties(String properties) {
		this.properties = properties;
		setChanged();
	}

	public void setTreeEntryListener(TreeModelEntry entry) {
		deleteObserver(new Observer() {
			@Override
			public void update(Observable o, Object arg) {}
			@Override
			public boolean equals(Object o) {
				return o instanceof TreeModelEntry;
			}
		});
		
		if (entry != null) {
			addObserver(entry);
		}
	}
	
	private ByteArrayEditableData getCachedBinary() throws IOException, TreeModelLoadException {
		if (cachedBinary == null) {
			cachedBinary = binarySource.getBinaryData();
		}
		return cachedBinary;
	}
}
