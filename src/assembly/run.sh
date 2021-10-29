#!/bin/bash

export JAVA_HOME=/usr/lib/jvm/java-17-oracle
export PATH=$JAVA_HOME/bin:$PATH


# GDK_SCALE=2
java \
-Duser.language=en \
-Duser.country=US \
-Dsun.reflect.debugModuleAccessChecks=true \
-Djava.locale.providers=JRE,CLDR \
-Dglass.gtk.uiScale=192dpi \
--illegal-access=warn \
--module-path /opt/javafx-sdk-17.0.1/lib:/home/kiwiwings/bin/poi-visualizer/lib \
-Djava.library.path=/opt/javafx-sdk-17.0.1/lib \
--module kiwiwings.poivisualizer/de.kiwiwings.poi.visualizer.POIVisualizer
