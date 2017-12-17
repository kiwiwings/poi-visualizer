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

import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import javax.annotation.PostConstruct;
import javax.swing.JFileChooser;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPopupMenu;
import javax.swing.JTree;
import javax.swing.tree.TreePath;

import org.exbin.utils.binary_data.ByteArrayEditableData;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import de.kiwiwings.poi.visualizer.treemodel.TreeModelLoadException;
import de.kiwiwings.poi.visualizer.treemodel.TreeObservable;

@Component
@SuppressWarnings("serial")
public class POIContextMenu extends JPopupMenu {

	private boolean isInit = false;
	private JMenuItem saveStream;

	@Autowired
	private TreeObservable treeObservable;

	@PostConstruct
	public void init() {
    	if (isInit) {
    		return;
    	}

    	isInit = true;

    	saveStream = new JMenuItem("Save ...", KeyEvent.VK_S);
    	saveStream.addActionListener(e -> saveFile());

    	add(saveStream);
	}

	public void handleEntryClick(final MouseEvent mouseEvent) {
		if (!mouseEvent.isPopupTrigger() || !(mouseEvent.getComponent() instanceof JTree)) {
			return;
		}

		JTree treeDir = (JTree)mouseEvent.getComponent();
        final TreePath path = treeDir.getClosestPathForLocation(mouseEvent.getX(), mouseEvent.getY());
        if (path == null) {
        	return;
        }
    	treeDir.setSelectionPath(path);
    	
    	boolean hasStream;
    	try {
    		ByteArrayEditableData data = treeObservable.getBinarySource().getBinaryData();
    		hasStream = (data.getDataSize() > 0);
    	} catch (Exception e) {
    		hasStream = false;
    	}

    	saveStream.setEnabled(hasStream);
    	
    	show(treeDir, mouseEvent.getX(), mouseEvent.getY());
	}

	private void saveFile() {
		final JFileChooser fc = new JFileChooser();
		fc.setCurrentDirectory(new File("."));
		fc.setSelectedFile(new File(treeObservable.getFileName()));
		final int returnVal = fc.showOpenDialog(this);
		if (returnVal != JFileChooser.APPROVE_OPTION) {
			return;
		}
		final File file = fc.getSelectedFile();
		try (FileOutputStream fos = new FileOutputStream(file)) {
			ByteArrayEditableData data = treeObservable.getBinarySource().getBinaryData();
			fos.write(data.getData());
		} catch (IOException|TreeModelLoadException ex) {
			JOptionPane.showMessageDialog(this, ex.getMessage());
		}
	}
}
