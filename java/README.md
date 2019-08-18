Cucumber Java
=============

Provides annotation based step definitions. To use add the `cucumber-java` dependency to your pom.xml:

```xml
<dependencies>
  [...]
    <dependency>
        <groupId>io.cucumber</groupId>
        <artifactId>cucumber-java</artifactId>
        <version>${cucumber.version}</version>
        <scope>test</scope>
    </dependency>
  [...]
</dependencies>
```

## Step Definitions

Declare a step definition by annotating a method. It is possible use the same method for multiple steps by repeating
the annotation. For localized annotations import the annotations from `io.cucumber.java.<ISO2 Language Code>.*`

```java
package com.example.app;

import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class CalculatorSteps{
    private RpnCalculator calc;

    @Given("a calculator I just turned on")
    public void a_calculator_I_just_turned_on() {
        calc = new RpnCalculator();
    }

    @When("I add {int} and {int}")
    public void adding(int arg1, int arg2) {
        calc.push(arg1);
        calc.push(arg2);
        calc.push("+");
    }

    @Then("the result is {int}")
    public void the_result_is(double expected) {
        assertEquals(expected, calc.value());
    }
}
```

## Hooks

Declare hooks that will be executed before/after each scenario/step by annotating a method. The method may declare an
argument of type `io.cucumber.core.api.Scenario`.

 * `@Before` 
 * `@After`
 * `@BeforeStep`
 * `@AfterStep`
 
## Transformers 

### Parameter Type

Step definition parameter types can be declared by using `@ParameterType`. The name of the annotated method will be used
as the parameter name.

```java
package com.example.app;

import io.cucumber.java.ParameterType;
import io.cucumber.java.en.Given;

import java.time.LocalDate;

public class Steps {

    @ParameterType("([0-9]{4})-([0-9]{2})-([0-9]{2})")
    public LocalDate iso8601Date(String year, String month, String day) {
        return LocalDate.of(Integer.parseInt(year), Integer.parseInt(month), Integer.parseInt(day));
    }

    @Given("today is {iso8601Date}")
    public void today_is(LocalDate date) {

    }
}
``` 

### Data Table Type

Data table types can be declared by annotating a method with `@DataTableType`. Depending on the parameter type this
will be either a: 
 * `String` -> `io.cucumber.datatable.TableCellTranformer`
 * `Map<String,String>` -> `io.cucumber.datatable.TableEntry`
 * `List<String` -> `io.cucumber.datatable.TableRow`
 * `DataTable` -> `io.cucumber.datatable.TableTransformer`

```java
package com.example.app;

import io.cucumber.datatable.DataTable;
import io.cucumber.java.DataTableType;

import java.util.List;
import java.util.Map;

public class Steps {

    @DataTableType
    public Author authorEntryTransformer(Map<String, String> entry) {
        return new Author(
            entry.get("firstName"),
            entry.get("lastName"),
            entry.get("birthDate"));
    }
    
    @DataTableType
    public Author authorEntryTransformer(List<String> row) {
        return new Author(
            row.get(0),
            row.get(0),
            row.get(0));
    }
}

```

### Default Transformers

Default transformers allow you to specific a transformer that will be used when there is no transform defined. This can
be combined with an object mapper like Jackson to quickly transform well known string representations to Java objects.

 * `@DefaultParameterTransformer`
 * `@DefaultDataTableEntryTransformer`
 * `@DefaultDataTableCellTransformer`
 
 ```java
package com.example.app;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.cucumber.java.DefaultDataTableCellTransformer;
import io.cucumber.java.DefaultDataTableEntryTransformer;
import io.cucumber.java.DefaultParameterTransformer;

import java.lang.reflect.Type;

public class DataTableSteps {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @DefaultParameterTransformer
    @DefaultDataTableEntryTransformer
    @DefaultDataTableCellTransformer
    public Object defaultTransformer(Object fromValue, Type toValueType) {
        return objectMapper.convertValue(fromValue, objectMapper.constructType(toValueType));
    }
}
```