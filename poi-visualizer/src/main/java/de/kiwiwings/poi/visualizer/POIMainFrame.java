/* ====================================================================
   Licensed to the Apache Software Foundation (ASF) under one or more
   contributor license agreements.  See the NOTICE file distributed with
   this work for additional information regarding copyright ownership.
   The ASF licenses this file to You under the Apache License, Version 2.0
   (the "License"); you may not use this file except in compliance with
   the License.  You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
==================================================================== */

package de.kiwiwings.poi.visualizer;

import java.io.IOException;

import javax.annotation.PostConstruct;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextPane;
import javax.swing.JTree;
import javax.swing.SwingUtilities;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.tree.DefaultMutableTreeNode;

import org.exbin.deltahex.swing.CodeArea;
import org.exbin.utils.binary_data.ByteArrayEditableData;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
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
	private ApplicationContext appContext;

	@Autowired
	private TreeObservable treeObservable;

	@Autowired
	private JTabbedPane contentArea;
	
	@Autowired
	private JTree treeDir;

	@Autowired
	private CodeArea codeArea;

	@Autowired
	private JPanel structureArea;

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
        contentArea.addTab("structure", structureArea);
		add(splitPane);

		treeDir.addTreeSelectionListener(e -> loadEntry(e));
		treeDir.addMouseListener(MLFactory.mousePopup(e -> contextMenu.handleEntryClick(e)));
        treeObservable.addObserver((o, arg) -> updateCodeArea());
        addWindowListener(WLFactory.windowClosed(e -> shutdown() ));

		setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		setLocationByPlatform(true);
		setSize(1000,600);
    }

    private void shutdown() {
    	// stop timer, otherwise awt event queue is always triggered again
    	codeArea.getCaret().setBlinkRate(0);
    	SwingUtilities.invokeLater(() -> { synchronized(this) { this.notify(); } });
    }
    
	private void updateCodeArea() {
    	try {
    		ByteArrayEditableData data = treeObservable.getBinarySource().getBinaryData();
    		codeArea.setData(data);
    	} catch (IOException|TreeModelLoadException ex) {
    		// todo
    	}
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
		}
	}
}
