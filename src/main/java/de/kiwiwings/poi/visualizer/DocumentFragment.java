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

package de.kiwiwings.poi.visualizer;

import de.kiwiwings.poi.visualizer.treemodel.TreeModelLoadException;
import org.apache.commons.lang3.StringUtils;
import org.exbin.utils.binary_data.ByteArrayEditableData;

import javax.json.*;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.IOException;
import java.io.StringReader;
import java.util.Map.Entry;

public class DocumentFragment {
    public enum SourceType {
        empty, octet, text_xml, text_plain, image_jpeg, image_png
    }

    private BinarySource binarySource;
    private ByteArrayEditableData cachedBinary;
    private String fileName;
    private String properties;
    private SourceType sourceType;

    private PropertyChangeSupport mPcs = new PropertyChangeSupport(this);

    public void
    addPropertyChangeListener(PropertyChangeListener listener) {
        mPcs.addPropertyChangeListener(listener);
    }

    public void
    removePropertyChangeListener(PropertyChangeListener listener) {
        mPcs.removePropertyChangeListener(listener);
    }

    public void notifyListeners() {
        mPcs.firePropertyChange("DocumentFragment", null, null);
    }

    public BinarySource getBinarySource() {
        return () -> getCachedBinary();
    }

    public void setBinarySource(final BinarySource binarySource) {
        this.binarySource = binarySource;
        this.cachedBinary = null;
    }

    public SourceType getSourceType() {
        return sourceType;
    }

    public void setSourceType(final SourceType sourceType) {
        this.sourceType = sourceType;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(final String fileName) {
        this.fileName = fileName;
    }

    public String getProperties() {
        return properties;
    }

    public void setProperties(final String properties) {
        this.properties = properties;
    }

    public void mergeProperties(final String properties) {
        if (StringUtils.isEmpty(properties)) {
            return;
        }

        if (StringUtils.isEmpty(getProperties())) {
            setProperties(properties);
            return;
        }

        final JsonReaderFactory fact = Json.createReaderFactory(null);
        final JsonReader r1 = fact.createReader(new StringReader(getProperties()));
        final JsonObjectBuilder jbf = Json.createObjectBuilder(r1.readObject());

        final JsonReader r2 = fact.createReader(new StringReader(getProperties()));
        final JsonObject obj2 = r2.readObject();

        for (Entry<String, JsonValue> jv : obj2.entrySet()) {
            jbf.add(jv.getKey(), jv.getValue());
        }

        setProperties(jbf.build().toString());
    }


    private ByteArrayEditableData getCachedBinary() throws IOException, TreeModelLoadException {
        if (cachedBinary == null && binarySource != null) {
            cachedBinary = binarySource.getBinaryData();
        }
        return cachedBinary;
    }
}
