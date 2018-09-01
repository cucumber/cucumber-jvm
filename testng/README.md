Cucumber TestNG 
==============

Use TestNG to execute cucumber scenarios.

Add the `cucumber-testng` dependency to your pom.

```xml
<dependencies>
  [...]
    <dependency>
        <groupId>io.cucumber</groupId>
        <artifactId>cucumber-testng</artifactId>
        <version>${cucumber.version}</version>
        <scope>test</scope>
    </dependency>
  [...]
</dependencies>
```

Create an empty class that extends the `AbstractTestNGCucumberTests`.

```java
package cucumber.runtime.testng;

import cucumber.api.CucumberOptions;
import cucumber.api.testng.AbstractTestNGCucumberTests;

@CucumberOptions(plugin = "json:target/cucumber-report.json")
public class RunCukesTest extends AbstractTestNGCucumberTests {
}
```

This will execute all scenarios in same package as the runner, by default glue code is also assumed to be in the same 
package. The `@CucumberOptions` can be used to provide
[additional configuration](https://cucumber.io/docs/reference/jvm#list-configuration-options) to the runner. 

## Test composition ##

It is possible to use TestNG without inheriting from `AbstractTestNGCucumberTests` by using the `TestNGCucumberRunner`. 
See the [RunCukesByCompositionTest Example](../examples/java-calculator-testng/src/test/java/cucumber/examples/java/calculator/RunCukesByCompositionTest.java) 
for usage.

## SkipException ##

Cucumber provides limited support for [SkipException](https://jitpack.io/com/github/cbeust/testng/master/javadoc/org/testng/SkipException.html).

* Throwing a `SkipException` results in both Cucumber and TestNG marking the test as skipped.
* Throwing a subclass of `SkipException` results in Cucumber marking the test as failed and TestNG marking the test 
as skipped.

## Parallel execution ##

Cucumber TestNG supports parallel execution of scenarios. Override the `scenarios` method to enable parallel execution.

```java
public class RunCukesTest extends AbstractTestNGCucumberTests {

    @Override
    @DataProvider(parallel = true)
    public Object[][] scenarios() {
        return super.scenarios();
    }
}
```