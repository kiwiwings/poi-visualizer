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

import static de.kiwiwings.poi.visualizer.treemodel.TreeObservable.SourceOrigin.MENU_EDIT_APPLY;

import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.InputStream;

import javax.annotation.PostConstruct;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JTabbedPane;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;

import org.apache.poi.util.BoundedInputStream;
import org.apache.poi.util.IOUtils;
import org.exbin.deltahex.swing.CodeArea;
import org.exbin.utils.binary_data.BinaryData;
import org.exbin.utils.binary_data.ByteArrayEditableData;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import de.kiwiwings.poi.visualizer.treemodel.TreeModelEntry;
import de.kiwiwings.poi.visualizer.treemodel.TreeModelFileSource;
import de.kiwiwings.poi.visualizer.treemodel.TreeModelLoadException;
import de.kiwiwings.poi.visualizer.treemodel.TreeObservable;
import de.kiwiwings.poi.visualizer.treemodel.TreeObservable.SourceType;
import de.kiwiwings.poi.visualizer.xmleditor.XMLEditor;


@SuppressWarnings("serial")
@Component(value="topMenu")
public class POITopMenuBar extends JMenuBar {

	private boolean isInit = false;


	@Autowired
	private ApplicationContext appContext;

	@Autowired
	private TreeObservable treeObservable;

	@Autowired
	private DefaultMutableTreeNode treeRoot;

	@Autowired
	private DefaultTreeModel treeModel;

	@Autowired
	private XMLEditor xmlEditor;

	@Autowired
	private JTabbedPane contentArea;
	
	private JMenuItem menuFileOpen;
	private JMenuItem menuFileClose;
	private JMenuItem menuEditIndent;
	private JMenuItem menuEditApply;
	
	@PostConstruct
	public void init() {
    	if (isInit) {
    		return;
    	}
    	isInit = true;

    	menuFileOpen = new JMenuItem("Open ...", KeyEvent.VK_O);
    	menuFileOpen.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_O, ActionEvent.CTRL_MASK));
    	menuFileOpen.addActionListener(e -> loadNewFile());
    	
    	menuFileClose = new JMenuItem("Exit", KeyEvent.VK_Q);
    	menuFileClose.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_Q, ActionEvent.CTRL_MASK));
		menuFileClose.addActionListener(e -> fireClose());
    	
    	JMenu menuFile = new JMenu("File");
    	menuFile.add(menuFileOpen);
    	menuFile.add(menuFileClose);

    	menuEditIndent = new JMenuItem("Indent", KeyEvent.VK_I);
    	menuEditIndent.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_I, ActionEvent.CTRL_MASK));
    	menuEditIndent.addActionListener(e -> editIndentXml());
    	
    	menuEditApply = new JMenuItem("Apply Changes", KeyEvent.VK_A);
    	menuEditApply.addActionListener(e -> editApply());
    	
    	JMenu menuEdit = new JMenu("Edit");
    	menuEdit.add(menuEditIndent);
    	menuEdit.add(menuEditApply);
    	
		add(menuFile);
		add(menuEdit);

		treeObservable.addObserver((o,arg) -> SwingUtilities.invokeLater(() -> contentChanged()));
	}

	private void contentChanged() {
		menuEditIndent.setEnabled(treeObservable.getSourceType() == SourceType.text_xml);
		menuEditApply.setEnabled(treeObservable.getSourceType() != SourceType.empty);
	}
	
	private void editIndentXml() {
		if (treeObservable.getSourceType() != SourceType.text_xml) {
			return;
		}
		
		xmlEditor.prettyPrint();
	}

	private void editApply() {
		java.awt.Component comp = contentArea.getSelectedComponent();
		if (comp instanceof CodeArea) {
			treeObservable.setBinarySource(() -> {
				// workaround bug in ByteArrayDataInputStream.read(byte[] output, int off, int len)
				final BinaryData bad = ((CodeArea)comp).getData();
				final long size = bad.getDataSize();
				ByteArrayEditableData data = new ByteArrayEditableData();
				try (InputStream is = new BoundedInputStream(bad.getDataInputStream(),size)) {
					data.loadFromStream(is);
				}
				return data;
			});
		} else if (comp instanceof XMLEditor) {
			((XMLEditor)comp).saveXml();
		} else {
			return;
		}
		treeObservable.notifyObservers(MENU_EDIT_APPLY);	
	}

	private void loadNewFile() {
		final JFileChooser fc = new JFileChooser();
		fc.setCurrentDirectory(new File("."));
		final int returnVal = fc.showOpenDialog(this);
		if (returnVal != JFileChooser.APPROVE_OPTION) {
			return;
		}
		final File file = fc.getSelectedFile();
		try {
			clearCurrentFile();
			appContext.getBean(TreeModelFileSource.class, treeRoot).load(file);
			treeModel.reload(treeRoot);
		} catch (TreeModelLoadException ex) {
			JOptionPane.showMessageDialog(this, ex.getMessage());
			clearCurrentFile();
		}
	}

	private void fireClose() {
		final JFrame mainFrame = appContext.getBean(POIMainFrame.class);
		final WindowEvent we = new WindowEvent(mainFrame, WindowEvent.WINDOW_CLOSING);
		mainFrame.dispatchEvent(we);
	}

	public void clearCurrentFile() {
		Object userObject = treeRoot.getUserObject();
		if (userObject instanceof TreeModelEntry) {
			IOUtils.closeQuietly((TreeModelEntry)userObject);
			treeRoot.setUserObject(null);
		}
		treeRoot.removeAllChildren();
		treeRoot.setUserObject("Not loaded ...");
		treeModel.reload(treeRoot);
	}
}
