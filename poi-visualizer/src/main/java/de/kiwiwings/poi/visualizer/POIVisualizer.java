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

import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.IOException;

import javax.annotation.PostConstruct;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.JTree;
import javax.swing.KeyStroke;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeSelectionModel;

import org.apache.poi.util.IOUtils;
import org.exbin.deltahex.swing.CodeArea;
import org.exbin.utils.binary_data.ByteArrayEditableData;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.support.AbstractApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import de.kiwiwings.poi.visualizer.treemodel.TreeModelEntry;
import de.kiwiwings.poi.visualizer.treemodel.TreeModelFileSource;
import de.kiwiwings.poi.visualizer.treemodel.TreeModelLoadException;
import de.kiwiwings.poi.visualizer.treemodel.TreeObservable;

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
	private JScrollPane treeScroll;
	
	@Autowired
	private CodeArea codeArea;
	
	@Autowired
	private JPanel structureArea;

	@Autowired
	private JTabbedPane contentArea;

	@Autowired
	private JSplitPane splitPane;
	
	private final TreeObservable treeObservable = new TreeObservable();
	
	
	
    public static void main(String[] args) {
    	try (AbstractApplicationContext ctx = new ClassPathXmlApplicationContext("applicationContext.xml")) {
    		POIVisualizer view = ctx.getBean(de.kiwiwings.poi.visualizer.POIVisualizer.class);
    		view.init();
    	}
    }
	

    @PostConstruct
	void init() {
        contentArea.addTab("binary", codeArea);
        contentArea.addTab("structure", structureArea);

        initFrame();
		initSplitPane();
        initMenu();
        initTree();
        initCodeArea();
        
        visualizerFrame.setVisible(true);
	}
	
	private void initFrame() {
        visualizerFrame.setSize(1000, 600);
	}

	private void initSplitPane() {
		visualizerFrame.add(splitPane);
	}
	
	private void initMenu() {
		final JMenuBar bar = new JMenuBar();
        visualizerFrame.setJMenuBar(bar);
        final JMenu fileMenu = new JMenu("File");
        bar.add(fileMenu);
        final JMenuItem openItem = new JMenuItem("Open ...", KeyEvent.VK_O);
        openItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_O, ActionEvent.CTRL_MASK));
        fileMenu.add(openItem);
        openItem.addActionListener(e -> loadNewFile());
        
        fileMenu.addSeparator();
        final JMenuItem closeItem = new JMenuItem("Exit", KeyEvent.VK_X);
        closeItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_Q, ActionEvent.CTRL_MASK));
        fileMenu.add(closeItem);
        closeItem.addActionListener(e -> visualizerFrame.dispatchEvent(new WindowEvent(visualizerFrame, WindowEvent.WINDOW_CLOSING)));
	}

	private void initTree() {
		treeDir.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
		treeDir.addTreeSelectionListener(e -> loadEntry(e));
	}
	
	private void initCodeArea() {
        codeArea.setData(new ByteArrayEditableData(new byte[]{1, 2, 3}));
        treeObservable.addObserver((o, arg) -> {
        	try {
        		ByteArrayEditableData data = ((TreeObservable)o).getBinarySource().getBinaryData();
        		codeArea.setData(data);
        	} catch (IOException|TreeModelLoadException ex) {
        		// todo
        	}
        });
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
			new TreeModelFileSource(treeRoot, treeObservable).load(file);
			treeModel.reload(treeRoot);
		} catch (TreeModelLoadException ex) {
			JOptionPane.showMessageDialog(visualizerFrame, ex.getMessage());
			clearCurrentFile();
		}
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
		final DefaultMutableTreeNode node = (DefaultMutableTreeNode)treeDir.getLastSelectedPathComponent();
		if (node != null && node.getUserObject() != null) {
			((TreeModelEntry)node.getUserObject()).activate();
		}
	}
	
	
	
}
