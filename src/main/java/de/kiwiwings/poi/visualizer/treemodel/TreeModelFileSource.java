package de.kiwiwings.poi.visualizer.treemodel;

import javafx.scene.control.TreeItem;

import java.io.File;

public interface TreeModelFileSource {
    void load(TreeItem<TreeModelEntry> parent, File source) throws TreeModelLoadException;
}
