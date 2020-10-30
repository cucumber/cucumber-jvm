Cucumber Java
=============

Provides annotation based step definitions. To use, add the `cucumber-java` dependency to your pom.xml:

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

Declare a step definition by annotating a method. It is possible use the same
method for multiple steps by repeating the annotation. For localized annotations
import the annotations from `io.cucumber.java.<ISO2 Language Code>.*`

Step definitions can take either a
[Cucumber Expression](https://github.com/cucumber/cucumber/tree/master/cucumber-expressions) or a regular
expression.

```java
package com.example.app;

import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class CalculatorStepDefinitions {
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

### Data tables

Data tables from Gherkin can be accessed by using the `DataTable` object as the last parameter in a step definition.
Depending on the table shape, it can also be accessed as one of the following collections:
 * `List<List<String>> table`
 * `List<Map<String, String>> table`
 * `Map<String, String> table`
 * `Map<String, List<String>> table`
 * `Map<String, Map<String, String>> table`
 
For examples of each type see: [cucumber/datatable](https://github.com/cucumber/cucumber/tree/master/datatable)

```java
package com.example.app;

import io.cucumber.datatable.DataTable;
import io.cucumber.java.en.Given;

public class StepDefinitions {
    
    @Given("a datatable:")
    public void a_data_table(DataTable table){
        
    }
    
    @Given("a datatable as a list of maps:")
    public void a_data_table(List<Map<String, String>> table){
        
    }

    @Given("a datatable as a list of maps:")
    public void a_data_table(Map<String, Map<String, Double>> table){
        
    }
}
```

Note: In addition to collections of `String`, `Integer`, `Float`, `BigInteger` and `BigDecimal`, `Byte`, `Short`, `Long`
and `Double` are also supported. Numbers are parsed using the language of the feature file.

### Doc strings

Doc strings from Gherkin can be accessed by using the `DocString` object as a
parameter.

```java
package com.example.app;

import io.cucumber.docstring.DocString;
import io.cucumber.java.en.Given;

public class StepDefinitions {
    
    @Given("a docstring:")
    public void a_data_table(DocString docString){
        
    }
}
```

## Hooks

Declare hooks that will be executed before/after each scenario/step by
annotating a method. The method may declare an argument of type 
`io.cucumber.java.Scenario`.

 * `@Before` 
 * `@After`
 * `@BeforeStep`
 * `@AfterStep`
 
## Transformers 

Cucumber expression parameters, data tables and docs strings can be transformed
into arbitrary java objects.

### Parameter Type

Parameter types used by Cucumber expressions can be declared by using
`@ParameterType`. The name of the annotated method will be used as the parameter
name.

```java
package com.example.app;

import io.cucumber.java.ParameterType;
import io.cucumber.java.en.Given;

import java.time.LocalDate;

public class StepDefinitions {

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

Using a custom data table type will allow you to convert a table declaring values for fields in an object to a List of
that object.

For example, a list of authors:

```feature
    Given a list of authors in a table
      | firstName   | lastName | birthDate  |
      | Annie M. G. | Schmidt  | 1911-03-20 |
      | Roald       | Dahl     | 1916-09-13 |
```

```java
package com.example.app;

import io.cucumber.datatable.DataTable;
import io.cucumber.java.DataTableType;

import java.util.List;
import java.util.Map;

public class StepDefinitions {

    @DataTableType
    public Author authorEntryTransformer(Map<String, String> entry) {
        return new Author(
            entry.get("firstName"),
            entry.get("lastName"),
            entry.get("birthDate"));
    }

    @Given("a list of authors in a table")
    public void aListOfAuthorsInATable(List<Author> authors) {
        
    }
}
```

Data table types can be declared by annotating a method with `@DataTableType`. 
Depending on the parameter type this will be either a: 
 * `String` -> `io.cucumber.datatable.TableCellTranformer`
 * `Map<String,String>` -> `io.cucumber.datatable.TableEntryTransformer`
 * `List<String` -> `io.cucumber.datatable.TableRowTranformer`
 * `DataTable` -> `io.cucumber.datatable.TableTransformer`

For a full list of transformations that can be achieved with data table types
see [cucumber/datatable](https://github.com/cucumber/cucumber/tree/master/datatable)

### Default Transformers

Default transformers allow you to specify a transformer that will be used when there is no transformer defined. This can
be combined with an object mapper like Jackson to quickly transform well-known string representations to Java objects.

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

public class DataTableStepDefinitions {

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

Data tables in Gherkin cannot represent null or an empty string unambiguously. Cucumber will interpret empty cells as
`null`.

The empty string can be represented using a replacement. For example `[empty]`.
The replacement can be configured by setting the `replaceWithEmptyString`
property of `DataTableType`, `DefaultDataTableCellTransformer` and 
`DefaultDataTableEntryTransformer`. By default, no replacement is configured. 

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

public class DataTableStepDefinitions {

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

To make use of replacements when converting a data table directly to a list or map of strings, the data table type for
String has to be overridden.

```gherkin
Feature: Whitespace
  Scenario: Whitespace in a table
   Given a blank value
     | key | value   |
     | a   | [blank] | 
```

```java
package com.example.app;

import io.cucumber.java.DataTableType;
import io.cucumber.java.en.Given;

import java.util.Map;
import java.util.List;

public class DataTableStepDefinitions {

    @DataTableType(replaceWithEmptyString = "[blank]")
    public String listOfStringListsType(String cell) {
        return cell;
    }

    @Given("A blank value")
    public void given_a_blank_value(Map<String, String> map){
        // map contains { "key":"a", "value": ""}
    }
}
```

### Transposing Tables

A data table can be transposed by annotating the data table parameter (or the
parameter the data table will be converted into) with `@Transpose`. This means
the keys will be in the first column rather than the first row.

For example, a table with the fields for a User and a data table type to create a User:

```gherkin
 Given the user is
    | firstname	    | Roberto	|
    | lastname	    | Lo Giacco |
    | nationality	| Italian	|
 ```

```java 
package com.example.app;

import io.cucumber.java.DataTableType;
import io.cucumber.java.en.Given;
import io.cucumber.java.Transpose;

import java.util.Map;
import java.util.List;

public class DataTableStepDefinitions {

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

## DocString type

Using `@DocStringType` annotation, it is possible to define transformations to other object types.

```gherkin
Given some more information
  """json
  { 
     "produce": "Cucumbers",
     "weight": "5 Kilo", 
     "price": "1€/Kilo"
  }
  """
```

```java
package com.example;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import io.cucumber.java.DocStringType;
import io.cucumber.java.en.Given;

public class StepDefinitions {

    @DocStringType
    public JsonNode json(String docString) throws IOException {
        return objectMapper.readTree(docString);
    }
        
    @Given("some more information")
    public void some_more_information(JsonNode json){
    
    }
}
```
