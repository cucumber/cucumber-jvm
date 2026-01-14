package io.cucumber.testng;

import org.apiguardian.api.API;
import org.jspecify.annotations.Nullable;
import org.testng.ITestContext;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import org.testng.xml.XmlTest;

/**
 * Abstract TestNG Cucumber Test
 * <p>
 * Runs each cucumber scenario found in the features as separated test.
 *
 * @see TestNGCucumberRunner
 */
@API(status = API.Status.STABLE)
public abstract class AbstractTestNGCucumberTests {

    private @Nullable TestNGCucumberRunner testNGCucumberRunner;

    /**
     * Starts the test run.
     * <p>
     * Sublcasses may override this method, but must invoke it.
     */

    @BeforeClass(alwaysRun = true)
    public void setUpClass(ITestContext context) {
        XmlTest currentXmlTest = context.getCurrentXmlTest();
        CucumberPropertiesProvider properties = currentXmlTest::getParameter;
        testNGCucumberRunner = new TestNGCucumberRunner(this.getClass(), properties);
    }

    /**
     * Runs a given pickle from given feature.
     * <p>
     * Sublcasses may override this method, but must invoke it.
     */
    @SuppressWarnings("unused")
    @Test(groups = "cucumber", description = "Runs Cucumber Scenarios", dataProvider = "scenarios")
    public void runScenario(PickleWrapper pickleWrapper, FeatureWrapper featureWrapper) {
        if (testNGCucumberRunner == null) {
            throw new IllegalStateException(
                "Tests were started without calling AbstractTestNGCucumberTests::setUpClass");
        }

        // the 'featureWrapper' parameter solely exists to display the feature
        // file in a test report
        testNGCucumberRunner.runScenario(pickleWrapper.getPickle());
    }

    /**
     * Returns two dimensional array of {@link PickleWrapper}s with their
     * associated {@link FeatureWrapper}s.
     * <p>
     * Sublcasses may override this method, but must invoke it.
     *
     * @return a two dimensional array of scenarios features.
     */
    @DataProvider
    public Object[][] scenarios() {
        if (testNGCucumberRunner == null) {
            return new Object[0][0];
        }
        return testNGCucumberRunner.provideScenarios();
    }

    /**
     * Finishes the test run.
     * <p>
     * Sublcasses may override this method, but must invoke it.
     */
    @AfterClass(alwaysRun = true)
    public void tearDownClass() {
        if (testNGCucumberRunner == null) {
            return;
        }
        testNGCucumberRunner.finish();
    }

}
