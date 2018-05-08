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

import de.kiwiwings.poi.visualizer.BinarySource;
import org.apache.commons.lang3.StringUtils;
import org.exbin.utils.binary_data.ByteArrayEditableData;

import javax.json.*;
import java.io.IOException;
import java.io.StringReader;
import java.util.Map.Entry;
import java.util.Observable;
import java.util.Observer;

public class TreeObservable extends Observable {
    public enum SourceType {
        empty, octet, text_xml, text_plain, image_jpeg, image_png
    }

    public enum SourceOrigin {
        MENU_EDIT_APPLY;
    }

    private TreeObservable() {
    }

    private static class SingletonHelper {
        private static final TreeObservable INSTANCE = new TreeObservable();
    }

    public static TreeObservable getInstance() {
        return SingletonHelper.INSTANCE;
    }


    private BinarySource binarySource;
    private ByteArrayEditableData cachedBinary;
    private String fileName;
    private String properties;
    private SourceType sourceType;

    public BinarySource getBinarySource() {
        return () -> getCachedBinary();
    }

    public void setBinarySource(final BinarySource binarySource) {
        this.binarySource = binarySource;
        this.cachedBinary = null;
        setChanged();
    }

    public SourceType getSourceType() {
        return sourceType;
    }

    public void setSourceType(final SourceType sourceType) {
        this.sourceType = sourceType;
        setChanged();
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(final String fileName) {
        this.fileName = fileName;
        setChanged();
    }

    public String getProperties() {
        return properties;
    }

    public void setProperties(final String properties) {
        this.properties = properties;
        setChanged();
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


    public void setTreeEntryListener(final TreeModelEntry entry) {
        deleteObserver(new Observer() {
            @Override
            public void update(final Observable o, final Object arg) {
            }

            @Override
            public boolean equals(final Object o) {
                return o instanceof TreeModelEntry;
            }
        });

        if (entry != null) {
            addObserver(entry);
        }
    }

    private ByteArrayEditableData getCachedBinary() throws IOException, TreeModelLoadException {
        if (cachedBinary == null && binarySource != null) {
            cachedBinary = binarySource.getBinaryData();
        }
        return cachedBinary;
    }
}
