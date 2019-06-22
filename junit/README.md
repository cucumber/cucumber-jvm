Cucumber JUnit 
==============

Use JUnit to execute cucumber scenarios.

Add the `cucumber-junit` dependency to your pom.xml:

```xml
<dependencies>
  [...]
    <dependency>
        <groupId>io.cucumber</groupId>
        <artifactId>cucumber-junit</artifactId>
        <version>${cucumber.version}</version>
        <scope>test</scope>
    </dependency>
  [...]
</dependencies>
```

Create an empty class that uses the Cucumber JUnit runner.

```java
package com.example;

import io.cucumber.junit.CucumberOptions;
import io.cucumber.junit.Cucumber;
import org.junit.runner.RunWith;

@RunWith(Cucumber.class)
@CucumberOptions(plugin = "json:target/cucumber-report.json")
public class RunCucumberTest {
}
```

This will execute all scenarios in same package as the runner, by default glue code is also assumed to be in the same 
package. The `@CucumberOptions` can be used to provide
[additional configuration](https://docs.cucumber.io/cucumber/api/#list-configuration-options) to the runner. 


## Using JUnit Rules ##

Cucumber supports JUnits `@ClassRule`, `@BeforeClass` and `@AfterClass` annotations. These will executed before and 
after all scenarios. Using these is not recommended as it limits the portability between different runners; they may not
execute correctly when using the commandline, [IntelliJ IDEA](https://www.jetbrains.com/help/idea/cucumber.html) or
[Cucumber-Eclipse](https://github.com/cucumber/cucumber-eclipse). Instead it is recommended to use Cucumbers `Before` 
and `After` hooks.

## Using other JUnit features ##

The Cucumber runner acts like a suite of a JUnit tests. As such other JUnit features such as Custom JUnit 
Listeners and Reporters can all be expected to work.

For more information on JUnit, see the [JUnit web site](http://www.junit.org).

## Assume ## 

Through [Assume](https://junit.org/junit4/javadoc/4.12/org/junit/Assume.html) JUnit provides: 

> a set of methods useful for stating assumptions about the conditions in which a test is meaningful. A failed 
assumption does not mean the code is broken, but that the test provides no useful information. The default JUnit 
runner skips tests with failing assumptions. Custom runners may behave differently. 

The Cucumber runner supports `Assume` and will marked skipped scenarios as skipped.

## Parallel Execution with Maven ##

Cucumber JUnit supports parallel execution of feature files across multiple threads. To enable this with maven set the 
`parallel` property to either `methods` or `both`.

```xml
<build>
    <plugins>
        <plugin>
            <artifactId>maven-surefire-plugin</artifactId>
            <version>${maven-surefire-plugin.version}</version>  <!-- Use 2.22.1 or higher -->
            <configuration>
                <parallel>both</parallel>
                <threadCount>4</threadCount>
            </configuration>
        </plugin>
    </plugins>
</build>
```
