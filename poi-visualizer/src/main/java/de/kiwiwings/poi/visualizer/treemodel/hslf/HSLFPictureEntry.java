package de.kiwiwings.poi.visualizer.treemodel.hslf;

import java.io.IOException;
import java.io.OutputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.json.Json;
import javax.json.JsonObjectBuilder;
import javax.swing.tree.DefaultMutableTreeNode;

import org.apache.poi.hslf.record.Record;
import org.apache.poi.hslf.usermodel.HSLFPictureData;
import org.exbin.utils.binary_data.ByteArrayEditableData;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import de.kiwiwings.poi.visualizer.treemodel.TreeModelEntry;
import de.kiwiwings.poi.visualizer.treemodel.TreeObservable;
import de.kiwiwings.poi.visualizer.treemodel.TreeObservable.SourceType;

@Component(value="HSLFPictureEntry")
@Scope("prototype")
public class HSLFPictureEntry implements TreeModelEntry {

	private final HSLFPictureData picture;
	private final DefaultMutableTreeNode treeNode;

	@Autowired
	TreeObservable treeObservable;

	
	public HSLFPictureEntry(final HSLFPictureData picture, final DefaultMutableTreeNode treeNode) {
		this.picture = picture;
		this.treeNode = treeNode;
	}


	@Override
	public String toString() {
		return "picture_"+picture.getIndex()+"."+picture.getType().extension;
	}

	
	@Override
	public void close() throws IOException {
	}

	@Override
	public void activate() {
		treeObservable.setBinarySource(() -> getData());
		treeObservable.setSourceType(SourceType.octet);
		treeObservable.setFileName(toString());
		treeObservable.setProperties(reflectProperties());
	}

	private ByteArrayEditableData getData() throws IOException {
		return new ByteArrayEditableData(picture.getData());
	}

	private String reflectProperties() {
		final Pattern getter = Pattern.compile("(?:is|get)(.*)"); 
		final JsonObjectBuilder jsonBuilder = Json.createObjectBuilder();

		for (Method m : picture.getClass().getDeclaredMethods()) {
			final Matcher match = getter.matcher(m.getName());
			if (match.matches() && m.getParameterCount() == 0 && !byte[].class.isAssignableFrom(m.getReturnType())) {
				final String propName = match.group(1);
				String propVal;
				try {
					m.setAccessible(true);
					Object obj = m.invoke(picture);
					propVal = (obj == null) ? "" : obj.toString();
				} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
					propVal = e.getMessage();
				}
				jsonBuilder.add(propName, propVal);
			}
		}
		return jsonBuilder.build().toString();
	}
}
