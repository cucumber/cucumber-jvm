package cucumber.examples.java.websockets;

import cucumber.api.java.en.Given;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

public class NavigationStepdefs {
    private final WebDriver webDriver;

    public NavigationStepdefs(SharedDriver webDriver) {
        this.webDriver = webDriver;
    }

    @Given("^I am on the front page$")
    public void i_am_on_the_front_page() throws InterruptedException {
        webDriver.get("http://localhost:" + ServerHooks.PORT);

        // The input fields won't be enabled until the WebSocket has established
        // a connection. Wait for this to happen.
        WebDriverWait wait = new WebDriverWait(webDriver, 1);
        wait.until(ExpectedConditions.elementToBeClickable(By.id("celcius")));
    }
}
