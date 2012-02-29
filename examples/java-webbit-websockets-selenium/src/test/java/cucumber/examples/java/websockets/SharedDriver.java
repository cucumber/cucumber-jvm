package cucumber.examples.java.websockets;

import cucumber.annotation.After;
import cucumber.runtime.ScenarioResult;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.WebDriverException;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.support.events.EventFiringWebDriver;

import java.io.ByteArrayInputStream;

/**
 * Example of a WebDriver implementation that automatically closes at the
 * end of each scenario, and can be shared across step definitions via dependency injection.
 */
public class SharedDriver extends EventFiringWebDriver {
    public SharedDriver() {
        super(new ChromeDriver());
    }

    @After
    public void close(ScenarioResult result) {
        try {
            byte[] screenshot = this.getScreenshotAs(OutputType.BYTES);
            result.embed(new ByteArrayInputStream(screenshot), "image/png");
        } catch (WebDriverException somePlatformsDontSupportScreenshots) {
            System.err.println(somePlatformsDontSupportScreenshots.getMessage());
        } finally {
            super.close();
        }
    }
}
