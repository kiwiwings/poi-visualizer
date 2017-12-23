package de.kiwiwings.poi.visualizer.treemodel.hslf;

import java.io.IOException;

import javax.swing.tree.DefaultMutableTreeNode;

import org.apache.poi.hslf.record.RecordContainer;
import org.exbin.utils.binary_data.ByteArrayEditableData;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import de.kiwiwings.poi.visualizer.treemodel.TreeObservable.SourceType;

@Component(value="HSLFDirEntry")
@Scope("prototype")
public class HSLFDirEntry extends HSLFEntry {
	
	public HSLFDirEntry(final RecordContainer path, final DefaultMutableTreeNode treeNode) {
		super(path, treeNode);
	}

	@Override
	public void close() throws IOException {
	}

	@Override
	public void activate() {
		treeObservable.setBinarySource(() -> new ByteArrayEditableData());
		treeObservable.setSourceType(SourceType.empty);
		treeObservable.setProperties(null);
	}

}
