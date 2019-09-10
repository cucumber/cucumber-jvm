Cucumber JUnit Platform Engine
==============================

Use JUnit Platform to execute cucumber scenarios.

Add the `cucumber-junit-platform-engine` dependency to your `pom.xml`:


```xml
<dependency>
    <groupId>io.cucumber</groupId>
    <artifactId>cucumber-junit-platform-engine</artifactId>
    <scope>test</scope>
</dependency>
```

This will allow the IntelliJ IDEA, Eclipse, Maven Surefire, ect to discover, select and execute Cucumber scenarios. 

## Parallel execution ## 

By default, Cucumber tests are run sequentially in a single thread. Running tests in parallel, e.g. to speed up 
execution, is available as an opt-in feature. To enable parallel execution, simply set the set the 
`cucumber.execution.parallel.enabled` configuration parameter to `true`, e.g. in `junit-platform.properties`.

## Configuration Options ##

Cucumber receives its configuration from the JUnit platform. To see how these can be supplied see the JUnit documentation
[4.5. Configuration Parameters](https://junit.org/junit5/docs/5.3.0-M1/user-guide/index.html#running-tests-config-params). 
For supported values see [Constants](src/main/java/io/cucumber/jupiter/engine/Constants.java).


## Supported Discovery Selectors and Filters ## 

Supported `DiscoverySelector`s are:
* `ClasspathRootSelector`
* `ClasspathResourceSelector`
* `PackageSelector`
* `FileSelector`
* `DirectorySelector`
* `UriSelector`
* `UniqueIdSelector`

The only supported `DiscoveryFilter` is the `PackageNameFilter` and only when features are selected from the classpath.

## Tags

Cucumber tags are mapped to JUnit tags. See the relevant documentation on how to select tags:
* [Maven: Filtering by Tags](https://maven.apache.org/surefire/maven-surefire-plugin/examples/junit-platform.html)
* [Gradle: Test Grouping](https://docs.gradle.org/current/userguide/java_testing.html#test_grouping)
* [JUnit 5 Console Launcher: Options](https://junit.org/junit5/docs/current/user-guide/#running-tests-console-launcher-options)
