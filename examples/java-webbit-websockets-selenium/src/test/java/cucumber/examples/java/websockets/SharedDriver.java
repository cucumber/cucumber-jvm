package cucumber.examples.java.websockets;

import cucumber.annotation.After;
import cucumber.annotation.Before;
import cucumber.runtime.ScenarioResult;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebDriverException;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.support.events.EventFiringWebDriver;

/**
 * Example of a WebDriver implementation that has an underlying instance that is used for all scenarios and only closed
 * when the JVM exits. This makes scenarios a lot faster since opening and closing a browser for each scenario is pretty slow.
 * To prevent browser state from leaking between scenarios, cookies are deleted before every scenario.
 * <p/>
 * As a bonus, screenshots are embedded into the report for each scenario. (This only works
 * if you're also using the HTML formatter).
 * <p/>
 * This class can be shared across step definitions via dependency injection.
 */
public class SharedDriver extends EventFiringWebDriver {
    private static final WebDriver REAL_DRIVER = new ChromeDriver();
    private static final Thread CLOSE_THREAD = new Thread() {
        @Override
        public void run() {
            REAL_DRIVER.close();
        }
    };

    static {
        Runtime.getRuntime().addShutdownHook(CLOSE_THREAD);
    }

    public SharedDriver() {
        super(REAL_DRIVER);
    }

    @Override
    public void close() {
        if(Thread.currentThread() != CLOSE_THREAD) {
            throw new UnsupportedOperationException("You shouldn't close this WebDriver. It's shared and will close when the JVM exits.");
        }
        super.close();
    }

    @Before
    public void deleteAllCookies() {
        manage().deleteAllCookies();
    }

    @After
    public void embedScreenshot(ScenarioResult result) {
        try {
            byte[] screenshot = getScreenshotAs(OutputType.BYTES);
            result.embed(screenshot, "image/png");
        } catch (WebDriverException somePlatformsDontSupportScreenshots) {
            System.err.println(somePlatformsDontSupportScreenshots.getMessage());
        }
    }
}
