#!/bin/bash

export JAVA_HOME=/usr/lib/jvm/java-14-oracle
export PATH=$JAVA_HOME/bin:$PATH
#export J2SDKDIR=$JAVA_HOME
#export J2REDIR=$JAVA_HOME

#--add-exports javafx.graphics/com.sun.javafx.text=richtextfx \
#--add-exports javafx.graphics/com.sun.javafx.scene.text=richtextfx \
#--add-exports javafx.graphics/com.sun.javafx.geom=richtextfx \
#--add-opens javafx.graphics/javafx.scene.text=richtextfx \
#--add-opens javafx.graphics/com.sun.javafx.text=richtextfx \

# you might need to fully qualify the classpath (-cp) below

java \
-Duser.language=en \
-Duser.country=US \
-Dsun.reflect.debugModuleAccessChecks=true \
-Djava.locale.providers=JRE,CLDR \
--illegal-access=warn \
-cp "lib/*" \
--module-path /opt/javafx-sdk-14/lib \
--add-modules=javafx.controls,javafx.fxml,javafx.swing \
--add-opens javafx.graphics/javafx.scene.text=ALL-UNNAMED \
--add-opens javafx.graphics/com.sun.javafx.text=ALL-UNNAMED \
--add-exports javafx.graphics/com.sun.javafx.scene.text=ALL-UNNAMED \
--add-exports javafx.graphics/com.sun.javafx.geom=ALL-UNNAMED \
--add-exports javafx.graphics/com.sun.javafx.text=ALL-UNNAMED \
-Djava.library.path=/opt/javafx-sdk-14/lib \
de.kiwiwings.poi.visualizer.POIVisualizer
