package de.kiwiwings.poi.ssview.treemodel;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.tree.DefaultMutableTreeNode;

import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.openxml4j.opc.OPCPackage;
import org.apache.poi.openxml4j.opc.PackageAccess;
import org.apache.poi.openxml4j.opc.PackagePart;
import org.apache.poi.openxml4j.opc.internal.PackagePropertiesPart;
import org.apache.poi.poifs.filesystem.FileMagic;
import org.apache.poi.util.IOUtils;
import org.apache.poi.util.LocaleUtil;
import org.apache.poi.util.TempFile;
import org.exbin.utils.binary_data.ByteArrayEditableData;

import de.kiwiwings.poi.ssview.TreeObservable;

public class TreeModelOPCSource implements TreeModelSource {
	
	final DefaultMutableTreeNode parent;
	final TreeObservable treeObservable;
	
	TreeModelOPCSource(final DefaultMutableTreeNode parent, final TreeObservable treeObservable) {
		this.parent = parent;
		this.treeObservable = treeObservable;
	}
	
	public void load(Object source) throws TreeModelLoadException {
		if (!(source instanceof File)) {
			throw new TreeModelLoadException("source isn't a file.");
		}
		
		OPCPackage opc = null;
		try {
			opc = OPCPackage.open((File)source, PackageAccess.READ);
			OPCRootEntry opcRoot = new OPCRootEntry(opc, parent);
			parent.setUserObject(opcRoot);

			final Map<String,DefaultMutableTreeNode> mapFolders = new HashMap<>();
			final Map<String,List<DefaultMutableTreeNode>> mapFiles = new HashMap<>();
			mapFolders.put("/", parent);
			
			// first create the folders, so we don't have folders and files mixed in the tree
			for (final PackagePart pp : opc.getParts()) {
				final String uri = pp.getPartName().toString();
				DefaultMutableTreeNode parDir = parent;
				for (int idx=1;(idx=uri.indexOf('/',idx)) != -1;idx++) {
					final String path = uri.substring(0,idx);
					final DefaultMutableTreeNode dir;
					if (mapFolders.containsKey(path)) {
						dir = mapFolders.get(path);
					} else {
						dir = new DefaultMutableTreeNode();
						final OPCDirEntry entry = new OPCDirEntry(path, dir);
						dir.setUserObject(entry);
						mapFolders.put(path, dir);
						parDir.add(dir);
					}
					parDir = dir;
				}

				// temporarily store the entries
				final String parPath = ((OPCDirEntry)parDir.getUserObject()).getPath();
				final List<DefaultMutableTreeNode> listFiles;
				if (mapFiles.containsKey(parPath)) {
					listFiles = mapFiles.get(parPath);
				} else {
					listFiles = new ArrayList<DefaultMutableTreeNode>();
					mapFiles.put(parPath, listFiles);
				}
				final DefaultMutableTreeNode node = new DefaultMutableTreeNode();
				node.setUserObject(new OPCEntry(pp, node));
				listFiles.add(node);
			}


			// then add the items
			mapFiles.entrySet().stream().forEach(me -> {
				final DefaultMutableTreeNode parDir = mapFolders.get(me.getKey());
				me.getValue().forEach(n -> parDir.add(n));
			});
		} catch (InvalidFormatException ex) {
			IOUtils.closeQuietly(opc);
			throw new TreeModelLoadException("Error in opening '"+((File)source).getPath()+"'");
		}

	
	}

	
	private class OPCEntry implements TreeModelEntry {
		final PackagePart packagePart;
		final DefaultMutableTreeNode treeNode;
		File oleFile;
		
		OPCEntry(final PackagePart packagePart, final DefaultMutableTreeNode treeNode) {
			this.packagePart = packagePart;
			this.treeNode = treeNode;
		}
		
		@Override
		public String toString() {
			return escapeString(packagePart.getPartName().getName().replaceAll(".*/", ""));
		}
		
		@Override
		public void activate() {
			treeObservable.setBinarySource(() -> getData());
			treeObservable.setStructuredSource(null);
			treeObservable.notifyObservers();
		}
		
		private ByteArrayEditableData getData() throws IOException, TreeModelLoadException {
			if (packagePart instanceof PackagePropertiesPart) {
				return new ByteArrayEditableData("Property parts can't be exported.".getBytes(LocaleUtil.CHARSET_1252));
			}
			
			FileMagic fm;
			try (InputStream is = FileMagic.prepareToCheckMagic(packagePart.getInputStream())) {
				fm = FileMagic.valueOf(is);
				if (fm == FileMagic.OLE2 && oleFile == null) {
					oleFile = copyToTempFile(is);
					TreeModelPOIFSSource poifsNode = new TreeModelPOIFSSource(treeNode, treeObservable);
					poifsNode.load(oleFile);
				}
				final ByteArrayEditableData data;
				if (oleFile == null) {
					data = new ByteArrayEditableData();
					data.loadFromStream(is);
				} else {
					data = new ByteArrayEditableData("OLE data".getBytes());
				}
				return data;
			}
			
		}

		private File copyToTempFile(InputStream is) throws IOException {
			String partName = packagePart.getPartName().getName();
			partName = partName.substring(partName.lastIndexOf('/')+1);
			final int idx = partName.lastIndexOf('.');
			final String prefix = ((idx == -1) ? partName : partName.substring(0, idx)) + "-";
			final String suffix = (idx == -1 || idx == partName.length()-1) ? "" : partName.substring(idx);

			final File oleFile = TempFile.createTempFile(prefix, suffix);
			try (FileOutputStream fos = new FileOutputStream(oleFile)) {
				IOUtils.copy(is, fos);
			}
			oleFile.deleteOnExit();
			return oleFile;
		}
		
		@Override
		public void close() throws IOException {
			
		}
	}
	
	private class OPCDirEntry implements TreeModelEntry {
		final String path;
		@SuppressWarnings("unused")
		final DefaultMutableTreeNode treeNode;
		
		OPCDirEntry(final String path, final DefaultMutableTreeNode treeNode) {
			this.path = path;
			this.treeNode = treeNode;
		}

		@Override
		public String toString() {
			return escapeString(path.substring(path.lastIndexOf('/')+1));
		}

		@Override
		public void activate() {
		}

		@Override
		public void close() throws IOException {
		}
		
		public String getPath() {
			return path;
		}
	}
	
	private class OPCRootEntry extends OPCDirEntry {
		final OPCPackage opcPackage;
		OPCRootEntry(final OPCPackage opcPackage, final DefaultMutableTreeNode treeNode) {
			super("/", treeNode);
			this.opcPackage = opcPackage;
		}

		@Override
		public void close() throws IOException {
			opcPackage.revert();
		}

		@Override
		public String toString() {
			return "opc";
		}
	}


}
