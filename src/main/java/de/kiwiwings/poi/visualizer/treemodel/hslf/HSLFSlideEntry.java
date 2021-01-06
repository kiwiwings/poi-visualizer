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

import de.kiwiwings.poi.visualizer.treemodel.TreeModelEntry;
import javafx.scene.control.TreeItem;
import org.apache.poi.hslf.record.Record;
import org.apache.poi.hslf.record.Slide;

public class HSLFSlideEntry extends HSLFDirEntry {

	private final Slide slide;

	public HSLFSlideEntry(final Record slide, final TreeItem<TreeModelEntry> treeNode) {
		super(slide, treeNode);
		this.slide = (Slide)slide;
	}

	@Override
	public String toString() {
		return "Slide (id " + slide.getSheetId() + ")";
	}
}
