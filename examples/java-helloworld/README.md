## Hello World

This is the simplest possible setup for Cucumber-JVM using Java.

### Ant

Simply run:

```
ant download
ant runcukes
```

This runs Cucumber features using the Command Line Interface (CLI) runner. Note that the `RunCukes` junit class is not used at all.
If you remove it (and the `cucumber-junit` jar dependency), it will run just the same.

### Maven

Simply run:

```
mvn test
```

This runs Cucumber features using the JUnit runner. The `@RunWith(Cucumber.class)` annotation on the `RunCukes` junit class
kicks off Cucumber.
