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
argument of type `io.cucumber.java.Scenario`.

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

Data table types can be declared by annotating a method with `@DataTableType`. 
Depending on the parameter type this will be either a: 
 * `String` -> `io.cucumber.datatable.TableCellTranformer`
 * `Map<String,String>` -> `io.cucumber.datatable.TableEntryTransformer`
 * `List<String` -> `io.cucumber.datatable.TableRowTranformer`
 * `DataTable` -> `io.cucumber.datatable.TableTransformer`

For a full list of transformations that can be achieved with data table types
see [cucumber/datatable](https://github.com/cucumber/cucumber/tree/master/datatable)

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

### Empty Cells

Data tables in Gherkin can not represent null or the empty string unambiguously.
Cucumber will interpret empty cells as `null`.

Empty string be represented using a replacement. For example `[empty]`.
The replacement can be configured by setting the `replaceWithEmptyString`
property of `DataTableType`, `DefaultDataTableCellTransformer` and 
`DefaultDataTableEntryTransformer`. By default no replacement is configured. 

```gherkin
Given some authors
   | name            | first publication |
   | Aspiring Author |                   |
   | Ancient Author  | [blank]           |
```

```java
package com.example.app;

import io.cucumber.java.DataTableType;
import io.cucumber.java.en.Given;

import java.util.Map;
import java.util.List;

public class DataTableSteps {

    @DataTableType(replaceWithEmptyString = "[blank]")
    public Author convert(Map<String, String> entry){
      return new Author(
         entry.get("name"),
         entry.get("first publication")
      );
    }
    
    @Given("some authors")
    public void given_some_authors(List<Author> authors){
      // authors = [Author(name="Aspiring Author", firstPublication=null), Author(name="Ancient Author", firstPublication=)]
    }
}
```

# Transposing Tables

A data table can be transposed by annotating the data table parameter (or the
parameter the data table will be converted into) with `@Transpose`. This means
the keys will be in the first column rather then the first row.


```gherkin
 Given the user is
    | firstname	    | Roberto	|
    | lastname	    | Lo Giacco |
    | nationality	| Italian	|
 ```

And a data table type to create a User

```java 
package com.example.app;

import io.cucumber.java.DataTableType;
import io.cucumber.java.en.Given;
import io.cucumber.java.Transpose;

import java.util.Map;
import java.util.List;

public class DataTableSteps {

    @DataTableType
    public User convert(Map<String, String> entry){
      return new User(
         entry.get("firstname"),
         entry.get("lastname")
         entry.get("nationality")
      );
    }
    
    @Given("the user is")
    public void the_user_is(@Transpose User user){
      // user  = [User(firstname="Roberto", lastname="Lo Giacco", nationality="Italian")
    }
}
```
