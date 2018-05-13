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
import java.beans.PropertyChangeEvent;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.net.URL;
import java.util.ResourceBundle;
import java.util.ServiceLoader;
import java.util.function.Consumer;

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

    private File workingDir = new File(".");

    private final DocumentFragment fragment = new DocumentFragment();


    @FunctionalInterface
    private interface ConsumerEx<T> {
        void accept(T t) throws IOException;
    }


    @Override
    public void initialize(URL location, ResourceBundle resources) {
        addDeltaHex();

        fragment.addPropertyChangeListener(this::updateCodeArea);
        fragment.addPropertyChangeListener(this::updateProperties);
        fragment.addPropertyChangeListener(this::updateXml);

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
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Open Office File");
        fileChooser.setInitialDirectory(workingDir);
        final File file = fileChooser.showOpenDialog(stage);
        if (file != null) {
            workingDir = file.getParentFile();
            openFile(file);
        }
    }

    void openFile(File file) {
        closeFile();
        workingDir = file.getParentFile();

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
        fragment.setProperties("");
    }


    @SuppressWarnings("unused")
    @FXML
    private void handleExit(final ActionEvent event) {
        stage.hide();
    }

    @SuppressWarnings("unused")
    @FXML
    private void exportBinary(final ActionEvent event) {
        exportFile("Save Binary Data", (file) -> {
            try (FileOutputStream fos = new FileOutputStream(file)) {
                getCodeArea().getData().saveToStream(fos);
            }
        });
    }

    @SuppressWarnings("unused")
    @FXML
    private void exportXML(final ActionEvent event) {
        exportFile("Save XML Data", (file) -> {
            try (OutputStreamWriter osw = new OutputStreamWriter(new FileOutputStream(file), "UTF-8")) {
                osw.write(xmlArea.getText());
            }
        });
    }

    @SuppressWarnings("unused")
    @FXML
    private void exportProperties(final ActionEvent event) {
        exportFile("Save Properties", (file) -> {
            try (OutputStreamWriter osw = new OutputStreamWriter(new FileOutputStream(file), "UTF-8")) {
                osw.write(fragment.getProperties());
            }
        });
    }

    private void exportFile(final String title, final ConsumerEx<File> fileSaver) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle(title);
        fileChooser.setInitialDirectory(workingDir);
        fileChooser.setInitialFileName(fragment.getFileName());

        final File file = fileChooser.showSaveDialog(stage);
        if (file != null) {
            workingDir = file.getParentFile();

            try  {
                fileSaver.accept(file);
            } catch (IOException e) {
                new Alert(AlertType.ERROR, "error writing file", ButtonType.OK).showAndWait();
            }
        }
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
            final MultipleSelectionModel<TreeItem<TreeModelEntry>> sm = treeDir.getSelectionModel();
            final TreeModelEntry tme = sm.isEmpty() ? null : sm.getSelectedItem().getValue();
            if (tme != null) {
                tme.activate(fragment);
                fragment.notifyListeners();
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
                tme.activate(fragment);
                fragment.notifyListeners();
            }
        }
    }

    @SuppressWarnings("unused")
    private void updateCodeArea(PropertyChangeEvent evt) {
        try {
            ByteArrayEditableData data = fragment.getBinarySource().getBinaryData();
            if (data != null) {
                getCodeArea().setData(data);
            }
        } catch (IOException | TreeModelLoadException ex) {
            // todo
        }
    }

    @SuppressWarnings("unused")
    private void updateProperties(PropertyChangeEvent evt) {
        final String props = fragment.getProperties();
        propertiesArea.replaceText(CodeIndenter.indentJson(props));
    }

    @SuppressWarnings("unused")
    private void updateXml(PropertyChangeEvent evt) {
        if (fragment.getSourceType() != DocumentFragment.SourceType.text_xml) {
            xmlArea.clear();
        } else {
            // can't use getDataInputStream because of deltahex error in released artifact - fixed in trunk ...
            try {
                final byte[] xmlInput = fragment.getBinarySource().getBinaryData().getData();
                xmlArea.replaceText(CodeIndenter.indentXml(xmlInput));
            } catch (IOException|TreeModelLoadException e) {
                xmlArea.clear();
            }
        }
    }




}
