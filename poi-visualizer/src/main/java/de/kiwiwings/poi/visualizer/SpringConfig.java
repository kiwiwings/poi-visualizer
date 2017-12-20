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

import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextPane;
import javax.swing.JTree;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeSelectionModel;

import org.exbin.deltahex.swing.CodeArea;
import org.exbin.utils.binary_data.ByteArrayEditableData;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

@Configuration
@ComponentScan("de.kiwiwings.poi.visualizer")
public class SpringConfig {

	@Bean
	public DefaultMutableTreeNode treeRoot() {
		return new DefaultMutableTreeNode("not loaded ...");
	}
	
	@Bean
	public DefaultTreeModel treeModel(
		@Qualifier("treeRoot") DefaultMutableTreeNode treeRoot
	) {
		return new DefaultTreeModel(treeRoot);
	}
	
	@Bean
	public JTree treeDir(final @Qualifier("treeModel") DefaultTreeModel treeModel) {
		final JTree tree = new JTree(treeModel);
		tree.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
		return tree;
	}
	
	@Bean
	public JScrollPane treeScroll(final @Qualifier("treeDir") JTree treeDir) {
		return new JScrollPane(treeDir);
	}
	
	@Bean
	public CodeArea codeArea() {
		final CodeArea ca = new CodeArea();
		ca.setData(new ByteArrayEditableData());
		return ca;
	}

	@Bean
	public JTextPane propertiesArea() {
		return new JTextPane();
	}

	@Bean
	public JTabbedPane contentArea() {
		return new JTabbedPane();
	}
	
	@Bean
	public JSplitPane splitPane(
		@Qualifier("treeScroll") JScrollPane treeScroll,
		@Qualifier("contentArea") JTabbedPane contentArea
	) {
		final JSplitPane splitPane = new JSplitPane();
		splitPane.setLeftComponent(treeScroll);
		splitPane.setRightComponent(contentArea);
		splitPane.setDividerLocation(150);
		splitPane.setOneTouchExpandable(true);
		splitPane.setContinuousLayout(true);
		return splitPane;
	}
}
