Cucumber JUnit 
==============

Use JUnit to execute cucumber scenarios. To use add the `cucumber-junit`
dependency to your pom.xml:

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
@CucumberOptions(plugin = "message:target/cucumber-report.ndjson")
public class RunCucumberTest {
}
```

This will execute all scenarios in the same package as the runner; by default glue
code is also assumed to be in the same 
package. The `@CucumberOptions` can be used to provide
[additional configuration](https://docs.cucumber.io/cucumber/api/#list-configuration-options) 
to the runner. 

## Using JUnit Rules ##

Cucumber supports JUnit's `@ClassRule`, `@BeforeClass`, and `@AfterClass`
annotations. These will be executed before and
after all scenarios. Using these is not recommended as it limits portability
between different runners; they may not
execute correctly when using the command line, [IntelliJ IDEA](https://www.jetbrains.com/help/idea/cucumber.html), or
[Cucumber-Eclipse](https://github.com/cucumber/cucumber-eclipse). Instead it is
recommended to use Cucumber's `Before` and `After` hooks.

## Using other JUnit features ##

The Cucumber runner acts like a suite of a JUnit tests. As such other JUnit
features like custom JUnit
Listeners and Reporters can all be expected to work.

For more information on JUnit, see the [JUnit web site](http://www.junit.org).

## Assume ## 

Through [Assume](https://junit.org/junit4/javadoc/4.12/org/junit/Assume.html) 
and [Assumptions](https://junit.org/junit5/docs/5.0.0/api/org/junit/jupiter/api/Assumptions.html) 
JUnit4 and JUnit5 provide: 

> a collection of utility methods that support conditional test execution based
> on assumptions.
> 
> In direct contrast to failed assertions, failed assumptions do not result in a
> test failure; rather, a failed assumption results in a test being aborted.
>  
> Assumptions are typically used whenever it does not make sense to continue
> execution of a given test method — for example, if the test depends on
> something that does not exist in the current runtime environment. 

The Cucumber runner supports `Assume` and will mark skipped scenarios as
skipped.

## Parallel Execution with Maven ##

Cucumber JUnit supports parallel execution of feature files across multiple 
threads. To enable this with maven set the `parallel` property to either
`methods` or `both`.

```xml
<build>
    <plugins>
        <plugin>
            <artifactId>maven-surefire-plugin</artifactId>
            <!-- Use 2.22.1 or higher -->
            <version>${maven-surefire-plugin.version}</version>  
            <configuration>
                <parallel>both</parallel>
                <threadCount>4</threadCount>
            </configuration>
        </plugin>
    </plugins>
</build>
```
