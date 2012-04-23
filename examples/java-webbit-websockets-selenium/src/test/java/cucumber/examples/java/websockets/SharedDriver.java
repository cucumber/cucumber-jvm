package cucumber.examples.java.websockets;

import cucumber.annotation.After;
import cucumber.annotation.Before;
import cucumber.runtime.ScenarioResult;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebDriverException;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.support.events.EventFiringWebDriver;

import java.io.ByteArrayInputStream;

/**
 * Example of a WebDriver implementation that has an underlying instance that is used for all scenarios and closed
 * when the JVM exits. This saves time. To prevent browser state from leaking between scenarios, cookies are deleted before
 * every scenario.
 *
 * As a bonus, screenshots are embedded into the report for each scenario. (This only works
 * if you're also using the HTML formatter).
 *
 * This class can be shared across step definitions via dependency injection.
 */
public class SharedDriver extends EventFiringWebDriver {
    private static final WebDriver REAL_DRIVER = new ChromeDriver();

    static {
        Runtime.getRuntime().addShutdownHook(new Thread() {
            @Override
            public void run() {
                REAL_DRIVER.close();
            }
        });
    }

    public SharedDriver() {
        super(REAL_DRIVER);
    }

    @Before
    public void deleteAllCookies() {
        manage().deleteAllCookies();
    }

    @After
    public void embedScreenshot(ScenarioResult result) {
        try {
            byte[] screenshot = this.getScreenshotAs(OutputType.BYTES);
            result.embed(new ByteArrayInputStream(screenshot), "image/png");
        } catch (WebDriverException somePlatformsDontSupportScreenshots) {
            System.err.println(somePlatformsDontSupportScreenshots.getMessage());
        }
    }
}
