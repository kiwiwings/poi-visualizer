package de.kiwiwings.poi.ssview.treemodel;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import javax.swing.tree.DefaultMutableTreeNode;

import org.apache.poi.poifs.filesystem.DirectoryNode;
import org.apache.poi.poifs.filesystem.DocumentNode;
import org.apache.poi.poifs.filesystem.Entry;
import org.apache.poi.poifs.filesystem.POIFSFileSystem;
import org.apache.poi.util.IOUtils;
import org.exbin.utils.binary_data.ByteArrayEditableData;

import de.kiwiwings.poi.visualizer.TreeObservable;

public class TreeModelPOIFSSource implements TreeModelSource {

	final DefaultMutableTreeNode parent;
	final TreeObservable treeObservable;

	TreeModelPOIFSSource(final DefaultMutableTreeNode parent, final TreeObservable treeObservable) {
		this.parent = parent;
		this.treeObservable = treeObservable;
	}

	@SuppressWarnings("resource")
	public void load(Object source) throws TreeModelLoadException {
		if (!(source instanceof File)) {
			throw new TreeModelLoadException("source isn't a file.");
		}

		POIFSFileSystem poifs = null;
		try {
			poifs = new POIFSFileSystem((File)source);
			traverseFileSystem(poifs.getRoot(), parent);
		} catch (IOException ex) {
			IOUtils.closeQuietly(poifs);
			throw new TreeModelLoadException("Error in opening '"+((File)source).getPath()+"'");
		}

	}

	private void traverseFileSystem(Entry poifsNode, DefaultMutableTreeNode treeNode) {
		final TreeModelEntry tme;
		final DirectoryNode dn;
		if (poifsNode instanceof DirectoryNode) {
			dn = (DirectoryNode)poifsNode;
			tme = (dn.getParent() == null) ? new POIFSRootEntry(dn, treeNode) : new POIFSDirEntry(dn, treeNode);
		} else {
			dn = null;
			tme = new POIFSEntry(poifsNode, treeNode);
		}
		treeNode.setUserObject(tme);
		if (dn != null) {
			for (Entry poifsChild : dn) {
				DefaultMutableTreeNode treeChild = new DefaultMutableTreeNode();
				treeNode.add(treeChild);
				traverseFileSystem(poifsChild, treeChild);
			}
		}
	}


	private class POIFSEntry implements TreeModelEntry {
		final Entry entry;
		final DefaultMutableTreeNode treeNode;
		final String oldNodeName;
		
		POIFSEntry(final Entry entry, final DefaultMutableTreeNode treeNode) {
			this.entry = entry;
			this.treeNode = treeNode;
			Object obj = treeNode.getUserObject();
			oldNodeName = (obj != null) ? obj.toString() : null;
		}
		@Override
		public String toString() {
			return 
				(oldNodeName != null ? oldNodeName+" (" : "")
				+ escapeString(entry.getName())
				+ (oldNodeName != null ? ")" : "");
		}

		@Override
		public void activate() {
			treeObservable.setBinarySource(() -> getData());
			treeObservable.setStructuredSource(null);
			treeObservable.notifyObservers();
		}

		private ByteArrayEditableData getData() throws IOException {
			final DocumentNode dn = (DocumentNode)entry;
			final DirectoryNode parent = (DirectoryNode)dn.getParent();
			final ByteArrayEditableData data;
			try (final InputStream is = parent.createDocumentInputStream(dn)) {
				data = new ByteArrayEditableData();
				data.loadFromStream(is);
				return data;
			}
		}


		@Override
		public void close() throws IOException {

		}
	}

	private class POIFSDirEntry extends POIFSEntry {
		POIFSDirEntry(final DirectoryNode dirEntry, final DefaultMutableTreeNode treeNode) {
			super(dirEntry, treeNode);
		}

		@Override
		public void activate() {
		}
	}

	private class POIFSRootEntry extends POIFSDirEntry {
		POIFSRootEntry(final DirectoryNode dirEntry, final DefaultMutableTreeNode treeNode) {
			super(dirEntry, treeNode);
		}

		@Override
		public void close() throws IOException {
			((DirectoryNode)this.entry).getFileSystem().close();
		}
	}
}