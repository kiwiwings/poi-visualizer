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

import de.kiwiwings.poi.visualizer.DocumentFragment;
import de.kiwiwings.poi.visualizer.treemodel.TreeModelEntry;
import de.kiwiwings.poi.visualizer.DocumentFragment.SourceType;
import javafx.scene.control.TreeItem;
import org.apache.poi.hpsf.*;
import org.apache.poi.hpsf.wellknown.PropertyIDMap;
import org.apache.poi.util.LittleEndianByteArrayInputStream;
import org.exbin.utils.binary_data.ByteArrayEditableData;

import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonArrayBuilder;
import javax.json.JsonObjectBuilder;
import java.io.IOException;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;

public class HPSFProperty implements TreeModelEntry {

	private final Property property;
	@SuppressWarnings("unused")
	private final TreeItem<TreeModelEntry> treeNode;
	PropertySet propertySet;

	public HPSFProperty(final Property property, final TreeItem<TreeModelEntry> treeNode) {
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
	public void activate(final DocumentFragment fragment) {
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
			fragment.setSourceType(SourceType.octet);
			fragment.setFileName("thumbnail"+ext);
		} else {
			fragment.setBinarySource(() -> new ByteArrayEditableData());
			fragment.setSourceType(SourceType.empty);
			fragment.setFileName(null);
		}
		fragment.setBinarySource(() -> getData());
		fragment.setProperties(getProperties(fragment));
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

	private String getProperties(final DocumentFragment fragment) {
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
        case Variant.VT_ARRAY | Variant.VT_UINT: {
			Array arr = new Array();
			LittleEndianByteArrayInputStream leis = new LittleEndianByteArrayInputStream((byte[]) property.getValue());
			arr.read(leis);
			jsonBuilder.add("Value", vecArrToJson(arr.getValues(), property, propertySet));
			break;
		}
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
        case Variant.VT_VECTOR | Variant.VT_CLSID: {
			Vector vec = new Vector((short) (property.getType() & 0x0FFF));
			LittleEndianByteArrayInputStream leis = new LittleEndianByteArrayInputStream((byte[]) property.getValue());
			vec.read(leis);
			jsonBuilder.add("Value", vecArrToJson(vec.getValues(), property, propertySet));
			break;
		}
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

	private static JsonArray vecArrToJson(TypedPropertyValue[] values, Property property, PropertySet propertySet) {
		final JsonArrayBuilder jsonBuilder = Json.createArrayBuilder();
		for (final TypedPropertyValue v : values) {
			switch ((int)property.getType() & 0x0FFF) {
				case Variant.VT_I2:
				case Variant.VT_I4:
				case Variant.VT_R4:
				case Variant.VT_R8:
				case Variant.VT_I1:
				case Variant.VT_UI1:
				case Variant.VT_UI2:
				case Variant.VT_UI4:
				case Variant.VT_I8:
				case Variant.VT_UI8:
				case Variant.VT_ERROR:
					jsonBuilder.add(v.getValue().toString());
					break;

				// simplified/combined instanceof-check because of VT_VARIANT
				case Variant.VT_BSTR:
				case Variant.VT_LPSTR:
				case Variant.VT_BOOL:
				case Variant.VT_LPWSTR:
				case Variant.VT_FILETIME:
				case Variant.VT_CLSID:
				case Variant.VT_VARIANT:
					final Object obj = v.getValue();
					if (obj instanceof CodePageString) {
						try {
							jsonBuilder.add(((CodePageString)obj).getJavaValue(propertySet.getFirstSection().getCodepage()));
						} catch (UnsupportedEncodingException e) {
							jsonBuilder.add(e.getMessage());
						}
					} else if (obj instanceof VariantBool) {
						jsonBuilder.add(((VariantBool)obj).getValue());
					} else if (obj instanceof UnicodeString) {
						jsonBuilder.add(((UnicodeString)v.getValue()).toJavaString());
					} else if (obj instanceof Filetime) {
						jsonBuilder.add(((Filetime)v.getValue()).getJavaValue().toString());
					} else if (obj instanceof ClassID) {
						jsonBuilder.add(((ClassID)v.getValue()).toString());
					} else {
						jsonBuilder.add(v.getValue().toString());
					}
					break;


				default:
				case Variant.VT_CF:
				case Variant.VT_DATE:
				case Variant.VT_CY:
					jsonBuilder.add("<unsupported>");
					break;
			}
		}

		return jsonBuilder.build();
	}

}
