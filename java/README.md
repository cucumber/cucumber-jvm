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

Use annotations to mark methods as steps. For localized annotations import from `io.cucumber.java.<ISO2 Language Code>`


```java
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;

import static org.junit.Assert.assertEquals;

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

## TODO: Hooks
 * `@Before` 
 * `@After`
 * `@BeforeStep`
 * `@AfterStep`
 
## TODO: Transformers 
 * `@DefaultParameterTransformer`
 * `@DefaultDataTableEntryTransformer`
 * `@DefaultDataTableCellTransformer`
 