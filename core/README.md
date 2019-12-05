Cucumber Core
=============

Provides components needed to discover, parse and execute feature files. The
core is designed with a few extension systems and plugin points. You
typically don't depend on `cucumber-core` but rather use the different
sub modules together e.g. `cucumber-junit` and `cucumber-java`.     

## Backend ##

Backends consists of two components, a `Backend` and `ObjectFactory`. They are
respectively responsible for discovering glue classes, registering step definitions
and creating instances of said glue classes.

## Plugin ##

By implementing the Plugin interface classes can listen to execution events
inside Cucumber JVM.

## FileSystem ##

Cucumber uses `java.nio.fileFileSystems` to scan for features and will be able
to scan features on any file system registered with the JVM.