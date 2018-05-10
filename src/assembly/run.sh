#!/bin/sh

#--add-exports javafx.graphics/com.sun.javafx.text=richtextfx \
#--add-exports javafx.graphics/com.sun.javafx.scene.text=richtextfx \
#--add-exports javafx.graphics/com.sun.javafx.geom=richtextfx \
#--add-opens javafx.graphics/javafx.scene.text=richtextfx \
#--add-opens javafx.graphics/com.sun.javafx.text=richtextfx \


java \
-Duser.language=en \
-Duser.country=US \
-Dsun.reflect.debugModuleAccessChecks=true \
-Djava.locale.providers=JRE,CLDR \
--illegal-access=warn \
-cp "lib/*" \
de.kiwiwings.poi.visualizer.POIVisualizer
