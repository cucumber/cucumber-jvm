package cucumber.examples.java.websockets;

import cucumber.annotation.After;
import cucumber.annotation.Before;
import cucumber.annotation.en.Given;
import cucumber.annotation.en.Then;
import cucumber.annotation.en.When;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;

import java.io.IOException;

import static org.junit.Assert.assertEquals;

public class TemperatureStepdefs {
    private static final int PORT = 8887;

    private TemperatureServer temperatureServer;
    private WebDriver browser;

    @Before
    public void startServer() throws IOException {
        temperatureServer = new TemperatureServer(PORT);
        temperatureServer.start();
    }

    @Before
    public void startBrowser() throws Exception {
        try {
            browser = new ChromeDriver();
        } catch (Exception e) {
            System.err.println("You must have a chromedriver executable on your PATH");
            throw e;
        }
    }

    @After
    public void closeBrowser() {
        browser.close();
    }

    @After
    public void stopServer() throws IOException {
        temperatureServer.stop();
    }

    @Given("^I am on the front page$")
    public void i_am_on_the_front_page() {
        browser.get("http://localhost:" + PORT);
    }

    @When("^I enter (.+) Celcius$")
    public void i_enter_Celcius(double celcius) {
        browser.findElement(By.id("celcius")).sendKeys(String.valueOf(celcius));
    }

    @Then("^I should see (.+) Fahrenheit$")
    public void i_should_see_Fahrenheit(double fahrenheit) {
        assertEquals(String.valueOf(fahrenheit), browser.findElement(By.id("fahrenheit")).getAttribute("value"));
    }
}
