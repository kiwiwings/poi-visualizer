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

import javax.swing.tree.DefaultMutableTreeNode;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import de.kiwiwings.poi.visualizer.treemodel.TreeModelEntry;
import de.kiwiwings.poi.visualizer.treemodel.TreeObservable;
import org.apache.poi.util.IOUtils;
import org.exbin.utils.binary_data.ByteArrayEditableData;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@Component(value="OPCContentType")
@Scope("prototype")
public class OPCContentType implements TreeModelEntry {
    private final byte[] data;
    final DefaultMutableTreeNode treeNode;
    final TreeModelEntry surrugateEntry;

    @Autowired
    TreeObservable treeObservable;

    @Autowired
    private ApplicationContext appContext;

    OPCContentType(File source, final DefaultMutableTreeNode treeNode) throws IOException {
        try (final ZipFile zipFile = new ZipFile((File)source)) {
            final ZipEntry ze = zipFile.getEntry("[Content_Types].xml");
            try (InputStream is = zipFile.getInputStream(ze)) {
                data = IOUtils.toByteArray(is);
            }
        }

        this.treeNode = treeNode;
        Object oldUserObject = treeNode.getUserObject();
        surrugateEntry = (oldUserObject instanceof TreeModelEntry) ? (TreeModelEntry)oldUserObject : null;
    }

    @Override
    public String toString() {
        final String name = "[Content_Types].xml";
        return (treeNode.getParent() == null || surrugateEntry == null)
                ? name : surrugateEntry+" ("+name+")";
    }

    @Override
    public void activate() {
        final String fileName = toString();
        treeObservable.setFileName(fileName);
        treeObservable.setSourceType(TreeObservable.SourceType.text_xml);
        treeObservable.setProperties(null);
        treeObservable.setTreeEntryListener(null);
        treeObservable.setBinarySource(() -> new ByteArrayEditableData(this.data));
    }

    @Override
    public void close() throws IOException {

    }
}
