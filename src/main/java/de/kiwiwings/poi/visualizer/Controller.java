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

package de.kiwiwings.poi.visualizer;

import de.kiwiwings.poi.visualizer.treemodel.TreeModelEntry;
import de.kiwiwings.poi.visualizer.treemodel.TreeModelFileSource;
import de.kiwiwings.poi.visualizer.treemodel.TreeModelLoadException;
import de.kiwiwings.poi.visualizer.treemodel.TreeObservable;
import javafx.embed.swing.SwingNode;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.text.Text;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import org.apache.poi.util.IOUtils;
import org.exbin.deltahex.swing.CodeArea;
import org.exbin.utils.binary_data.ByteArrayEditableData;

import javax.swing.*;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.Observable;
import java.util.ResourceBundle;
import java.util.ServiceLoader;

public class Controller implements Initializable {

    @FXML
    private TreeView<TreeModelEntry> treeDir;

    @FXML
    private SwingNode deltaHexSN;

    @FXML
    private org.fxmisc.richtext.CodeArea xmlArea;

    @SuppressWarnings({"FieldCanBeLocal", "unused"})
    private CodeFormatter xmlAreaFormatter;

    @FXML
    private org.fxmisc.richtext.CodeArea propertiesArea;

    @SuppressWarnings({"FieldCanBeLocal", "unused"})
    private CodeFormatter propertiesAreaFormatter;


    private Stage stage;

    private File workingDir;

    private final TreeObservable treeObservable = TreeObservable.getInstance();

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        addDeltaHex();
        treeObservable.addObserver(this::updateCodeArea);
        treeObservable.addObserver(this::updateProperties);
        treeObservable.addObserver(this::updateXml);

        // reference code formatter because of garbage collection
        xmlAreaFormatter = new CodeFormatter(xmlArea);
        propertiesAreaFormatter = new CodeFormatter(propertiesArea);
    }

    void setStage(Stage stage) {
        this.stage = stage;
    }

    @SuppressWarnings("unused")
    @FXML
    private void handleOpen(final ActionEvent event) {
        if (workingDir == null) {
            workingDir = new File(".");
        }

        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Open Office File");
        fileChooser.setInitialDirectory(workingDir);
        final File file = fileChooser.showOpenDialog(stage);
        if (file != null) {
            openFile(file);
        }
    }

    void openFile(File file) {
        workingDir = file.getParentFile();

        closeFile();

        TreeItem<TreeModelEntry> treeNode = new TreeItem<>();
        final ServiceLoader<TreeModelFileSource> sl = ServiceLoader.load(TreeModelFileSource.class);
        for (TreeModelFileSource src : sl) {
            try {
                src.load(treeNode, file);
                treeDir.setRoot(treeNode);
                stage.setTitle("POI Visualizer - <" + file.getName() + ">");
                return;
            } catch (TreeModelLoadException ex) {
                // TODO: log
            }
        }

        new Alert(AlertType.ERROR, "file is not an Office file.", ButtonType.OK).showAndWait();
    }

    @FXML
    void closeFile() {
        stage.setTitle("POI Visualizer - <no file>");
        TreeItem<TreeModelEntry> tr = treeDir.getRoot();
        if (tr != null && tr.getValue() != null) {
            IOUtils.closeQuietly(tr.getValue());
        }
        treeDir.setRoot(null);
        treeObservable.setProperties("");
    }


    @SuppressWarnings("unused")
    @FXML
    private void handleExit(final ActionEvent event) {
        stage.hide();
    }

    private void addDeltaHex() {
        final CodeArea ca = new CodeArea();
        ca.setData(new ByteArrayEditableData());
        SwingUtilities.invokeLater(() -> deltaHexSN.setContent(ca));
    }

    CodeArea getCodeArea() {
        return (CodeArea) deltaHexSN.getContent();
    }

    @FXML
    private void onClick(final MouseEvent e) {
        Node node = e.getPickResult().getIntersectedNode();
        if (node instanceof Text || (node instanceof TreeCell && ((TreeCell) node).getText() != null)) {
            TreeModelEntry tme = treeDir.getSelectionModel().getSelectedItem().getValue();
            if (tme != null) {
                tme.activate();
                treeObservable.notifyObservers();
            }
        }
    }

    @SuppressWarnings("unused")
    @FXML
    private void onKey(final KeyEvent e) {
        final TreeItem<TreeModelEntry> ti = treeDir.getSelectionModel().getSelectedItem();

        if (ti != null) {
            final TreeModelEntry tme = ti.getValue();
            if (tme != null) {
                tme.activate();
                treeObservable.notifyObservers();
            }
        }
    }

    private void updateCodeArea(final Observable o, final Object arg) {
        try {
            ByteArrayEditableData data = treeObservable.getBinarySource().getBinaryData();
            if (data != null) {
                getCodeArea().setData(data);
            }
        } catch (IOException | TreeModelLoadException ex) {
            // todo
        }
    }

    @SuppressWarnings("unused")
    private void updateProperties(final Observable o, final Object arg) {
        final String props = treeObservable.getProperties();
        propertiesArea.replaceText(CodeIndenter.indentJson(props));
    }

    @SuppressWarnings("unused")
    private void updateXml(final Observable o, final Object arg) {
        if (treeObservable.getSourceType() != TreeObservable.SourceType.text_xml) {
            xmlArea.clear();
        } else {
            // can't use getDataInputStream because of deltahex error in released artifact - fixed in trunk ...
            try {
                final byte[] xmlInput = treeObservable.getBinarySource().getBinaryData().getData();
                xmlArea.replaceText(CodeIndenter.indentXml(xmlInput));
            } catch (IOException|TreeModelLoadException e) {
                xmlArea.clear();
            }
        }
    }




}
