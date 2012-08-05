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

#### Overriding options

The Cucumber runtime parses command line options to know what features to run, where the glue code lives, what formatters to use etc.
When you use the JUnit runner, these options are generated from the `@Cucumber.Options` annotation on your test.

Sometimes it can be useful to override these options without changing the JUnit class. This can be done with the `cucumber.options` system property. If it is specified it will override *all* options. Here is an example:

```
mvn test -Dcucumber.options="--format json-pretty --glue classpath:cucumber/examples/java/helloworld src/test/resources"
```

