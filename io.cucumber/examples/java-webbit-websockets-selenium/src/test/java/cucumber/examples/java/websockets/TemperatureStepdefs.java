package cucumber.examples.java.websockets;

import cucumber.api.java.en.Then;
import cucumber.api.java.en.When;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;

import static org.junit.Assert.assertEquals;

public class TemperatureStepdefs {
    private final WebDriver webDriver;

    public TemperatureStepdefs(SharedDriver webDriver) {
        this.webDriver = webDriver;
    }

    @When("^I enter (.+) (celcius|fahrenheit)$")
    public void i_enter_temperature(double value, String unit) {
        webDriver.findElement(By.id(unit)).sendKeys(String.valueOf(value));
    }

    @Then("^I should see (.+) (celcius|fahrenheit)$")
    public void i_should_see_temperature(double value, String unit) {
        assertEquals(String.valueOf(value), webDriver.findElement(By.id(unit)).getAttribute("value"));
    }
}
