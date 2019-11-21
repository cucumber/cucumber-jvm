Cucumber JUnit Platform Engine
==============================

Use JUnit Platform to execute cucumber scenarios.

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

## Maven Surefire workaround ##

Maven Surefire does not yet support discovery of non-class based test as a
workaround you can use the antrun plugin to start the the JUnit Platform 
Console Launcher.

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
## Gradle Test workaround ##

Gradle Test does not yet support discovery of non-class based test ([gradle/#4773](https://github.com/gradle/gradle/issues/4773)). 
As a work around you can use a custom task to start the the JUnit Platform
Console Launcher.

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
* `PackageSelector`
* `FileSelector`
* `DirectorySelector`
* `UriSelector`
* `UniqueIdSelector`

The only supported `DiscoveryFilter` is the `PackageNameFilter` and only when
features are selected from the classpath.

## Tags

Cucumber tags are mapped to JUnit tags. See the relevant documentation on how to
select tags:
* [Maven: Filtering by Tags](https://maven.apache.org/surefire/maven-surefire-plugin/examples/junit-platform.html)
* [Gradle: Test Grouping](https://docs.gradle.org/current/userguide/java_testing.html#test_grouping)
* [JUnit 5 Console Launcher: Options](https://junit.org/junit5/docs/current/user-guide/#running-tests-console-launcher-options)
