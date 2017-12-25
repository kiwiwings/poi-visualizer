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

package de.kiwiwings.poi.visualizer.treemodel.hpsf;

import java.io.IOException;
import java.io.OutputStream;

import javax.json.Json;
import javax.json.JsonObjectBuilder;
import javax.swing.tree.DefaultMutableTreeNode;

import org.apache.poi.hpsf.HPSFException;
import org.apache.poi.hpsf.Property;
import org.apache.poi.hpsf.PropertySet;
import org.apache.poi.hpsf.Thumbnail;
import org.apache.poi.hpsf.Variant;
import org.apache.poi.hpsf.VariantSpy;
import org.apache.poi.hpsf.VariantSupport;
import org.apache.poi.hpsf.wellknown.PropertyIDMap;
import org.exbin.utils.binary_data.ByteArrayEditableData;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import de.kiwiwings.poi.visualizer.treemodel.TreeModelEntry;
import de.kiwiwings.poi.visualizer.treemodel.TreeObservable;
import de.kiwiwings.poi.visualizer.treemodel.TreeObservable.SourceType;

@Component(value="HPSFProperty")
@Scope("prototype")
public class HPSFProperty implements TreeModelEntry {

	private final Property property;
	@SuppressWarnings("unused")
	private final DefaultMutableTreeNode treeNode;
	PropertySet propertySet;

	@Autowired
	TreeObservable treeObservable;

	
	public HPSFProperty(final Property property, final DefaultMutableTreeNode treeNode) {
		this.property = property;
		this.treeNode = treeNode;
	}

	public void setPropertySet(PropertySet propertySet) {
		this.propertySet = propertySet;
	}

	@Override
	public String toString() {
		final PropertyIDMap idMap = (propertySet == null) ? null : propertySet.getPropertySetIDMap();
		final long id = property.getID();
		final String name = (idMap == null) ? null : idMap.get(id);
		return name == null ? (id==1 ? "PID_CODEPAGE" : "property-"+id) : name;
	}

	
	@Override
	public void close() throws IOException {
	}

	@Override
	public void activate() {
		if (property.getType() == Variant.VT_CF) {
			final Thumbnail thumb = new Thumbnail((byte[])property.getValue());
			long cfTag;
			try {
				cfTag = thumb.getClipboardFormat();
			} catch (HPSFException e) {
				cfTag = -1;
			}
			final String ext;
			switch ((int)cfTag) {
			case Thumbnail.CF_BITMAP:
				ext = ".bmp";
				break;
			case Thumbnail.CF_DIB:
				ext = ".dib";
				break;
			case Thumbnail.CF_ENHMETAFILE:
				ext = ".emf";
				break;
			case Thumbnail.CF_METAFILEPICT:
				ext = ".wmf";
				break;
			default:
				ext = ".bin";
				break;
			}
			treeObservable.setSourceType(SourceType.octet);
			treeObservable.setFileName("thumbnail"+ext);
		} else {
			treeObservable.setBinarySource(() -> new ByteArrayEditableData());
			treeObservable.setSourceType(SourceType.empty);
			treeObservable.setFileName(null);
		}
		treeObservable.setBinarySource(() -> getData());
		treeObservable.setProperties(getProperties());
	}

	private ByteArrayEditableData getData() throws IOException {
		final ByteArrayEditableData data = new ByteArrayEditableData();
		if (property.getType() == Variant.VT_CF) {
			try (final OutputStream os = data.getDataOutputStream()) {
				Thumbnail thumb = new Thumbnail((byte[])property.getValue());
				os.write(
					(thumb.getClipboardFormat() == Thumbnail.CF_METAFILEPICT)
					? thumb.getThumbnailAsWMF()
					: thumb.getThumbnail()
				);
			} catch (HPSFException e) {
				throw new IOException(e);
			}
		}
		return data;
	}
	
	private String getProperties() {
		final JsonObjectBuilder jsonBuilder = Json.createObjectBuilder();
		final long id = property.getID();
		jsonBuilder.add("ID", id);
		jsonBuilder.add("Type", property.getType());
		jsonBuilder.add("VariantName", id == 0 ? "dictionary" : Variant.getVariantName(property.getType()));
		switch ((int)property.getType()) {
		case Variant.VT_CF:
			jsonBuilder.add("Value", "<see binary/save entry>");
			break;
        case Variant.VT_ARRAY | Variant.VT_I2:
        case Variant.VT_ARRAY | Variant.VT_I4:
        case Variant.VT_ARRAY | Variant.VT_R4:
        case Variant.VT_ARRAY | Variant.VT_R8:
        case Variant.VT_ARRAY | Variant.VT_CY:
        case Variant.VT_ARRAY | Variant.VT_DATE:
        case Variant.VT_ARRAY | Variant.VT_BSTR:
        case Variant.VT_ARRAY | Variant.VT_ERROR:
        case Variant.VT_ARRAY | Variant.VT_BOOL:
        case Variant.VT_ARRAY | Variant.VT_VARIANT:
        case Variant.VT_ARRAY | Variant.VT_DECIMAL:
        case Variant.VT_ARRAY | Variant.VT_I1:
        case Variant.VT_ARRAY | Variant.VT_UI1:
        case Variant.VT_ARRAY | Variant.VT_UI2:
        case Variant.VT_ARRAY | Variant.VT_UI4:
        case Variant.VT_ARRAY | Variant.VT_INT:
        case Variant.VT_ARRAY | Variant.VT_UINT:
			jsonBuilder.add("Value", VariantSpy.arrayToJson(property, propertySet));
			break;
        case Variant.VT_VECTOR | Variant.VT_I2:
        case Variant.VT_VECTOR | Variant.VT_I4:
        case Variant.VT_VECTOR | Variant.VT_R4:
        case Variant.VT_VECTOR | Variant.VT_R8:
        case Variant.VT_VECTOR | Variant.VT_CY:
        case Variant.VT_VECTOR | Variant.VT_DATE:
        case Variant.VT_VECTOR | Variant.VT_BSTR:
        case Variant.VT_VECTOR | Variant.VT_ERROR:
        case Variant.VT_VECTOR | Variant.VT_BOOL:
        case Variant.VT_VECTOR | Variant.VT_VARIANT:
        case Variant.VT_VECTOR | Variant.VT_I1:
        case Variant.VT_VECTOR | Variant.VT_UI1:
        case Variant.VT_VECTOR | Variant.VT_UI2:
        case Variant.VT_VECTOR | Variant.VT_UI4:
        case Variant.VT_VECTOR | Variant.VT_I8:
        case Variant.VT_VECTOR | Variant.VT_UI8:
        case Variant.VT_VECTOR | Variant.VT_LPSTR:
        case Variant.VT_VECTOR | Variant.VT_LPWSTR:
        case Variant.VT_VECTOR | Variant.VT_FILETIME:
        case Variant.VT_VECTOR | Variant.VT_CF:
        case Variant.VT_VECTOR | Variant.VT_CLSID:
			jsonBuilder.add("Value", VariantSpy.vectorToJson(property, propertySet));
			break;
		default:
			if (property.getValue() == null) {
				jsonBuilder.addNull("Value");
			} else {
				jsonBuilder.add("Value", property.getValue().toString());
			}
			break;
		}
		return jsonBuilder.build().toString();
	}
}
