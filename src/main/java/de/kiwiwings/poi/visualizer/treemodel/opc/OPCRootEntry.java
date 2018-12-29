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

package de.kiwiwings.poi.visualizer.treemodel.opc;

import de.kiwiwings.poi.visualizer.DocumentFragment;
import de.kiwiwings.poi.visualizer.treemodel.TreeModelEntry;
import javafx.scene.control.TreeItem;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.openxml4j.opc.OPCPackage;
import org.apache.poi.openxml4j.opc.PackageProperties;
import org.apache.poi.util.LocaleUtil;

import javax.json.Json;
import javax.json.JsonObjectBuilder;
import java.io.IOException;
import java.text.DateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.Supplier;

public class OPCRootEntry extends OPCDirEntry {
	private static final DateFormat DATE_FMT =
		DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.SHORT, LocaleUtil.getUserLocale());
	
	private final OPCPackage opcPackage;
	
	
	
	OPCRootEntry(final OPCPackage opcPackage, final TreeItem<TreeModelEntry> treeNode) {
		super("/", treeNode);
		this.opcPackage = opcPackage;
	}

	@Override
	public void close() throws IOException {
		opcPackage.revert();
	}

	@Override
	public String toString() {
		final String name = "opc";
		return (treeNode.getParent() == null || surrugateEntry == null)
				? name : surrugateEntry+" ("+name+")";
	}
	
	@Override
	public void activate(final DocumentFragment fragment) {
		if (surrugateEntry != null) {
			surrugateEntry.activate(fragment);
			setProperties(fragment);
		} else {
			super.activate(fragment);
		}
	}

	@Override
	protected void setProperties(final DocumentFragment fragment) {
		try {
			final PackageProperties props = opcPackage.getPackageProperties();
			final Map<String, Supplier<Optional<?>>> values = new HashMap<>();
			values.put("category", props::getCategoryProperty);
			values.put( "contentStatus", props::getContentStatusProperty);
			values.put( "contentType", props::getContentTypeProperty);
			values.put( "creator", props::getCreatorProperty);
			values.put( "description", props::getDescriptionProperty);
			values.put( "identifier", props::getIdentifierProperty);
			values.put( "keywords", props::getKeywordsProperty);
			values.put( "language", props::getLanguageProperty);
			values.put( "lastModifiedBy", props::getLastModifiedByProperty);
			values.put( "revision", props::getRevisionProperty);
			values.put( "subject", props::getSubjectProperty);
			values.put( "title", props::getTitleProperty);
			values.put( "version", props::getVersionProperty);
			values.put( "created", props::getCreatedProperty);
			values.put( "modified", props::getModifiedProperty);


			final JsonObjectBuilder jsonBuilder = Json.createObjectBuilder();
			values.forEach((n,v) -> {
				v.get().ifPresent(p ->
					jsonBuilder.add(n, (p instanceof Date ? DATE_FMT.format((Date)p) : p).toString())
				);
			});

			fragment.mergeProperties(jsonBuilder.build().toString());
		} catch (InvalidFormatException e) {
			fragment.mergeProperties(null);
		}
	}
}