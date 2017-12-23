package de.kiwiwings.poi.visualizer.treemodel.hslf;

import java.io.IOException;

import javax.swing.tree.DefaultMutableTreeNode;

import org.apache.poi.hslf.usermodel.HSLFSlideShow;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import de.kiwiwings.poi.visualizer.treemodel.TreeModelEntry;


@Component(value="HSLFRootEntry")
@Scope("prototype")
public class HSLFRootEntry implements TreeModelEntry {
	HSLFSlideShow ppt;
	final DefaultMutableTreeNode treeNode;
	final TreeModelEntry surrugateEntry;
	
	public HSLFRootEntry(HSLFSlideShow ppt, DefaultMutableTreeNode treeNode) {
		this.ppt = ppt;
		this.treeNode = treeNode;
		Object oldUserObject = treeNode.getUserObject();
		surrugateEntry = (oldUserObject instanceof TreeModelEntry) ? (TreeModelEntry)oldUserObject : null;
	}

	@Override
	public String toString() {
		final String name = "SlideShow";
		return (treeNode.getParent() == null || surrugateEntry == null)
				? name : surrugateEntry+" ("+name+")";
	}

	
	@Override
	public void close() throws IOException {
	}

	@Override
	public void activate() {
	}

}
