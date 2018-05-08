package de.kiwiwings.poi.visualizer.treemodel;

import javafx.scene.control.TreeItem;

public interface TreeModelSource {
    void load(TreeItem<TreeModelEntry> parent, Object source) throws TreeModelLoadException;
}
