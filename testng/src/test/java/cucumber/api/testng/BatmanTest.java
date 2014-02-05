package cucumber.api.testng;

import cucumber.api.CucumberOptions;
import org.testng.annotations.Test;

/**
 * This test is intentionally disabled, as it serves only as a part of {@link cucumber.api.testng.TestNGCucumberRunnerTest}
 *
 * @see cucumber.api.testng.TestNGCucumberRunnerTest
 */
@CucumberOptions(features = "src/test/resources/cucumber/api/testng/batman")
@Test(groups = "batman", enabled = false)
public class BatmanTest extends AbstractTestNGCucumberTests {
}
