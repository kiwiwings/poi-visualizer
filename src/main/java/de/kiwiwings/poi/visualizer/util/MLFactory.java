/* ====================================================================
   Licensed to the Apache Software Foundation (ASF) under one or more
   contributor license agreements.  See the NOTICE file distributed with
   this work for additional information regarding copyright ownership.
   The ASF licenses this file to You under the Apache License, Version 2.0
   (the "License"); you may not use this file except in compliance with
   the License.  You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
==================================================================== */

package de.kiwiwings.poi.visualizer.util;

import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.function.Consumer;

/**
 * Helper class to consume MouseEvents via lambda expression
 */
public class MLFactory {
    public static MouseListener mouseClicked(Consumer<MouseEvent> c) {
        return new MouseAdapter() {
            @Override public void mouseClicked(MouseEvent e) { c.accept(e); }
        };
    }
    
    public static MouseListener mousePressed(Consumer<MouseEvent> c) {
        return new MouseAdapter() {
            @Override public void mousePressed(MouseEvent e) { c.accept(e); }
        };
    }

    public static MouseListener mouseReleased(Consumer<MouseEvent> c) {
        return new MouseAdapter() {
            @Override public void mouseReleased(MouseEvent e) { c.accept(e); }
        };
    }

    public static MouseListener mouseEntered(Consumer<MouseEvent> c) {
        return new MouseAdapter() {
            @Override public void mouseEntered(MouseEvent e) { c.accept(e); }
        };
    }

    public static MouseListener mouseExited(Consumer<MouseEvent> c) {
        return new MouseAdapter() {
            @Override public void mouseExited(MouseEvent e) { c.accept(e); }
        };
    }

    public static MouseListener mousePopup(Consumer<MouseEvent> c) {
        return new MouseAdapter() {
            @Override public void mousePressed(MouseEvent e) { c.accept(e); }
            @Override public void mouseReleased(MouseEvent e) { c.accept(e); }
        };
    }
}
