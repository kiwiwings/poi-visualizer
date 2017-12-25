#!/bin/sh

env GDK_SCALE=2 java \
"-Duser.language=en" \
"-Duser.country=US" \
"-Dsun.reflect.debugModuleAccessChecks=true" \
"-Djava.locale.providers=JRE,CLDR" \
"--add-modules=java.xml.bind" \
"--add-opens=java.xml/com.sun.org.apache.xerces.internal.util=ALL-UNNAMED" \
"--add-opens=java.base/java.io=ALL-UNNAMED" \
"--add-opens=java.base/java.nio=ALL-UNNAMED" \
"--add-opens=java.base/java.lang=ALL-UNNAMED" \
"--add-opens=java.base/jdk.internal.ref=ALL-UNNAMED" \
"--add-opens=java.base/java.util.zip=ALL-UNNAMED" \
"-cp" "lib/*" \
"de.kiwiwings.poi.visualizer.POIVisualizer"