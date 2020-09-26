Cucumber Core
=============

Provides components needed to discover, parse and execute feature files. The
core is designed with a few extension systems and plugin points. You
typically don't depend directly on `cucumber-core` but rather use the different
sub modules together e.g. `cucumber-junit` and `cucumber-java`.     

## Properties, Environment variables, System Options ##

Cucumber will in order of precedence parse properties from system properties,
environment variables, `@CucumberOptions` and the `cucumber.properties` file.
Note that the CLI arguments take precedence over all.

Note that the `cucumber-junit-platform-engine` is provided with properties
by the Junit Platform rather than Cucumber. See
[junit-platform-engine Configuration Options](../junit-platform-engine#configuration-options)
for more information.

Supported properties are:

```
cucumber.ansi-colors.disabled=  # true or false. default: false
                     
cucumber.execution.dry-run=     # true or false. default: false
 
cucumber.execution.limit=       # number of scenarios to execute (CLI only).
  
cucumber.execution.order=       # lexical, reverse, random or random:[seed] (CLI only). default: lexical

cucumber.execution.strict=      # true or false. default: false.

cucumber.execution.wip=         # true or false. default: false.
                                # Fails if there any passing scenarios
                                # CLI only.   

cucumber.features=              # command separated paths to feature files. 
                                # example: path/to/example.feature, path/to/other.feature
  
cucumber.filter.name=           # a regular expression
                                # only scenarios with matching names are executed. 
                                # example: ^Hello (World|Cucumber)$     

cucumber.filter.tags=           # a cucumber tag expression. 
                                # only scenarios with matching tags are executed. 
                                # example: @Cucumber and not (@Gherkin or @Zucchini)

cucumber.glue=                  # comma separated package names. 
                                # example: com.example.glue  
  
cucumber.plugin=                # comma separated plugin strings. 
                                # example: pretty, json:path/to/report.json

cucumber.object-factory=        # object factory class name.
                                # example: com.example.MyObjectFactory

cucumber.publish.enabled        # true or false. default: false
                                # enable publishing of test results 

cucumber.publish.quiet          # true or false. default: false
                                # supress publish banner after test exeuction  

cucumber.publish.token          # any string value.
                                # publish authenticated test results

cucumber.publish.url            # a valid url
                                # location to publish test reports to

cucumber.snippet-type=          # underscore or camelcase. 
                                # default: underscore
```

Each property also has an `UPPER_CASE` and `snake_case` variant. For example
`cucumber.ansi-colors.disabled` would also be understood as 
`CUCUMBER_ANSI_COLORS_DISABLED` and `cucumber_ansi_colors_disabled`.

## Backend ##

Backends consist of two components: a `Backend`, and an `ObjectFactory`. They are
respectively responsible for discovering glue classes, registering step definitions,
and creating instances of said glue classes. Backend and object factory
implementations are discovered via SPI.

## Plugin ##

By implementing the Plugin interface classes can listen to execution events
inside Cucumber JVM. Consider using a Plugin when creating test execution reports.

## FileSystem ##

Cucumber uses `java.nio.fileFileSystems` to scan for features and glue and will
be able to scan features on any file system registered with the JVM.

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
