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

package de.kiwiwings.poi.visualizer.treemodel.generic;

import de.kiwiwings.poi.visualizer.DocumentFragment;
import de.kiwiwings.poi.visualizer.DocumentFragment.SourceType;
import de.kiwiwings.poi.visualizer.treemodel.TreeModelEntry;
import javafx.scene.control.TreeItem;
import org.apache.poi.common.usermodel.GenericRecord;
import org.apache.poi.util.GenericRecordJsonWriter;
import org.exbin.utils.binary_data.ByteArrayEditableData;

import java.io.IOException;

public class GenericRecordEntry implements TreeModelEntry {
    private final GenericRecord record;
    private final TreeItem<TreeModelEntry> treeNode;
    private String name = null;
    private boolean wasActivated = false;

    public GenericRecordEntry(final GenericRecord record, final TreeItem<TreeModelEntry> treeNode) {
        this.record = record;
        this.treeNode = treeNode;
    }

    public void setName(String name) {
        this.name = name;
    }

    public GenericRecord getRecord() {
        return record;
    }

    @Override
    public String toString() {
        if (name != null) {
            return name;
        }
        Enum type = record.getGenericRecordType();
        return type != null ? type.name() : record.getClass().getSimpleName();
    }

    @Override
    public void activate(DocumentFragment fragment) {
        final StringBuilder sb = new StringBuilder();
        try (MyGenericWriter w = new MyGenericWriter(sb)) {
            w.setWithComments(false);
            w.write(record);
            fragment.setProperties(sb.toString());
        } catch (IOException e) {
            fragment.setProperties("{}");
        }
        wasActivated = true;
        fragment.setBinarySource(ByteArrayEditableData::new);
        fragment.setSourceType(SourceType.text_plain);
        fragment.setFileName(toString()+".json");
    }

    @Override
    public void close() throws IOException {

    }

    private class MyGenericWriter extends GenericRecordJsonWriter {
        MyGenericWriter(Appendable buffer) {
            super(buffer);
        }

        @Override
        protected boolean writeChildren(GenericRecord record, boolean hasProperties) {
            printGenericRecord(null, record);
            return false;
        }

        @Override
        protected boolean printGenericRecord(String name, Object o) {
            if (!wasActivated) {
                GenericRecord newParent = (GenericRecord) o;
                if (name != null) {
                    final TreeItem<TreeModelEntry> childNode = new TreeItem<>();
                    GenericRecordEntry ge = new GenericRecordEntry(newParent, childNode);
                    ge.setName(name);
                    childNode.setValue(ge);
                    treeNode.getChildren().add(childNode);
                    GenericTreeModel.loadRecords(childNode, newParent);
                } else {
                    GenericTreeModel.loadRecords(treeNode, newParent);
                }
            }
            return false;
        }

        @Override
        protected boolean printBytes(String name, Object o) {
            if (!wasActivated) {
                final TreeItem<TreeModelEntry> childNode = new TreeItem<>();
                GenericNamedEntry gne = new GenericNamedEntry(name, (docFrag) -> {
                    docFrag.setBinarySource(() -> new ByteArrayEditableData((byte[]) o));
                    docFrag.setSourceType(SourceType.octet);
                });
                childNode.setValue(gne);
                treeNode.getChildren().add(childNode);
            }
            return false;
        }
    }

}
