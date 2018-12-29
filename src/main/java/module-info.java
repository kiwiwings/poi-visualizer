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

import de.kiwiwings.poi.visualizer.treemodel.TreeModelDirNodeSource;
import de.kiwiwings.poi.visualizer.treemodel.TreeModelFileSource;
import de.kiwiwings.poi.visualizer.treemodel.hpsf.HPSFTreeModel;
import de.kiwiwings.poi.visualizer.treemodel.hslf.HSLFTreeModel;
import de.kiwiwings.poi.visualizer.treemodel.hssf.HSSFTreeModel;
import de.kiwiwings.poi.visualizer.treemodel.ole.OLETreeModel;
import de.kiwiwings.poi.visualizer.treemodel.opc.OPCTreeModel;

module kiwiwings.poivisualizer {
//    requires java.xml.bind;
    requires javafx.base;
    requires javafx.graphics;
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.swing;
    requires exbin.binary.data;
    requires org.apache.commons.collections4;
    requires deltahex.core;
    requires deltahex.swing;
//    requires xmlbeans;
    requires java.json;
    requires poi;
    requires poi.ooxml;
//    requires poi.ooxml.schemas;
    requires poi.scratchpad;
    requires richtextfx;
    requires org.apache.commons.lang3;
    requires reactfx;
    exports de.kiwiwings.poi.visualizer;
    opens de.kiwiwings.poi.visualizer to javafx.fxml;
    uses TreeModelFileSource;
    uses TreeModelDirNodeSource;
    provides TreeModelFileSource with OPCTreeModel, OLETreeModel;
    provides TreeModelDirNodeSource with HPSFTreeModel, HSLFTreeModel, HSSFTreeModel;
}