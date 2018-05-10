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


import de.kiwiwings.poi.visualizer.DocumentFragment;

import java.io.Closeable;

public interface TreeModelEntry extends Closeable {
    @Override
    String toString();

    /**
     * Entry is clicked/activated.
     * The observables will be updated by the caller.
     * @param fragment the document to receive the entry properties
     */
    void activate(DocumentFragment fragment);

//    /**
//     * Store the document entries into the entry
//     * @param fragment the document containing the new data
//     */
//    void update(DocumentFragment fragment);

}
