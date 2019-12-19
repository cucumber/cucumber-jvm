Cucumber Core
=============

Provides components needed to discover, parse and execute feature files. The
core is designed with a few extension systems and plugin points. You
typically don't depend directly on `cucumber-core` but rather use the different
sub modules together e.g. `cucumber-junit` and `cucumber-java`.     

## Backend ##

Backends consists of two components, a `Backend` and `ObjectFactory`. They are
respectively responsible for discovering glue classes, registering step definitions
and creating instances of said glue classes. Backend and object factory
implementations are discovered via SPI

## Plugin ##

By implementing the Plugin interface classes can listen to execution events
inside Cucumber JVM. Consider using a Plugin when creating test execution reports.

## FileSystem ##

Cucumber uses `java.nio.fileFileSystems` to scan for features and will be able
to scan features on any file system registered with the JVM.

## Logging ##
Cucumber uses the Java Logging APIs from `java.util.logging`. See the
[LogManager](https://docs.oracle.com/javase/8/docs/api/java/util/logging/LogManager.html)
for configuration options or use the [JUL to SLF4J Bridge](https://www.slf4j.org/legacy.html#jul-to-slf4j).

For quick debugging run with:  

```
-Djava.util.logging.config.file=path/to/logging.properties
```

```properties
handlers=java.util.logging.ConsoleHandler
.level=FINE
java.util.logging.ConsoleHandler.level=FINE
java.util.logging.ConsoleHandler.formatter=java.util.logging.SimpleFormatter
```