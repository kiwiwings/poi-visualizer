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

import de.kiwiwings.poi.visualizer.treemodel.TreeModelEntry;
import javafx.scene.control.TreeItem;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.openxml4j.opc.OPCPackage;
import org.apache.poi.openxml4j.opc.PackageProperties;
import org.apache.poi.openxml4j.util.Nullable;
import org.apache.poi.util.LocaleUtil;

import javax.json.Json;
import javax.json.JsonObjectBuilder;
import java.io.IOException;
import java.text.DateFormat;
import java.util.Date;

public class OPCRootEntry extends OPCDirEntry {
	private static final DateFormat DATE_FMT =
		DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.SHORT, LocaleUtil.getUserLocale());
	
	final OPCPackage opcPackage;
	
	
	
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
	public void activate() {
		if (surrugateEntry != null) {
			surrugateEntry.activate();
			setProperties();
		} else {
			super.activate();
		}
	}

	@Override
	protected void setProperties() {
		try {
			final PackageProperties props = opcPackage.getPackageProperties();

			final Object[][] values = {
					{ "category", props.getCategoryProperty() },
					{ "contentStatus", props.getContentStatusProperty() },
					{ "contentType", props.getContentTypeProperty() },
					{ "creator", props.getCreatorProperty() },
					{ "description", props.getDescriptionProperty() },
					{ "identifier", props.getIdentifierProperty() },
					{ "keywords", props.getKeywordsProperty() },
					{ "language", props.getLanguageProperty() },
					{ "lastModifiedBy", props.getLastModifiedByProperty() },
					{ "revision", props.getRevisionProperty() },
					{ "subject", props.getSubjectProperty() },
					{ "title", props.getTitleProperty() },
					{ "version", props.getVersionProperty() },
					{ "created", props.getCreatedProperty() },
					{ "modified", props.getModifiedProperty() },
			};
			
			final JsonObjectBuilder jsonBuilder = Json.createObjectBuilder();
			for (Object[] v : values) {
				final Nullable<?> nValue = (Nullable<?>)v[1];
				if (nValue.hasValue()) {
					Object val = nValue.getValue();
					if (val instanceof Date) {
						val = DATE_FMT.format((Date)val);
					}
					jsonBuilder.add((String)v[0], val.toString());
				}
			}
			
			treeObservable.mergeProperties(jsonBuilder.build().toString());
		} catch (InvalidFormatException e) {
			treeObservable.mergeProperties(null);
		}
	}
}