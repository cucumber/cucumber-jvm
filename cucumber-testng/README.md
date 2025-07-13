Cucumber TestNG 
==============

Use TestNG to execute Cucumber scenarios. To use add the `cucumber-testng` dependency to your `pom.xml`
and use the [`cucumber-bom`](../cucumber-bom/README.md) for dependency management:

```xml
<dependencies>
  [...]
    <dependency>
        <groupId>io.cucumber</groupId>
        <artifactId>cucumber-testng</artifactId>
        <scope>test</scope>
    </dependency>
  [...]
</dependencies>
```

Create an empty class that extends the `AbstractTestNGCucumberTests`.

```java
package io.cucumber.runtime.testng;

import io.cucumber.testng.AbstractTestNGCucumberTests;
import io.cucumber.testng.CucumberOptions;

@CucumberOptions(plugin = "message:target/cucumber-report.ndjson")
public class RunCucumberTest extends AbstractTestNGCucumberTests {
}
```

This will execute all scenarios in the same package as the runner. By default, glue code is also assumed to be in the same 
package. The `@CucumberOptions` can be used to provide
[additional configuration](https://docs.cucumber.io/cucumber/api/#list-configuration-options) to the runner. 

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
public class RunCucumberTest extends AbstractTestNGCucumberTests {

    @Override
    @DataProvider(parallel = true)
    public Object[][] scenarios() {
        return super.scenarios();
    }
}
```

#### Maven Surefire plugin configuration for parallel execution ####

```xml
<plugins>
   <plugin>
      <groupId>org.apache.maven.plugins</groupId>
      <artifactId>maven-surefire-plugin</artifactId>
      <configuration>
         <properties>
            <property>
               <name>dataproviderthreadcount</name>
               <value>${threadcount}</value>
            </property>
         </properties>
      </configuration>
   </plugin>
</plugins>
```
Where **dataproviderthreadcount** is the default number of threads to use for data providers when running tests in parallel.

### Configure cucumber options via testNG xml

If you need different [cucumber options](../cucumber-core) for each test suite, add the cucumber options as parameters to the relevant suite. Add the common options inside the
suite.

```xml
<!DOCTYPE suite SYSTEM "http://testng.org/testng-1.0.dtd" >
<suite name="Example Suite">
	<parameter name="cucumber.filter.tags" value="@Gherkin and not @Zucchini" />
	
	<test name="Vegetable garden" preserve-order="true">
        <parameter name="cucumber.features" value="classpath:com/example/features/vegetable"/>
		<parameter name="cucumber.glue" value="com.example.vegetables.glue"/>
		<classes>
			<class name="com.example.RunCucumberTests"/>
		</classes>
	</test>
 	
	<test name="Herb garden" preserve-order="true">
		<parameter name="cucumber.features" value="classpath:com/example/features/herbs"/>
		<parameter name="cucumber.glue" value="com.example.herbs.glue"/>
		<classes>
			<class name="com.example.RunCucumberTests"/>
		</classes>
	</test>
</suite>
```
