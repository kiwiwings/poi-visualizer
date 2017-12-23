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

package de.kiwiwings.poi.visualizer.treemodel.hslf;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.json.Json;
import javax.json.JsonObjectBuilder;

public class HSLFProperties {
	private static final Pattern getter = Pattern.compile("(?:is|get)(.*)");

	public static String reflectProperties(Object obj) {
		final JsonObjectBuilder jsonBuilder = Json.createObjectBuilder();

		for (Method m : getGetter(new ArrayList<>(), obj.getClass())) {
			if (useReturnType(m)) {
				Matcher match = getter.matcher(m.getName());
				match.matches();
				final String propName = match.group(1);
				String propVal;
				try {
					m.setAccessible(true);
					Object retVal = m.invoke(obj);
					propVal = (retVal == null) ? "" : retVal.toString();
				} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
					propVal = e.getMessage();
				}
				jsonBuilder.add(propName, propVal);
			}
		}
		return jsonBuilder.build().toString();
	}

	private static boolean useReturnType(Method m) {
		final Class<?> retType = m.getReturnType();
		return !(
			retType.isArray() ||
			Iterator.class.isAssignableFrom(retType) ||
			(Collection.class.isAssignableFrom(retType) && !m.getName().contains("getEscherProperties"))
		);
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
