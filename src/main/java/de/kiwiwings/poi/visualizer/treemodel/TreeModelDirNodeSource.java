package de.kiwiwings.poi.visualizer.treemodel;

import javafx.scene.control.TreeItem;
import org.apache.poi.poifs.filesystem.DirectoryNode;

import java.io.File;

public interface TreeModelDirNodeSource {
    void load(TreeItem<TreeModelEntry> parent, DirectoryNode source) throws TreeModelLoadException;
}
