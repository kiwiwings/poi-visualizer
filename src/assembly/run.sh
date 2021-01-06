#!/bin/bash

export JAVA_HOME=/usr/lib/jvm/java-14-oracle
export PATH=$JAVA_HOME/bin:$PATH


java \
-Duser.language=en \
-Duser.country=US \
-Dsun.reflect.debugModuleAccessChecks=true \
-Djava.locale.providers=JRE,CLDR \
--illegal-access=warn \
--module-path /opt/javafx-sdk-14/lib:/home/kiwiwings/bin/poi-visualizer/lib \
-Djava.library.path=/opt/javafx-sdk-14/lib \
--module kiwiwings.poivisualizer/de.kiwiwings.poi.visualizer.POIVisualizer
