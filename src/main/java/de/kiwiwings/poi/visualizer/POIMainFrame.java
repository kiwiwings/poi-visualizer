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

import java.awt.event.WindowEvent;
import java.io.File;
import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.Writer;
import java.util.Collections;
import java.util.Map;
import java.util.Observable;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.json.Json;
import javax.json.JsonReader;
import javax.json.JsonWriter;
import javax.json.stream.JsonGenerator;
import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.JTree;
import javax.swing.SwingUtilities;
import javax.swing.WindowConstants;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.text.JTextComponent;
import javax.swing.tree.DefaultMutableTreeNode;

import org.apache.poi.hpsf.ClassID;
import org.exbin.deltahex.swing.CodeArea;
import org.exbin.utils.binary_data.ByteArrayEditableData;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import de.kiwiwings.poi.visualizer.treemodel.TreeModelEntry;
import de.kiwiwings.poi.visualizer.treemodel.TreeModelLoadException;
import de.kiwiwings.poi.visualizer.treemodel.TreeObservable;
import de.kiwiwings.poi.visualizer.util.MLFactory;
import de.kiwiwings.poi.visualizer.util.WLFactory;
import de.kiwiwings.poi.visualizer.xmleditor.XMLEditor;

@Component
public class POIMainFrame extends JFrame implements InitializingBean {

	private static final long serialVersionUID = 4777146707371974468L;

	private static final Pattern PROPERTY_COMMENT = Pattern.compile("((\\d+)|(\"\\{\\p{XDigit}{8}(-\\p{XDigit}{4}){3}-\\p{XDigit}{12}\\}\"))(,?)$", Pattern.MULTILINE);

	@Autowired
	private TreeObservable treeObservable;

	@Autowired
	private JTabbedPane contentArea;

	@Autowired
	private JTree treeDir;

	@Autowired
	private CodeArea codeArea;

	@Autowired
	private JTextComponent propertiesArea;

	@Autowired
	private JSplitPane splitPane;

	@Autowired
	private POITopMenuBar topMenu;

	@Autowired
	private POIContextMenu contextMenu;

	@Autowired
	private XMLEditor xmlEditor;

	public POIMainFrame() {
		super("POI Visualizer");
	}

	public void setFileTitle(File newFile) {
		setTitle("POI Visualizer - "+newFile.getName());
	}


	@Override
	public void afterPropertiesSet() {
		setJMenuBar(topMenu);

        contentArea.addTab("binary", codeArea);
        contentArea.addTab("xml", xmlEditor);

        JScrollPane propScroll = new JScrollPane(propertiesArea);
        contentArea.addTab("properties", propScroll);
		add(splitPane);

		treeDir.addTreeSelectionListener(this::loadEntry);
		treeDir.addMouseListener(MLFactory.mousePopup(contextMenu::handleEntryClick));
        treeObservable.addObserver(this::updateCodeArea);
        treeObservable.addObserver(this::updateProperties);
        addWindowListener(WLFactory.windowClosed(this::shutdown));

		setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
		setLocationByPlatform(true);
		setSize(1000,600);
    }

    private void shutdown(final WindowEvent e) {
    	// stop timer, otherwise awt event queue is always triggered again
    	codeArea.getCaret().setBlinkRate(0);
    	SwingUtilities.invokeLater(() -> { synchronized(this) { this.notify(); } });
    }

	private void updateCodeArea(final Observable o, final Object arg) {
    	try {
    		ByteArrayEditableData data = treeObservable.getBinarySource().getBinaryData();
    		codeArea.setData(data);
    	} catch (IOException|TreeModelLoadException ex) {
    		// todo
    	}
	}

	private void updateProperties(final Observable o, final Object arg) {
		final String props = treeObservable.getProperties();
		if (props == null || "".equals(props)) {
			propertiesArea.setText("");
		} else if (!props.startsWith("{")) {
			propertiesArea.setText(props);
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
						comment = "0x"+Long.toHexString(l);
					}
					mat.appendReplacement(buf, mat.group()+" /*"+comment+"*/");
				}
				mat.appendTail(buf);
				propertiesArea.setText(buf.toString());
			} catch (IOException ex) {
				propertiesArea.setText(props);
			}
		}
	}

	private JsonReader createJsonReader(Reader reader) {
		return Json.createReaderFactory(null).createReader(reader);
	}

	private JsonWriter createJsonWriter(Writer writer) {
		final Map<String,Object> props = Collections.singletonMap(JsonGenerator.PRETTY_PRINTING, true);
		return Json.createWriterFactory(props).createWriter(writer);
	}

	private void loadEntry(final TreeSelectionEvent e) {
		final DefaultMutableTreeNode node =
			(DefaultMutableTreeNode)treeDir.getLastSelectedPathComponent();
		if (node == null) {
			return;
		}
		final Object userObject = node.getUserObject();
		if (userObject instanceof TreeModelEntry) {
			((TreeModelEntry)userObject).activate();
			treeObservable.notifyObservers();
		}
	}

	

	private static final Object[][] classIdMap = {
	    { ClassID.OLE10_PACKAGE, "OLE10_PACKAGE" },
	    { ClassID.PPT_SHOW, "PPT_SHOW" },
	    { ClassID.XLS_WORKBOOK, "XLS_WORKBOOK" },
	    { ClassID.TXT_ONLY, "TXT_ONLY" },
	    { ClassID.EXCEL_V3, "EXCEL_V3" },
	    { ClassID.EXCEL_V3_CHART, "EXCEL_V3_CHART" },
	    { ClassID.EXCEL_V3_MACRO, "EXCEL_V3_MACRO" },
	    { ClassID.EXCEL95, "EXCEL95" },
	    { ClassID.EXCEL95_CHART, "EXCEL95_CHART" },
	    { ClassID.EXCEL97, "EXCEL97" },
	    { ClassID.EXCEL97_CHART, "EXCEL97_CHART" },
	    { ClassID.EXCEL2003, "EXCEL2003" },
	    { ClassID.EXCEL2007, "EXCEL2007" },
	    { ClassID.EXCEL2007_MACRO, "EXCEL2007_MACRO" },
	    { ClassID.EXCEL2007_XLSB, "EXCEL2007_XLSB" },
	    { ClassID.EXCEL2010, "EXCEL2010" },
	    { ClassID.EXCEL2010_CHART, "EXCEL2010_CHART" },
	    { ClassID.EXCEL2010_ODS, "EXCEL2010_ODS" },
	    { ClassID.WORD97, "WORD97" },
	    { ClassID.WORD95, "WORD95" },
	    { ClassID.WORD2007, "WORD2007" },
	    { ClassID.WORD2007_MACRO, "WORD2007_MACRO" },
	    { ClassID.POWERPOINT97, "POWERPOINT97" },
	    { ClassID.POWERPOINT95, "POWERPOINT95" },
	    { ClassID.POWERPOINT2007, "POWERPOINT2007" },
	    { ClassID.POWERPOINT2007_MACRO, "POWERPOINT2007_MACRO" },
	    { ClassID.EQUATION30, "EQUATION30" },
	};
	
	private String getNameFromClassID(String match) {
		// TODO replace with ClassIDPredefined enum ...
		for (Object[] obj : classIdMap) {
			if (match.contains(obj[0].toString())) {
				return (String)obj[1];
			}
		}
		return "unknown classid";
	}
}
