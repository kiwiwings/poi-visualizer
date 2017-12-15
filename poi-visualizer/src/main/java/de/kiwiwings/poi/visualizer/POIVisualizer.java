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

import java.awt.event.MouseEvent;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.IOException;

import javax.annotation.PostConstruct;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.JTree;
import javax.swing.SwingUtilities;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;

import org.apache.poi.util.IOUtils;
import org.exbin.deltahex.swing.CodeArea;
import org.exbin.utils.binary_data.ByteArrayEditableData;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.AbstractApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import de.kiwiwings.poi.visualizer.treemodel.TreeModelEntry;
import de.kiwiwings.poi.visualizer.treemodel.TreeModelFileSource;
import de.kiwiwings.poi.visualizer.treemodel.TreeModelLoadException;
import de.kiwiwings.poi.visualizer.treemodel.TreeObservable;
import de.kiwiwings.poi.visualizer.util.MLFactory;

@org.springframework.stereotype.Component
public class POIVisualizer {
	
	@Autowired
	private JFrame visualizerFrame;

	@Autowired
	private DefaultMutableTreeNode treeRoot;
	
	@Autowired
	private DefaultTreeModel treeModel;

	@Autowired
	private JTree treeDir;

	@Autowired
	private CodeArea codeArea;
	
	@Autowired
	private JPanel structureArea;

	@Autowired
	private JTabbedPane contentArea;

	@Autowired
	private JSplitPane splitPane;
	
	private final TreeObservable treeObservable = new TreeObservable();
	private static ApplicationContext context; 
	
	
    public static void main(String[] args) {
    	try (AbstractApplicationContext ctx = new ClassPathXmlApplicationContext("applicationContext.xml")) {
    		context = ctx;
    		POIVisualizer view = ctx.getBean(POIVisualizer.class);
    		view.init();
    	}
    }
	

    @PostConstruct
	void init() {
        contentArea.addTab("binary", codeArea);
        contentArea.addTab("structure", structureArea);
		visualizerFrame.add(splitPane);
		context.getBean("menuFileOpen", JMenuItem.class).addActionListener(e -> loadNewFile());
		context.getBean("menuFileClose", JMenuItem.class).addActionListener(e -> fireClose());
		treeDir.addTreeSelectionListener(e -> loadEntry(e));
		treeDir.addMouseListener(MLFactory.mouseClicked(e -> handleEntryClick(e)));
        treeObservable.addObserver((o, arg) -> updateCodeArea((TreeObservable)o));

        visualizerFrame.setVisible(true);
	}
	
	private void loadNewFile() {
		final JFileChooser fc = new JFileChooser();
		fc.setCurrentDirectory(new File("."));
		final int returnVal = fc.showOpenDialog(visualizerFrame);
		if (returnVal != JFileChooser.APPROVE_OPTION) {
			return;
		}
		final File file = fc.getSelectedFile();
		try {
			clearCurrentFile(); 		
			new TreeModelFileSource(treeRoot).load(file);
			treeModel.reload(treeRoot);
		} catch (TreeModelLoadException ex) {
			JOptionPane.showMessageDialog(visualizerFrame, ex.getMessage());
			clearCurrentFile();
		}
	}
	
	private void updateCodeArea(TreeObservable o) {
    	try {
    		ByteArrayEditableData data = o.getBinarySource().getBinaryData();
    		codeArea.setData(data);
    	} catch (IOException|TreeModelLoadException ex) {
    		// todo
    	}
	}
	
	private void fireClose() {
		final WindowEvent we = new WindowEvent(visualizerFrame, WindowEvent.WINDOW_CLOSING);
		visualizerFrame.dispatchEvent(we);
	}
	
	private void clearCurrentFile() {
		Object userObject = treeRoot.getUserObject();
		if (userObject instanceof TreeModelEntry) {
			IOUtils.closeQuietly((TreeModelEntry)userObject);
			treeRoot.setUserObject(null);
		}
		treeRoot.removeAllChildren();
		treeRoot.setUserObject("Not loaded ...");
		treeModel.reload(treeRoot);
	}
	
	private void loadEntry(final TreeSelectionEvent e) {
		final DefaultMutableTreeNode node =
			(DefaultMutableTreeNode)treeDir.getLastSelectedPathComponent();
		if (node == null) {
			return;
		}
		final Object userObject = node.getUserObject();
		if (userObject instanceof TreeModelEntry) {
			((TreeModelEntry)userObject).activate(treeObservable);
		}
	}
	
	private void handleEntryClick(final MouseEvent mouseEvent) {
		if (SwingUtilities.isRightMouseButton(mouseEvent)) {
	        final TreePath path = treeDir.getClosestPathForLocation(mouseEvent.getX(), mouseEvent.getY());
	        if (path != null) {
	        	DefaultMutableTreeNode node = (DefaultMutableTreeNode)path.getLastPathComponent();
	        	// TODO: show popup for saving the stream
	        }
		}
	}
}
