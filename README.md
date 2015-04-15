<a href="http://projectdanube.org/" target="_blank"><img src="http://projectdanube.github.com/xdi2/images/projectdanube_logo.png" align="right"></a>
<img src="http://projectdanube.github.com/xdi2/images/logo64.png"><br>

This contains various maintenance and other tools for the [XDI2](http://github.com/projectdanube/xdi2) server.

### Information

* [Usage](https://github.com/projectdanube/xdi2-tools/wiki/Usage)
* Special Topics
  * [Great Symbol Shift](https://github.com/projectdanube/xdi2-tools/wiki/Great-Symbol-Shift)
  * [Link Contract Shift](https://github.com/projectdanube/xdi2-tools/wiki/Link-Contract-Shift)
  * [Notation Shift](https://github.com/projectdanube/xdi2-tools/wiki/Notation-Shift)
  * [No Value Node Shift](https://github.com/projectdanube/xdi2-tools/wiki/No-Value-Node-Shift)
  * [Star Shift](https://github.com/projectdanube/xdi2-tools/wiki/Star-Shift)
  * [Single Root Arc Shift](https://github.com/projectdanube/xdi2-tools/wiki/Single-Root-Arc-Shift)

### How to build

First, you need to build the main [XDI2](http://github.com/projectdanube/xdi2) project.

After that, just run

    mvn clean install

To build all components.

### How to run

	java -jar target/xdi2-tools-*.one-jar.jar [arguments]

Or

	mvn exec:java -Dexec.args="[arguments]"

### Community

Google Group: http://groups.google.com/group/xdi2

IRC: irc://irc.freenode.net:6667/xdi
