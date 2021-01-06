/* ====================================================================
   Copyright 2017 Andreas Beeker (kiwiwings@apache.org)

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
==================================================================== */

package de.kiwiwings.poi.visualizer.treemodel;

import javafx.scene.control.TreeItem;
import org.apache.poi.ddf.EscherComplexProperty;
import org.apache.poi.ddf.EscherProperties;
import org.apache.poi.util.StringUtil;

import javax.json.Json;
import javax.json.JsonArrayBuilder;
import javax.json.JsonObjectBuilder;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class TreeModelUtils {
	// replace control characters
	private static final Pattern CTRL_CHR = Pattern.compile("\\p{Cc}");
	private static final Pattern getter = Pattern.compile("(?:is|get)(.*)");

	public static TreeItem<TreeModelEntry> getNamedTreeNode(final TreeItem<TreeModelEntry> parent, final String... names) {
		final List<String> escNames = Arrays.stream(names).map(TreeModelUtils::escapeString).collect(Collectors.toList());
		return parent.getChildren().stream().filter(c -> escNames.stream().anyMatch(n -> c.getValue().toString().contains(n))).findFirst().orElse(null);
	}

	/**
	 * Escape string suitable for display in a tree
	 * @param string the raw string
	 * @return the escaped string
	 */
	public static String escapeString(final String string) {
		final Matcher match = CTRL_CHR.matcher(string);
		final StringBuffer sb = new StringBuffer();
		while (match.find()) {
			int cp = match.group().codePointAt(0);
			match.appendReplacement(sb, String.format("\\\\%02X", cp));
		}
		match.appendTail(sb);
		return sb.toString();
	}

	public static String reflectProperties(Object obj) {
		final JsonObjectBuilder jsonBuilder = Json.createObjectBuilder();

		for (Method m : getGetter(new ArrayList<>(), obj.getClass())) {
			if (useReturnType(m)) {
				Matcher match = getter.matcher(m.getName());
				match.matches();
				final String propName = match.group(1);
				Object retVal;
				try {
					m.setAccessible(true);
					retVal = m.invoke(obj);
				} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
					retVal = e.getMessage();
				}

				if (retVal == null) {
					jsonBuilder.addNull(propName);
				} else if (retVal instanceof Collection<?>) {
					final JsonArrayBuilder arrBuilder = Json.createArrayBuilder();
					for (Object o : ((Collection<?>)retVal)) {
						if (o == null) {
							arrBuilder.addNull();
						} else if (o instanceof EscherComplexProperty) {
							final EscherComplexProperty ep = (EscherComplexProperty)o;
							String val = ep.toString();
							if (ep.getPropertyNumber() == EscherProperties.GROUPSHAPE__SHAPENAME) {
								final byte[] cd = ep.getComplexData();
								final String name = StringUtil.getFromUnicodeLE0Terminated(cd, 0, cd.length / 2);
								val = val.replaceFirst(", data: ", ", name: '" + name + "', data: ");
							}
							arrBuilder.add(val);
						} else {
							arrBuilder.add(o.toString());
						}
					}
					jsonBuilder.add(propName, arrBuilder.build());
				} else if (retVal instanceof Double) {
					jsonBuilder.add(propName, ((Double)retVal).doubleValue());
				} else if (retVal instanceof Boolean) {
					jsonBuilder.add(propName, ((Boolean)retVal).booleanValue());
				} else if (retVal instanceof Integer || retVal instanceof Short || retVal instanceof Byte) {
					jsonBuilder.add(propName, ((Number)retVal).intValue());
				} else if (retVal instanceof Long) {
					jsonBuilder.add(propName, ((Long)retVal).longValue());
				} else if (retVal instanceof BigDecimal) {
					jsonBuilder.add(propName, (BigDecimal)retVal);
				} else if (retVal instanceof BigInteger) {
					jsonBuilder.add(propName, (BigInteger)retVal);
				} else {
					jsonBuilder.add(propName, retVal.toString());
				}
			}
		}
		return jsonBuilder.build().toString();
	}

	private static boolean useReturnType(Method m) {
		final Class<?> retType = m.getReturnType();
		return m.getName().matches("get(EscherProperties|TabStops)") ||
		(!(
			retType.isArray() ||
			Iterator.class.isAssignableFrom(retType) ||
			(Collection.class.isAssignableFrom(retType))
		) && (
			retType.getPackage() == null ||
			retType.getName().contains("java.lang") ||
			Enum.class.isAssignableFrom(retType)
		));
	}

	private static List<Method> getGetter(List<Method> list, Class<?> clazz) {
		if (clazz == null) {
			return list;
		}
		getGetter(list, clazz.getSuperclass());

		for (Method m : clazz.getDeclaredMethods()) {
			final Matcher match = getter.matcher(m.getName());
			if (match.matches() && m.getParameterCount() == 0) {
				m.setAccessible(true);
				list.add(m);
			}
		}
		return list;
	}
}
