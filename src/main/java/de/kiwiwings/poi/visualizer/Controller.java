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
import javafx.event.Event;
import javafx.event.EventType;
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
import org.apache.poi.hpsf.ClassIDPredefined;
import org.apache.poi.util.IOUtils;
import org.exbin.deltahex.swing.CodeArea;
import org.exbin.utils.binary_data.ByteArrayEditableData;
import org.w3c.dom.DOMConfiguration;
import org.w3c.dom.DOMImplementation;
import org.w3c.dom.Document;
import org.w3c.dom.ls.DOMImplementationLS;
import org.w3c.dom.ls.LSOutput;
import org.w3c.dom.ls.LSSerializer;
import org.xml.sax.SAXException;

import javax.json.Json;
import javax.json.JsonReader;
import javax.json.JsonWriter;
import javax.json.stream.JsonGenerator;
import javax.swing.*;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.*;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Controller implements Initializable {

    private static final Pattern PROPERTY_COMMENT = Pattern.compile("((\\d+)|(\"\\{\\p{XDigit}{8}(-\\p{XDigit}{4}){3}-\\p{XDigit}{12}\\}\"))(,?)$", Pattern.MULTILINE);

    private static final Object[][] classIdMap = {
        {ClassIDPredefined.OLE_V1_PACKAGE, "OLE10_PACKAGE"},
        {ClassIDPredefined.POWERPOINT_V8, "PPT_SHOW"},
        {ClassIDPredefined.EXCEL_V7_WORKBOOK, "XLS_WORKBOOK"},
        {ClassIDPredefined.TXT_ONLY, "TXT_ONLY"},
        {ClassIDPredefined.EXCEL_V3, "EXCEL_V3"},
        {ClassIDPredefined.EXCEL_V3_CHART, "EXCEL_V3_CHART"},
        {ClassIDPredefined.EXCEL_V3_MACRO, "EXCEL_V3_MACRO"},
        {ClassIDPredefined.EXCEL_V7, "EXCEL95"},
        {ClassIDPredefined.EXCEL_V7_CHART, "EXCEL95_CHART"},
        {ClassIDPredefined.EXCEL_V8, "EXCEL97"},
        {ClassIDPredefined.EXCEL_V8_CHART, "EXCEL97_CHART"},
        {ClassIDPredefined.EXCEL_V11, "EXCEL2003"},
        {ClassIDPredefined.EXCEL_V12, "EXCEL2007"},
        {ClassIDPredefined.EXCEL_V12_MACRO, "EXCEL2007_MACRO"},
        {ClassIDPredefined.EXCEL_V12_XLSB, "EXCEL2007_XLSB"},
        {ClassIDPredefined.EXCEL_V14, "EXCEL2010"},
        {ClassIDPredefined.EXCEL_V14_CHART, "EXCEL2010_CHART"},
        {ClassIDPredefined.EXCEL_V14_ODS, "EXCEL2010_ODS"},
        {ClassIDPredefined.WORD_V7, "WORD95"},
        {ClassIDPredefined.WORD_V8, "WORD97"},
        {ClassIDPredefined.WORD_V12, "WORD2007"},
        {ClassIDPredefined.WORD_V12_MACRO, "WORD2007_MACRO"},
        {ClassIDPredefined.POWERPOINT_V7, "POWERPOINT95"},
        {ClassIDPredefined.POWERPOINT_V8, "POWERPOINT97"},
        {ClassIDPredefined.POWERPOINT_V12, "POWERPOINT2007"},
        {ClassIDPredefined.POWERPOINT_V12_MACRO, "POWERPOINT2007_MACRO"},
        {ClassIDPredefined.EQUATION_V3, "EQUATION30"},
    };

    @FXML
    private TreeView<TreeModelEntry> treeDir;

    @FXML
    private SwingNode deltaHexSN;

    @FXML
    private org.fxmisc.richtext.CodeArea xmlArea;

    private CodeFormatter xmlAreaFormatter;

    @FXML
    private org.fxmisc.richtext.CodeArea propertiesArea;

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

        xmlAreaFormatter = new CodeFormatter(xmlArea);
        propertiesAreaFormatter = new CodeFormatter(propertiesArea);

//        treeDir.addEventHandler(EventType.ROOT, this::onKey);
    }

    public void setStage(Stage stage) {
        this.stage = stage;
    }

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

    public void openFile(File file) {
        workingDir = file.getParentFile();

        clearCurrentFile();

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

    public void clearCurrentFile() {
        stage.setTitle("POI Visualizer - <no file>");
        TreeItem<TreeModelEntry> tr = treeDir.getRoot();
        if (tr != null && tr.getValue() != null) {
            IOUtils.closeQuietly(tr.getValue());
        }
        treeDir.setRoot(null);
//        treeRoot.setUserObject("Not loaded ...");
//        treeModel.reload(treeRoot);
        treeObservable.setProperties("");
    }


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

    private void updateProperties(final Observable o, final Object arg) {
        final String props = treeObservable.getProperties();
        if (props == null || "".equals(props)) {
            propertiesArea.clear();
        } else if (!props.startsWith("{")) {
            propertiesArea.replaceText(props);
        } else {
            try (
                StringWriter writer = new StringWriter();
                JsonReader jReader = createJsonReader(new StringReader(props));
                JsonWriter jWriter = createJsonWriter(writer)
            ) {
                jWriter.write(jReader.readObject());
                // add comment for ...
                // numbers to hexadecimal number
                // class id to class id name, if possible
                final Matcher mat = PROPERTY_COMMENT.matcher(writer.getBuffer());
                final StringBuffer buf = new StringBuffer();
                while (mat.find()) {
                    final String match = mat.group(1);
                    final String comment;
                    if (match.contains("-")) {
                        comment = getNameFromClassID(match);
                    } else {
                        final long l = Long.parseLong(mat.group(1));
                        comment = "0x" + Long.toHexString(l);
                    }
                    mat.appendReplacement(buf, mat.group() + " /*" + comment + "*/");
                }
                mat.appendTail(buf);
                propertiesArea.replaceText(buf.toString().trim());
            } catch (IOException ex) {
                propertiesArea.replaceText(props);
            }
        }
    }

    private JsonReader createJsonReader(Reader reader) {
        return Json.createReaderFactory(null).createReader(reader);
    }

    private JsonWriter createJsonWriter(Writer writer) {
        final Map<String, Object> props = Collections.singletonMap(JsonGenerator.PRETTY_PRINTING, true);
        return Json.createWriterFactory(props).createWriter(writer);
    }

    private String getNameFromClassID(String match) {
        // TODO replace with ClassIDPredefined enum ...
        for (Object[] obj : classIdMap) {
            if (match.contains(((ClassIDPredefined) obj[0]).getClassID().toString())) {
                return (String) obj[1];
            }
        }
        return "unknown classid";
    }

    private static final Object[][] DOM_PARAMS = {
        { "format-pretty-print", true },
        { "infoset", true },
        { "xml-declaration", false },
        { "{http://xml.apache.org/xalan}indent-amount", "2" }
    };

    private void updateXml(final Observable o, final Object arg) {
        if (treeObservable.getSourceType() != TreeObservable.SourceType.text_xml) {
            xmlArea.clear();
        } else {
            // can't use getDataInputStream because of deltahex error in released artifact - fixed in trunk ...
            try (InputStream is = new ByteArrayInputStream(treeObservable.getBinarySource().getBinaryData().getData())) {
                final Document doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(is);
                final String xmlDecl = getXmlDeclaration(is);


                // Pretty-prints a DOM document to XML using DOM Load and Save's LSSerializer.
                // Note that the "format-pretty-print" DOM configuration parameter can only be set in JDK 1.6+.
                final DOMImplementation domImplementation = doc.getImplementation();
                if (!domImplementation.hasFeature("LS", "3.0") || !domImplementation.hasFeature("Core", "2.0")) {
                    return;
                }
                final DOMImplementationLS domImplementationLS = (DOMImplementationLS) domImplementation.getFeature("LS", "3.0");
                final LSSerializer lsSerializer = domImplementationLS.createLSSerializer();
                final DOMConfiguration domConfig = lsSerializer.getDomConfig();

                for (final Object[] domParam : DOM_PARAMS) {
                    if (domConfig.canSetParameter((String)domParam[0], domParam[1])) {
                        domConfig.setParameter((String)domParam[0], domParam[1]);
                    }
                }

                final LSOutput lsOutput = domImplementationLS.createLSOutput();
                lsOutput.setEncoding("UTF-8");
                final StringWriter stringWriter = new StringWriter(50000);
                stringWriter.write(xmlDecl);
                stringWriter.write("\n");

                lsOutput.setCharacterStream(stringWriter);
                lsSerializer.write(doc, lsOutput);
                xmlArea.replaceText(stringWriter.toString());
            } catch (IOException | TreeModelLoadException | ParserConfigurationException | SAXException e) {
                xmlArea.clear();
            }
        }
    }

    /**
     * LSSerializer ignores standalone in the xml declaration, therefore we need to parse
     * the xml declaration separately
     * @param is the xml inputstream
     * @return the xml declaration
     * @throws IOException
     */
    private static String getXmlDeclaration(final InputStream is) throws IOException {
        try (InputStreamReader isr = new InputStreamReader(is, Charset.forName("UTF-8"))) {
            is.reset();

            char[] buf = new char[1000];
            isr.read(buf);

            final Pattern p = Pattern.compile("^<\\?xml.*\\?>");
            final Matcher m = p.matcher(new String(buf));
            return (m.find()) ? m.group() : "<?xml version=\"1.0\" encoding=\"utf-8\"?>";
        }
    }





}
