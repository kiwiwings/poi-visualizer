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

import de.kiwiwings.poi.visualizer.treemodel.TreeModelDirNodeSource;
import de.kiwiwings.poi.visualizer.treemodel.TreeModelEntry;
import de.kiwiwings.poi.visualizer.treemodel.TreeModelLoadException;
import javafx.scene.control.TreeItem;
import org.apache.poi.common.usermodel.GenericRecord;
import org.apache.poi.hslf.usermodel.HSLFSlideShow;
import org.apache.poi.poifs.filesystem.DirectoryNode;

import java.util.List;

import static de.kiwiwings.poi.visualizer.treemodel.TreeModelUtils.getNamedTreeNode;

public class GenericTreeModel implements TreeModelDirNodeSource {
    private TreeItem<TreeModelEntry> parent;
    private GenericRecord root;

    @Override
    public void load(TreeItem<TreeModelEntry> parent, DirectoryNode source) throws TreeModelLoadException {
        this.parent = parent;

        if (!source.hasEntry(HSLFSlideShow.POWERPOINT_DOCUMENT)) {
            throw new TreeModelLoadException("unsupported generic model");
        }

        try {
            GenericRecord root = new HSLFSlideShow(source);
            final TreeItem<TreeModelEntry> slNode = getNamedTreeNode(parent, HSLFSlideShow.POWERPOINT_DOCUMENT);
            GenericRootEntry rootNode = new GenericRootEntry(root, slNode);
            slNode.setValue(rootNode);
            loadRecords(slNode, root);
        } catch (Exception e) {
            throw new TreeModelLoadException("Can't load HSLF slideshow",e);
        }

    }

    static void loadRecords(final TreeItem<TreeModelEntry> parentNode, final GenericRecord parentRecord) {
        List<? extends GenericRecord> children = parentRecord.getGenericChildren();
        if (children == null) {
            return;
        }

        children.forEach(c -> {
            final TreeItem<TreeModelEntry> childNode = new TreeItem<>();
            childNode.setValue(new GenericRecordEntry(c, childNode));
            parentNode.getChildren().add(childNode);
            loadRecords(childNode, c);
        });
    }
}
