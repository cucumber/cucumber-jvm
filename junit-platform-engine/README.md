Cucumber JUnit Platform Engine
==============================

Use JUnit Platform to execute Cucumber scenarios.

Add the `cucumber-junit-platform-engine` dependency to your `pom.xml`:

```xml
<dependency>
    <groupId>io.cucumber</groupId>
    <artifactId>cucumber-junit-platform-engine</artifactId>
    <version>${cucumber.version}</version>
    <scope>test</scope>
</dependency>
```

This will allow the IntelliJ IDEA, Eclipse, Maven, Gradle, ect, to discover,
select and execute Cucumber scenarios. 

## Surefire and Gradle workarounds

Maven Surefire and Gradle do not yet support discovery of non-class based tests
(see: [gradle/#4773](https://github.com/gradle/gradle/issues/4773),
[SUREFIRE-1724](https://issues.apache.org/jira/browse/SUREFIRE-1724)). As a
workaround you can either use the `@Cucumber` annotation or the JUnit Platform
Console Launcher.

### Use the @Cucumber annotation ###

Cucumber will scan the package of a class annotated with `@Cucumber` for feature
files.  

```java
package com.example.app;

import io.cucumber.junit.platform.engine.Cucumber;

@Cucumber
public class RunCucumberTest {
}
```

### Use the JUnit Console Launcher ###

You can integrate the JUnit Platform Console Launcher in your build by using
either the Maven Antrun plugin or the Gradle JavaExec task.

```xml
<dependencies>
    ....
    <dependency>
        <groupId>org.junit.platform</groupId>
        <artifactId>junit-platform-launcher</artifactId>
        <version>${junit-platform.version}</version>
        <scope>test</scope>
    </dependency>
</dependencies>

<build>
    <plugins>
        <plugin>
            <groupId>org.apache.maven.plugins</groupId>
            <artifactId>maven-antrun-plugin</artifactId>
            <executions>
                <execution>
                    <!--Work around. Surefire does not use JUnits Test Engine discovery functionality -->
                    <id>CLI-test</id>
                    <phase>integration-test</phase>
                    <goals>
                        <goal>run</goal>
                    </goals>
                    <configuration>
                        <target>
                            <echo message="Running JUnit Platform CLI"/>
                            <java classname="org.junit.platform.console.ConsoleLauncher"
                                  fork="true"
                                  failonerror="true"
                                  newenvironment="true"
                                  maxmemory="512m"
                                  classpathref="maven.test.classpath">
                                <arg value="--include-engine"/>
                                <arg value="cucumber"/>
                                <arg value="--scan-classpath"/>
                                <arg value="${project.build.testOutputDirectory}"/>
                            </java>
                        </target>
                    </configuration>
                </execution>
            </executions>
        </plugin>
    </plugins>
</build>
```
```groovy

tasks {

	val consoleLauncherTest by registering(JavaExec::class) {
		dependsOn(testClasses)
		val reportsDir = file("$buildDir/test-results")
		outputs.dir(reportsDir)
		classpath = sourceSets["test"].runtimeClasspath
		main = "org.junit.platform.console.ConsoleLauncher"
		args("--scan-classpath")
		args("--include-engine", "cucumber")
		args("--reports-dir", reportsDir)
	}

	test {
		dependsOn(consoleLauncherTest)
		exclude("**/*")
	}
}
```

## Parallel execution ## 

By default, Cucumber runs tests sequentially in a single thread. Running
tests in parallel is available as an opt-in feature. To enable parallel
execution, set the `cucumber.execution.parallel.enabled` configuration
parameter to `true`, e.g. in `junit-platform.properties`.

Cucumber supports JUnits `ParallelExecutionConfigurationStrategy` see the
configuration options below.

### Exclusive Resources ###

Using exclusive resources it is possible to control which scenarios will
not run concurrently with other scenarios that use the same resource.
 
Cucumber tags can be mapped to exclusive resources. A resource can be
either locked with a read-write-lock, or a read lock.  
  
For example:
 
 
```gherkin
Feature: Exclusive resources

 @my-tag-ab-rw
 Scenario: first example
   Given this reads and writes resource a
   And this reads and writes resource b
   When it is executed it will   
   Then it will not be executed concurrently with the second example

 @my-tag-a-r
 Scenario: second example
   Given this reads resource a
   When it is executed it will
   Then it will not be executed concurrently with the first example

```
 
With this configuration: 
 
```
cucumber.execution.exclusive-resources.my-tag-ab-rw.read-write=resource-a,resource-b
cucumber.execution.exclusive-resources.my-tag-a-r.read=resource-a
```
 
The first scenario tagged with `@my-tag-ab-rw` will lock resource `a` and `b`
with a read-write lock and will not be concurrently executed with the second
scenario that locks resource `a` with a read lock.

Note: The `@` is not included.

## Configuration Options ##

Cucumber receives its configuration from the JUnit platform. To see how these
can be supplied see the JUnit documentation [4.5. Configuration Parameters](https://junit.org/junit5/docs/current/user-guide/#running-tests-config-params). 
For documentation see [Constants](src/main/java/io/cucumber/junit/platform/engine/Constants.java).

```
cucumber.ansi-colors.disabled=                                # true or false. default: false                     
      
cucumber.filter.tags=                                         # a cucumber tag expression. 
                                                              # only matching scenarios are executed. 
                                                              # example: @integration and not @disabled
      
cucumber.glue=                                                # comma separated package names. 
                                                              # example: com.example.glue  
      
cucumber.plugin=                                              # comma separated plugin strings. 
                                                              # example: pretty, json:path/to/report.json
      
cucumber.object-factory=                                      # object factory class name.
                                                              # example: com.example.MyObjectFactory
      
cucumber.snippet-type=                                        # underscore or camelcase. 
                                                              # default: underscore
      
cucumber.execution.dry-run=                                   # true or false. 
                                                              # default: false
       
cucumber.execution.parallel.enabled=                          # true or false. 
                                                              # default: false
      
cucumber.execution.parallel.config.strategy=                  # dynamic, fixed or custom. 
                                                              # default: dynamic
      
cucumber.execution.parallel.config.fixed.parallelism=         # positive integer. 
                                                              # example: 4 
      
cucumber.execution.parallel.config.dynamic.factor=            # positive double.
                                                              # default: 1.0
      
cucumber.execution.parallel.config.custom.class=              # class name. 
                                                              # example: com.example.MyCustomParallelStrategy

cucumber.execution.exclusive-resources.<tag-name>.read-write= # a comma seperated list of strings
                                                              # example: resource-a, resource-b 
     
cucumber.execution.exclusive-resources.<tag-name>.read=       # a comma seperated list of strings
                                                              # example: resource-a, resource-b

```

## Supported Discovery Selectors and Filters ## 

Supported `DiscoverySelector`s are:
* `ClasspathRootSelector`
* `ClasspathResourceSelector`
* `ClassSelector`
* `PackageSelector`
* `FileSelector`
* `DirectorySelector`
* `UriSelector`
* `UniqueIdSelector`

The only supported `DiscoveryFilter` is the `PackageNameFilter` and only when
features are selected from the classpath.

The `UriSelector` supports URI's with a `line` query parameter:
  - `classpath:/com/example/example.feature?line=20`
  - `file:/path/to/com/example/example.feature?line=20`
 
Any `TestDescriptor` that matches the line *and* its descendents will be 
included in the discovery result.

## Tags ##

Cucumber tags are mapped to JUnit tags. Note that the `@` symbol is not part of
the JUnit tag. So the scenarios below are tagged with `Smoke` and `Sanity`. 

```gherkin
@Smoke
@Ignore
Scenario: A tagged scenario
  Given I tag a scenario 
  When I select tests with that tag for execution 
  Then my tagged scenario is executed

@Sanity
Scenario: Another tagged scenario
  Given I tag a scenario 
  When I select tests with that tag for execution 
  Then my tagged scenario is executed

```

When using Maven tags can be provided from the CLI using the `groups` and
`excludedGroups` parameters. These take a [JUnit5 Tag Expression](https://junit.org/junit5/docs/current/user-guide/#running-tests-tag-expressions). 
The example below will execute `Another tagged scenario`.

```
mvn verify -DexcludedGroups="Ignore" -Dgroups="Smoke | Sanity"
```

For further information See the relevant documentation on how to select tags:
* [Maven: Filtering by Tags](https://maven.apache.org/surefire/maven-surefire-plugin/examples/junit-platform.html)
* [Gradle: Test Grouping](https://docs.gradle.org/current/userguide/java_testing.html#test_grouping)
* [JUnit 5 Console Launcher: Options](https://junit.org/junit5/docs/current/user-guide/#running-tests-console-launcher-options)
* [JUnit 5 Tag Expression](https://junit.org/junit5/docs/current/user-guide/#running-tests-tag-expressions)

### @Disabled

It is possible to recreate JUnit Jupiter's `@Disabled` functionality by
setting the `cucumber.filter.tags=not @Disabled` property<sup>1</sup>. Any scenarios 
tagged with `@Disabled` will be skipped. See [Configuration Options](#configuration-options)
for more information. 

1. Do note that this is a [Cucumber Tag Expression](https://cucumber.io/docs/cucumber/api/#tags)
rather then a JUnit5 tag expression.
