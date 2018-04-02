package cucumber.examples.java.websockets;

import static org.junit.Assert.assertEquals;

import cucumber.api.java8.En;
import org.openqa.selenium.By;

public class TemperatureStepdefs implements En {

    public TemperatureStepdefs(SharedDriver webDriver) {

        When("^I enter (.+) (celsius|fahrenheit)$", (Double value, String unit) ->
            webDriver.findElement(By.id(unit)).sendKeys(String.valueOf(value)));

        Then("^I should see (.+) (celsius|fahrenheit)$", (Double value, String unit) ->
            assertEquals(String.valueOf(value), webDriver.findElement(By.id(unit)).getAttribute("value")));
    }
}
