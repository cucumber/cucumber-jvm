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

As a workaround you can use the JUnit Platform Console Launcher by using either
the Maven Antrun plugin or the Gradle JavaExec task.

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

By default, Cucumber tests are run sequentially in a single thread. Running
tests in parallel is available as an opt-in feature. To enable parallel
execution, set the set the `cucumber.execution.parallel.enabled` configuration
parameter to `true`, e.g. in `junit-platform.properties`.

## Configuration Options ##

Cucumber receives its configuration from the JUnit platform. To see how these
can be supplied see the JUnit documentation [4.5. Configuration Parameters](https://junit.org/junit5/docs/current/user-guide/user-guide/index.html#running-tests-config-params). 
For supported values see [Constants](src/main/java/io/cucumber/junit/platform/engine/Constants.java).

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

## Tags

Cucumber tags are mapped to JUnit tags. Note that the `@` symbol is not part of
the JUnit tag. So the scenario below is tagged with `Smoke` and `Sanity`. 

```gherkin
@Smoke @Sanity
Scenario: A tagged scenario
  Given I tag a scenario 
  When I select tests with that tag for execution 
  Then my tagged scenario is executed
```
 
See the relevant documentation on how to select tags:
* [Maven: Filtering by Tags](https://maven.apache.org/surefire/maven-surefire-plugin/examples/junit-platform.html)
* [Gradle: Test Grouping](https://docs.gradle.org/current/userguide/java_testing.html#test_grouping)
* [JUnit 5 Console Launcher: Options](https://junit.org/junit5/docs/current/user-guide/#running-tests-console-launcher-options)
