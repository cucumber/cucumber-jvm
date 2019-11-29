Cucumber Java8
==============

Provides annotation based step definitions. To use add the `cucumber-java` dependency to your pom.xml:

```xml
<dependencies>
  [...]
    <dependency>
        <groupId>io.cucumber</groupId>
        <artifactId>cucumber-java8</artifactId>
        <version>${cucumber.version}</version>
        <scope>test</scope>
    </dependency>
  [...]
</dependencies>
```

## Step Definitions

Declare a step definition calling a method in the constructor of the glue class.
For localized methods import the interface from `io.cucumber.java8.<ISO2 Language Code>`

```java
package com.example.app;

import io.cucumber.java8.En;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class StepDefinitions implements En {

    private RpnCalculator calc;

    public RpnCalculatorSteps() {
        Given("a calculator I just turned on", () -> {
            calc = new RpnCalculator();
        });

        When("I add {int} and {int}", (Integer arg1, Integer arg2) -> {
            calc.push(arg1);
            calc.push(arg2);
            calc.push("+");
        });

        Then("the result is {double}", (Double expected) -> assertEquals(expected, calc.value()));
    }
}
```

## Hooks

Declare hooks that will be executed before/after each scenario/step by calling a
method in the constructor. The method may declare an argument of type `io.cucumber.java8.Scenario`.

 * `Before` 
 * `After`
 * `BeforeStep`
 * `AfterStep`
 
## Transformers 

### Parameter Type

Step definition parameter types can be declared by using `ParameterType`.

```java
package com.example.app;

import io.cucumber.java8.En;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class StepDefinitions implements En {

    public RpnCalculatorSteps() {
        ParameterType("amount", "(\\d+\\.\\d+)\\s([a-zA-Z]+)", (String[] values) -> {
            return new Amount(new BigDecimal(values[0]), Currency.getInstance(values[1]));
        });
    }
}
``` 

### Data Table Type

Data table types can be declared by calling `DataTableType` in the constructor.
Depending on the lambda type this will be either a: 
 * `String` -> `io.cucumber.datatable.TableCellTranformer`
 * `Map<String,String>` -> `io.cucumber.datatable.TableEntry`
 * `List<String` -> `io.cucumber.datatable.TableRow`
 * `DataTable` -> `io.cucumber.datatable.TableTransformer`

```java
package com.example.app;

import io.cucumber.java8.En;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class StepDefinitions implements En {

    public RpnCalculatorSteps() {
        DataTableType((Map<String, String> row) -> new ShoppingSteps.Grocery(
            row.get("name"),
            ShoppingSteps.Price.fromString(row.get("price"))
        ));
    }
}
``` 

### Default Transformers

Default transformers allow you to specific a transformer that will be used when
there is no transform defined. This can be combined with an object mapper like
Jackson to quickly transform well known string representations to Java objects.

 * `DefaultParameterTransformer`
 * `DefaultDataTableEntryTransformer`
 * `DefaultDataTableCellTransformer`
 
```java
package com.example.app;

import com.fasterxml.jackson.databind.ObjectMapper;

import io.cucumber.java8.En;

public class StepDefinitions implements En {

    public RpnCalculatorSteps() {
        final ObjectMapper objectMapper = new ObjectMapper();

        DefaultParameterTransformer((fromValue, toValueType) ->
            objectMapper.convertValue(fromValue, objectMapper.constructType(toValueType))
        );
    }
}
``` 