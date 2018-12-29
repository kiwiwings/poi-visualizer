# poi-visualizer
POI Visualizer - a OLE/OOXML browser/editor

A small tool to visualize the content of OLE / OOXML files (e.g. *.xls(x) / *.doc(x) / *.ppt(x)).
I've started to develop this tool due the lack of support for [SSView][1] in Linux ...

As I use it together with the current POI trunk, you might need to download it from the [SVN][2] and use `ant mvn-install` on it.

The JavaFX implementation is currently buggy with Ubuntu running on the [Wayland environment][3],
hence you might want to either use Oracle Java 9 (which works with Wayland) or X11 (which is supposed to work with OpenJDK) .


[1]: http://www.mitec.cz/ssv.html
[2]: http://poi.apache.org/devel/subversion.html
[3]: https://www.infoworld.com/article/3305073/java/removed-from-jdk-11-javafx-11-arrives-as-a-standalone-module.html