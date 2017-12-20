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

package de.kiwiwings.poi.visualizer.treemodel;

import java.io.Closeable;
import java.util.Observable;
import java.util.Observer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public interface TreeModelEntry extends Closeable, Observer {
	// replace control characters
	static final Pattern CTRL_CHR = Pattern.compile("\\p{Cc}"); 

	/**
	 * Escape string suitable for display in a tree
	 * @param string the raw string
	 * @return the escaped string
	 */
	default String escapeString(final String string) {
		final Matcher match = CTRL_CHR.matcher(string);
		final StringBuffer sb = new StringBuffer();
		while (match.find()) {
			int cp = match.group().codePointAt(0);
			match.appendReplacement(sb, String.format("\\\\%02X", cp));
		}
		match.appendTail(sb);
		return sb.toString();
	}
	
	default void update(Observable o, Object arg) {}
	
	String toString();
	
	/**
	 * Entry is clicked/activate - don't update the observable(s)
	 */
	void activate();
}
