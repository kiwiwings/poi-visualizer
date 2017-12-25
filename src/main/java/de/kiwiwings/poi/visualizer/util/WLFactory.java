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

package de.kiwiwings.poi.visualizer.util;

import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.util.function.Consumer;

public class WLFactory {
    public static WindowListener windowOpened(Consumer<WindowEvent> c) {
        return new WindowAdapter() {
            @Override public void windowOpened(WindowEvent e) { c.accept(e); }
        };
    }
    
    public static WindowListener windowClosing(Consumer<WindowEvent> c) {
        return new WindowAdapter() {
            @Override public void windowClosing(WindowEvent e) { c.accept(e); }
        };
    }

    public static WindowListener windowClosed(Consumer<WindowEvent> c) {
        return new WindowAdapter() {
            @Override public void windowClosed(WindowEvent e) { c.accept(e); }
        };
    }

    public static WindowListener windowIconified(Consumer<WindowEvent> c) {
        return new WindowAdapter() {
            @Override public void windowIconified(WindowEvent e) { c.accept(e); }
        };
    }

    public static WindowListener windowDeiconified(Consumer<WindowEvent> c) {
        return new WindowAdapter() {
            @Override public void windowDeiconified(WindowEvent e) { c.accept(e); }
        };
    }

    public static WindowListener windowActivated(Consumer<WindowEvent> c) {
        return new WindowAdapter() {
            @Override public void windowActivated(WindowEvent e) { c.accept(e); }
        };
    }

    public static WindowListener windowDeactivated(Consumer<WindowEvent> c) {
        return new WindowAdapter() {
            @Override public void windowDeactivated(WindowEvent e) { c.accept(e); }
        };
    }

    public static WindowListener windowStateChanged(Consumer<WindowEvent> c) {
        return new WindowAdapter() {
            @Override public void windowStateChanged(WindowEvent e) { c.accept(e); }
        };
    }

    public static WindowListener windowGainedFocus(Consumer<WindowEvent> c) {
        return new WindowAdapter() {
            @Override public void windowGainedFocus(WindowEvent e) { c.accept(e); }
        };
    }

    public static WindowListener windowLostFocus(Consumer<WindowEvent> c) {
        return new WindowAdapter() {
            @Override public void windowLostFocus(WindowEvent e) { c.accept(e); }
        };
    }
}
