package de.kiwiwings.poi.visualizer.treemodel.hslf;

import java.io.IOException;
import java.util.List;

import javax.swing.tree.DefaultMutableTreeNode;

import org.apache.poi.hslf.record.Record;
import org.apache.poi.hslf.record.RecordContainer;
import org.apache.poi.hslf.usermodel.HSLFPictureData;
import org.apache.poi.hslf.usermodel.HSLFSlideShow;
import org.apache.poi.poifs.filesystem.DirectoryNode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import de.kiwiwings.poi.visualizer.treemodel.TreeModelEntry;
import de.kiwiwings.poi.visualizer.treemodel.TreeModelLoadException;
import de.kiwiwings.poi.visualizer.treemodel.TreeModelSource;
import de.kiwiwings.poi.visualizer.treemodel.ole.OLEEntry;

@Component
@Scope("prototype")
public class HSLFTreeModel implements TreeModelSource {

	private final DefaultMutableTreeNode parent;
	
	private HSLFSlideShow ppt;

	@Autowired
	private ApplicationContext appContext;

	public HSLFTreeModel(final DefaultMutableTreeNode parent) {
		this.parent = parent;
	}
	
	@Override
	public void load(Object source) throws TreeModelLoadException {
		try {
			ppt = new HSLFSlideShow((DirectoryNode)source);
			final DefaultMutableTreeNode slNode = getNamedTreeNode(HSLFSlideShow.POWERPOINT_DOCUMENT);
			HSLFRootEntry rootNode = appContext.getBean(HSLFRootEntry.class, ppt, slNode);
			slNode.setUserObject(rootNode);
			loadRecords(slNode,ppt.getSlideShowImpl().getRecords());
			final DefaultMutableTreeNode picNode = getNamedTreeNode("Pictures");
			loadPictures(picNode);
		} catch (IOException e) {
			throw new TreeModelLoadException("Can't load HSLF slideshow",e);
		}
	}
	
	private void loadRecords(final DefaultMutableTreeNode parentNode, final Record[] records) {
		for (final Record r : records) {
			final String qualifier = (r instanceof RecordContainer) ? "HSLFDirEntry" : "HSLFEntry";
			final DefaultMutableTreeNode childNode = new DefaultMutableTreeNode();
			final TreeModelEntry dirEntry = (TreeModelEntry)appContext.getBean(qualifier, r, childNode);
			childNode.setUserObject(dirEntry);
			parentNode.add(childNode);
			
			if (r instanceof RecordContainer) {
				loadRecords(childNode, ((RecordContainer)r).getChildRecords());
			}
		}
	}
	
	private void loadPictures(final DefaultMutableTreeNode parentNode) {
		final DefaultMutableTreeNode slNode = getNamedTreeNode("Pictures");
		for (HSLFPictureData p : ppt.getPictureData()) {
			final DefaultMutableTreeNode childNode = new DefaultMutableTreeNode();
			final HSLFPictureEntry pic = appContext.getBean(HSLFPictureEntry.class, p, childNode);
			childNode.setUserObject(pic);
			slNode.add(childNode);
		}
		
	}
	
	private DefaultMutableTreeNode getNamedTreeNode(String name) {
		final int cnt = parent.getChildCount();
		for (int i=0; i<cnt; i++) {
			final DefaultMutableTreeNode c = (DefaultMutableTreeNode)parent.getChildAt(i);
			final TreeModelEntry poifsEntry = (TreeModelEntry)c.getUserObject();
			if (name.equals(poifsEntry.toString())) {
				return c;
			}
		}
		return null;
	}
}
