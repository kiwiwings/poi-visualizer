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

package de.kiwiwings.poi.visualizer.treemodel.generic;

import de.kiwiwings.poi.visualizer.DocumentFragment;
import de.kiwiwings.poi.visualizer.treemodel.TreeModelEntry;
import javafx.scene.control.TreeItem;
import org.apache.poi.common.usermodel.GenericRecord;

import java.io.IOException;

public class GenericRootEntry implements TreeModelEntry {
    final GenericRecord root;
    final TreeItem<TreeModelEntry> treeNode;
    final TreeModelEntry surrugateEntry;

    @Override
    public void activate(DocumentFragment fragment) {
        fragment.setBinarySource(null);
        fragment.setSourceType(DocumentFragment.SourceType.empty);
    }

    public GenericRootEntry(GenericRecord root, TreeItem<TreeModelEntry> treeNode) {
        this.root = root;
        this.treeNode = treeNode;
        surrugateEntry = treeNode.getValue();
    }

    public GenericRecord getRoot() {
        return root;
    }

    @Override
    public String toString() {
        final String name = "SlideShow";
        return (treeNode.getParent() == null || surrugateEntry == null)
            ? name : surrugateEntry+" ("+name+")";
    }


    @Override
    public void close() throws IOException {

    }
}
