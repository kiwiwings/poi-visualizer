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

import javax.annotation.PostConstruct;
import javax.json.Json;
import javax.json.JsonReader;
import javax.json.JsonWriter;
import javax.json.stream.JsonGenerator;
import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextPane;
import javax.swing.JTree;
import javax.swing.SwingUtilities;
import javax.swing.WindowConstants;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.tree.DefaultMutableTreeNode;

import org.exbin.deltahex.swing.CodeArea;
import org.exbin.utils.binary_data.ByteArrayEditableData;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import de.kiwiwings.poi.visualizer.treemodel.TreeModelEntry;
import de.kiwiwings.poi.visualizer.treemodel.TreeModelLoadException;
import de.kiwiwings.poi.visualizer.treemodel.TreeObservable;
import de.kiwiwings.poi.visualizer.util.MLFactory;
import de.kiwiwings.poi.visualizer.util.WLFactory;
import de.kiwiwings.poi.visualizer.xmleditor.XMLEditor;

@Component
public class POIMainFrame extends JFrame {

	private static final long serialVersionUID = 4777146707371974468L;

	@Autowired
	private TreeObservable treeObservable;

	@Autowired
	private JTabbedPane contentArea;

	@Autowired
	private JTree treeDir;

	@Autowired
	private CodeArea codeArea;

	@Autowired
	private JTextPane propertiesArea;

	@Autowired
	private JSplitPane splitPane;

	@Autowired
	private POITopMenuBar topMenu;

	@Autowired
	private POIContextMenu contextMenu;

	@Autowired
	private XMLEditor xmlEditor;

	private boolean isInit = false;

	public POIMainFrame() {
		super("POI Visualizer");
	}

	public void setFileTitle(File newFile) {
		setTitle("POI Visualizer - "+newFile.getName());
	}
	

    @PostConstruct
	public void init() {
    	if (isInit) {
    		return;
    	}

    	isInit = true;

    	topMenu.init();
		setJMenuBar(topMenu);

		contextMenu.init();
		xmlEditor.init();

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
				final Pattern pat = Pattern.compile("([0-9]+)(,?)$", Pattern.MULTILINE);
				final Matcher mat = pat.matcher(writer.getBuffer());
				final StringBuffer buf = new StringBuffer();
				while (mat.find()) {
					final long l = Long.parseLong(mat.group(1)); 
					mat.appendReplacement(buf, mat.group()+" /*0x"+Long.toHexString(l)+"*/");
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
}
