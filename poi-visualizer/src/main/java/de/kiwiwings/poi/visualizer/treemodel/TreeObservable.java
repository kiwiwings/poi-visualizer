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

import java.util.Observable;

import org.springframework.stereotype.Component;

import de.kiwiwings.poi.visualizer.BinarySource;
import de.kiwiwings.poi.visualizer.StructuredSource;

@Component(value="treeObserver")
public class TreeObservable extends Observable {
	private BinarySource binarySource;
	private String binaryFileName;
	private StructuredSource structuredSource;

	public BinarySource getBinarySource() {
		return binarySource;
	}

	public void setBinarySource(BinarySource binarySource) {
		this.binarySource = binarySource;
		setChanged();
	}

	public String getBinaryFileName() {
		return binaryFileName;
	}
	
	public void setBinaryFileName(final String fileName) {
		this.binaryFileName = fileName;
	}
	
	public StructuredSource getStructuredSource() {
		return structuredSource;
	}

	public void setStructuredSource(StructuredSource structuredSource) {
		this.structuredSource = structuredSource;
		setChanged();
	}

}
