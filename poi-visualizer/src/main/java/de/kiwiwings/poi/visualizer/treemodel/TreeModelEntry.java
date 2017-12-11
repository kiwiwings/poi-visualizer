package de.kiwiwings.poi.visualizer.treemodel;

import java.io.Closeable;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public interface TreeModelEntry extends Closeable {
	// replace control characters
	static final Pattern CTRL_CHR = Pattern.compile("\\p{Cc}"); 

	/**
	 * Escape string suitable for display in a tree
	 * @param string the raw string
	 * @return the escaped string
	 */
	default String escapeString(String string) {
		final Matcher match = CTRL_CHR.matcher(string);
		final StringBuffer sb = new StringBuffer();
		while (match.find()) {
			int cp = match.group().codePointAt(0);
			match.appendReplacement(sb, String.format("\\\\%02X", cp));
		}
		match.appendTail(sb);
		return sb.toString();
	}
	
	
	String toString();
	
	/**
	 * Entry is clicked/activate - update the observable(s)
	 */
	void activate();
}
