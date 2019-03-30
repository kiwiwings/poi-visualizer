# poi-visualizer
POI Visualizer - a OLE/OOXML browser/editor

A small tool to visualize the content of OLE / OOXML files (e.g. *.xls(x) / *.doc(x) / *.ppt(x)).
I've started to develop this tool due the lack of support for [SSView][1] in Linux ...

As I use it together with the current POI trunk, you might need to download it from the [SVN][2] and use `ant mvn-install` on it.

The stable JavaFX 11 implementation is currently buggy with Ubuntu running on the [Wayland environment][3],
You need to ...
* use Oracle JDK 9
* or set `GDK_BACKEND=X11`, e.g. `> env GDK_BACKEND=X11 run.sh ...`  
* or download [openjfx-12 (or higher)][4] and add it to your module-path:  

```shell
        java \
        -Duser.language=en \
        -Duser.country=US \
        -Dsun.reflect.debugModuleAccessChecks=true \
        -Djava.locale.providers=JRE,CLDR \
        --illegal-access=warn \
        -cp ".../poi-visualizer/lib/*" \
        --module-path /opt/javafx-sdk-12/lib \
        --add-modules=javafx.controls,javafx.fxml,javafx.swing \
        --add-opens javafx.graphics/javafx.scene.text=ALL-UNNAMED \
        --add-exports javafx.graphics/com.sun.javafx.text=ALL-UNNAMED \
        -Djava.library.path=/opt/javafx-sdk-12/lib \
        de.kiwiwings.poi.visualizer.POIVisualizer
```

[1]: http://www.mitec.cz/ssv.html
[2]: http://poi.apache.org/devel/subversion.html
[3]: https://www.infoworld.com/article/3305073/java/removed-from-jdk-11-javafx-11-arrives-as-a-standalone-module.html
[4]: https://openjfx.io/