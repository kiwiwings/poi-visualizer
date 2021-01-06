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
import org.exbin.utils.binary_data.ByteArrayEditableData;

import java.io.IOException;
import java.util.function.Consumer;

public class GenericNamedEntry implements TreeModelEntry {
    private final String name;
    private final Consumer<DocumentFragment> handler;

    public GenericNamedEntry(String name, Consumer<DocumentFragment> handler) {
        this.name = name;
        this.handler = handler;
    }

    @Override
    public void activate(DocumentFragment fragment) {
        fragment.setBinarySource(ByteArrayEditableData::new);
        fragment.setSourceType(SourceType.empty);
        fragment.setProperties("");
        handler.accept(fragment);
    }

    @Override
    public String toString() {
        return name;
    }

    @Override
    public void close() throws IOException {

    }
}
