package cucumber.examples.java.websockets;

import cucumber.api.java8.En;
import org.openqa.selenium.By;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

public class NavigationStepdefs implements En {

    public NavigationStepdefs(SharedDriver webDriver) {
        Given("I am on the front page", () -> {
            webDriver.get("http://localhost:" + ServerHooks.PORT);

            // The input fields won't be enabled until the WebSocket has established
            // a connection. Wait for this to happen.
            WebDriverWait wait = new WebDriverWait(webDriver, 1);
            wait.until(ExpectedConditions.elementToBeClickable(By.id("celsius")));
        });
    }
}
