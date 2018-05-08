package de.kiwiwings.poi.visualizer.treemodel;


import java.io.Closeable;
import java.util.Observable;
import java.util.Observer;

public interface TreeModelEntry extends Closeable, Observer {
    @Override
    default void update(Observable o, Object arg) {}

    @Override
    String toString();

    /**
     * Entry is clicked/activate - don't update the observable(s)
     */
    void activate();
}
