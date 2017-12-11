package de.kiwiwings.poi.visualizer.treemodel;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import javax.swing.JOptionPane;
import javax.swing.tree.DefaultMutableTreeNode;

import org.apache.poi.poifs.filesystem.FileMagic;
import org.apache.poi.poifs.filesystem.POIFSFileSystem;

import de.kiwiwings.poi.visualizer.TreeObservable;

public class TreeModelFileSource implements TreeModelSource {
	
	final DefaultMutableTreeNode parent;
	final TreeObservable treeObservable;
	
	public TreeModelFileSource(final DefaultMutableTreeNode parent, final TreeObservable treeObservable) {
		this.parent = parent;
		this.treeObservable = treeObservable;
	}
	
	public void load(final Object source) throws TreeModelLoadException {
		if (!(source instanceof File)) {
			throw new TreeModelLoadException("source isn't a file.");
		}
		
		final File file = (File)source;
		
		final FileMagic fm;
		try (InputStream is = FileMagic.prepareToCheckMagic(new FileInputStream(file))) {
			fm = FileMagic.valueOf(is);
		} catch (IOException ex) {
			throw new TreeModelLoadException("File Magic can't be determined.", ex);
		}

		switch (fm) {
		case OLE2:
			new TreeModelPOIFSSource(parent, treeObservable).load(source);
			break;
		case OOXML:
			new TreeModelOPCSource(parent, treeObservable).load(source);
			break;
		default:
			throw new TreeModelLoadException("File with file magic '"+fm+"' can't be processed.");
		}
	}
}
