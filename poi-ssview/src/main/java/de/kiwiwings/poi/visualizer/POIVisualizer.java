package de.kiwiwings.poi.visualizer;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.IOException;

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

import de.kiwiwings.poi.ssview.treemodel.TreeModelEntry;
import de.kiwiwings.poi.ssview.treemodel.TreeModelFileSource;
import de.kiwiwings.poi.ssview.treemodel.TreeModelLoadException;

public class POIVisualizer {
	
	private final JFrame frame;
	private final DefaultMutableTreeNode treeRoot;
	private final DefaultTreeModel treeModel;
	private final JScrollPane treeView;
	private final JTree fileTree;
	private final JTabbedPane contentArea;
	private final CodeArea codeArea;
	private final JPanel structureArea;
	private final JSplitPane splitPane;
	
	private final TreeObservable treeObservable = new TreeObservable();
	
	
	
    public static void main(String[] args) {
    	POIVisualizer view = new POIVisualizer();
    	view.init();
    }
	
	POIVisualizer() {
		frame = new JFrame("POI SSView");
		treeRoot = new DefaultMutableTreeNode("not loaded ...");
		treeModel = new DefaultTreeModel(treeRoot);
	    fileTree = new JTree(treeModel);
	    treeView = new JScrollPane(fileTree);
	    contentArea = new JTabbedPane();
        codeArea = new CodeArea();
        contentArea.addTab("binary", codeArea);
        structureArea = new JPanel();
        contentArea.addTab("structure", structureArea);
        splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, treeView, contentArea);
	}
	
	void init() {
		initFrame();
		initSplitPane();
        initMenu();
        initTree();
        initCodeArea();
        
        frame.setVisible(true);
	}
	
	private void initFrame() {
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(1000, 600);
	}

	private void initSplitPane() {
		splitPane.setDividerLocation(150);
		splitPane.setOneTouchExpandable(true);
		splitPane.setContinuousLayout(true);
		final Dimension minimumSize = new Dimension(100, 50);
		for (Component c : splitPane.getComponents()) {
			c.setMinimumSize(minimumSize);
		}
		frame.add(splitPane);
	}
	
	private void initMenu() {
		final JMenuBar bar = new JMenuBar();
        frame.setJMenuBar(bar);
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
        closeItem.addActionListener(e -> frame.dispatchEvent(new WindowEvent(frame, WindowEvent.WINDOW_CLOSING)));
	}

	private void initTree() {
		fileTree.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
		fileTree.addTreeSelectionListener(e -> loadEntry(e));
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
		final int returnVal = fc.showOpenDialog(frame);
		if (returnVal != JFileChooser.APPROVE_OPTION) {
			return;
		}
		final File file = fc.getSelectedFile();
		try {
			clearCurrentFile(); 		
			new TreeModelFileSource(treeRoot, treeObservable).load(file);
			treeModel.reload(treeRoot);
		} catch (TreeModelLoadException ex) {
			JOptionPane.showMessageDialog(frame, ex.getMessage());
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
		final DefaultMutableTreeNode node = (DefaultMutableTreeNode)fileTree.getLastSelectedPathComponent();
		if (node != null && node.getUserObject() != null) {
			((TreeModelEntry)node.getUserObject()).activate();
		}
	}
}
