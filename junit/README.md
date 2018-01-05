Cucumber JUnit 
==============

Use JUnit to execute cucumber scenarios.

Add the `cucumber-junit` dependency to your pom.

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

import cucumber.api.CucumberOptions;
import cucumber.api.junit.Cucumber;
import org.junit.runner.RunWith;

@RunWith(Cucumber.class)
@CucumberOptions(plugin = "json:target/cucumber-report.json")
public class RunCukesTest {
}
```

This will execute all scenarios in same package as the runner, by default glue code is also assumed to be in the same 
package. The `@CucumberOptions` can be used to provide
[additional configuration](https://cucumber.io/docs/reference/jvm#list-configuration-options) to the runner. 


## Using JUnit Rules ##

Cucumber supports JUnits `@ClassRule`, `@BeforeClass` and `@AfterClass` annotations. These will executed before and 
after all scenarios. Using these is not recommended as it limits the portability between different runners; they may not
execute correctly when using the commandline, [IntelliJ IDEA](https://www.jetbrains.com/help/idea/cucumber.html) or
[Cucumber-Eclipse](https://github.com/cucumber/cucumber-eclipse). Instead it is recommended to use Cucumbers `Before` 
and `After` hooks.

## Using other JUnit features ##

The Cucumber runner acts like a suite of a JUnit tests. As such other JUnit features such as Categories, Custom JUnit 
Listeners and Reporters can all be expected to work.

For more information on JUnit, see the [JUnit web site](http://www.junit.org).
