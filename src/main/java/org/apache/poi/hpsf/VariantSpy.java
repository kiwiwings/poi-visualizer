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

package org.apache.poi.hpsf;

import java.io.UnsupportedEncodingException;

import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonArrayBuilder;

import org.apache.poi.util.LittleEndianByteArrayInputStream;

/**
 * Helper class to access package-only visible classes and methods
 */
public class VariantSpy {
	public static JsonArray vectorToJson(Property property, PropertySet propertySet) {
        Vector vec = new Vector( (short) ( property.getType() & 0x0FFF ) );
        LittleEndianByteArrayInputStream leis = new LittleEndianByteArrayInputStream((byte[])property.getValue());
        vec.read(leis);
        return vecArrToJson(vec.getValues(), property, propertySet);
	}

	public static JsonArray arrayToJson(Property property, PropertySet propertySet) {
		Array arr = new Array();
        LittleEndianByteArrayInputStream leis = new LittleEndianByteArrayInputStream((byte[])property.getValue());
        arr.read(leis);
        return vecArrToJson(arr.getValues(), property, propertySet);
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
