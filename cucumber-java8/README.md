Cucumber Java8
==============

Provides lambda based step definitions. To use add the `cucumber-java8` dependency to your pom.xml:

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

Data tables and Docstrings from Gherkin can be accessed by using a `DataTable`
or `DocString` object as the last parameter.

```java
package com.example.app;

import io.cucumber.java8.En;
import io.cucumber.datatable.DataTable;
import io.cucumber.docstring.DocString;

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

        Given("the previous entries:", (DataTable dataTable) -> {
            List<Entry> entries = dataTable.asList(Entry.class);
            ...
        });

        Then("the calculation log displays:", (DocString docString) -> {
            ...
        });
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

    public StepDefinitions() {
        ParameterType("amount", "(\\d+\\.\\d+)\\s([a-zA-Z]+)", (String[] values) ->
            new Amount(new BigDecimal(values[0]), Currency.getInstance(values[1])));
    }
}
``` 

### Data Table Type

Data table types can be declared by calling `DataTableType` in the constructor.
Depending on the lambda type this will be either a: 
 * `String` -> `io.cucumber.datatable.TableCellTranformer`
 * `Map<String,String>` -> `io.cucumber.datatable.TableEntry`
 * `List<String>` -> `io.cucumber.datatable.TableRow`
 * `DataTable` -> `io.cucumber.datatable.TableTransformer`

```java
package com.example.app;

import io.cucumber.java8.En;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class StepDefinitions implements En {

    public StepDefinitions() {
        DataTableType((Map<String, String> row) -> new Grocery(
            row.get("name"),
            Price.fromString(row.get("price"))
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
 
For a full list of transformations that can be achieved with data table types
see [cucumber/datatable](https://github.com/cucumber/cucumber/tree/master/datatable)
 
```java
package com.example.app;

import com.fasterxml.jackson.databind.ObjectMapper;

import io.cucumber.java8.En;

public class StepDefinitions implements En {

    public StepDefinitions() {
        final ObjectMapper objectMapper = new ObjectMapper();

        DefaultParameterTransformer((fromValue, toValueType) ->
            objectMapper.convertValue(fromValue, objectMapper.constructType(toValueType))
        );
    }
}
``` 

### Empty Cells

Data tables in Gherkin can not represent null or the empty string unambiguously.
Cucumber will interpret empty cells as `null`.

Empty string be represented using a replacement. For example `[empty]`.
The replacement can be configured providing the `replaceWithEmptyString`
argument of `DataTableType`, `DefaultDataTableCellTransformer` and 
`DefaultDataTableEntryTransformer`. By default no replacement is configured. 

```gherkin
Given some authors
   | name            | first publication |
   | Aspiring Author |                   |
   | Ancient Author  | [blank]           |
```

```java
package com.example.app;

import io.cucumber.datatable.DataTable;

import io.cucumber.java8.En;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class StepDefinitions implements En {
    
    public StepDefinitions() {
        DataTableType("[blank]", (Map<String, String> entry) -> new Author(
            entry.get("name"),
            entry.get("first publication")
        ));
    
        Given("some authors",  (DataTable authorsTable) -> {
            List<Author> authors = authorsTable.asList(Author.class);
          // authors = [Author(name="Aspiring Author", firstPublication=null), Author(name="Ancient Author", firstPublication=)]

        });
    }
}
```

# Transposing Tables

A data table can be transposed by calling `.transpose()`. This means the keys
will be in the first column rather then the first row.

```gherkin
 Given the user is
    | firstname	    | Roberto	|
    | lastname	    | Lo Giacco |
    | nationality	| Italian	|
 ```

And a data table type to create a User

```java 
package com.example.app;

import io.cucumber.datatable.DataTable;

import io.cucumber.java8.En;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class StepDefinitions implements En {
    
    public StepDefinitions() {
        DataTableType((Map<String, String> entry) -> new User(
            entry.get("firstname"),
            entry.get("lastname")
            entry.get("nationality")
        ));
    
        Given("the user is",  (DataTable authorsTable) -> {
            User user = authorsTable.transpose().asList(User.class);
            // user  = User(firstname="Roberto", lastname="Lo Giacco", nationality="Italian")
        });
    }
}
```
