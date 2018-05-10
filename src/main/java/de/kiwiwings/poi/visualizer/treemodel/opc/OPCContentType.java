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
import org.apache.poi.util.IOUtils;
import org.exbin.utils.binary_data.ByteArrayEditableData;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

public class OPCContentType implements TreeModelEntry {
    private final byte[] data;
    final TreeItem<TreeModelEntry> treeNode;
    final TreeModelEntry surrugateEntry;

    OPCContentType(File source, final TreeItem<TreeModelEntry> treeNode) throws IOException {
        try (final ZipFile zipFile = new ZipFile((File)source)) {
            final ZipEntry ze = zipFile.getEntry("[Content_Types].xml");
            try (InputStream is = zipFile.getInputStream(ze)) {
                data = IOUtils.toByteArray(is);
            }
        }

        this.treeNode = treeNode;
        surrugateEntry = treeNode.getValue();
    }

    @Override
    public String toString() {
        final String name = "[Content_Types].xml";
        return (treeNode.getParent() == null || surrugateEntry == null)
                ? name : surrugateEntry+" ("+name+")";
    }

    @Override
    public void activate(final DocumentFragment fragment) {
        final String fileName = toString();
        fragment.setFileName(fileName);
        fragment.setSourceType(DocumentFragment.SourceType.text_xml);
        fragment.setProperties(null);
        fragment.setBinarySource(() -> new ByteArrayEditableData(this.data));
    }

    @Override
    public void close() throws IOException {

    }
}
