package cucumber.examples.java.websockets;

import cucumber.annotation.After;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.support.events.EventFiringWebDriver;

/**
 * Example of a WebDriver implementation that automatically closes at the
 * end of each scenario, and can be shared across step definitions via dependency injection.
 */
public class SharedDriver extends EventFiringWebDriver {
    public SharedDriver() {
        super(new ChromeDriver());
    }

    @After
    public void close() {
        super.close();
    }
}
