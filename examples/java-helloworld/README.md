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

Sometimes it can be useful to override these options without changing or recompiling the JUnit class. This can be done with the
`cucumber.options` system property. The general form is:

Using Maven:
```
mvn -Dcucumber.options="..." test
```

Using Ant:
```
JAVA_OPTIONS='-Dcucumber.options="..."' ant runcukes
```

Let's look at some things you can do with `cucumber.options`. Try this:
```
-Dcucumber.options="--help"
```

That should list all the available options.

#### Run a subset of Features or Scenarios

Specify a particular scenario by *line* (and use the pretty format)

```
-Dcucumber.options="classpath:cucumber/examples/java/helloworld/helloworld.feature:4 --format pretty"
```

This works because Maven puts `./src/test/resources` on your `classpath`.
You can also specify files to run by filesystem path:

```
-Dcucumber.options="src/test/resources/cucumber/examples/java/helloworld/helloworld.feature:4 --format pretty"
```

You can also specify what to run by *tag*:

```
-Dcucumber.options="--tags @bar --format pretty"
```

#### Specify a different formatter:

For example a JUnit formatter:
```
-Dcucumber.options="--format junit:target/cucumber-junit-report.xml"
```

