package cucumber.api.testng;

import org.testng.Assert;
import org.testng.TestListenerAdapter;
import org.testng.TestNG;
import org.testng.annotations.Test;

import cucumber.api.CucumberOptions;

public final class AbstractTestNGCucumberTestsTest {

    @Test
    public void failOnParsingError() throws Exception {
        TestListenerAdapter tla = runTestNG(FailOnUnparseableFeaturesTest.class);
        Assert.assertEquals(tla.getFailedTests().size(), 1, "Dataprovider must fail.");
        Assert.assertEquals(tla.getPassedTests().size(), 0, "No test has passed.");
        Assert.assertEquals(tla.getSkippedTests().size(), 1, "Actual test has been skipped.");
    }

    @Test
    public void failOnParsingErrorIfStrictMode() throws Exception {
        TestListenerAdapter tla = runTestNG(FailOnUnparseableFeaturesIfStrictModeTest.class);
        Assert.assertEquals(tla.getFailedTests().size(), 1, "Dataprovider must fail.");
        Assert.assertEquals(tla.getPassedTests().size(), 0, "No test has passed.");
        Assert.assertEquals(tla.getSkippedTests().size(), 1, "Actual test has been skipped.");
    }

    @Test
    public void succeedThoughParsingError() throws Exception {
        TestListenerAdapter tla = runTestNG(SucceedOnUnparseableFeaturesTest.class);
        Assert.assertEquals(tla.getFailedTests().size(), 0, "Dataprovider must not fail.");
        Assert.assertEquals(tla.getPassedTests().size(), 1, "Dataprovider test must pass.");
        Assert.assertEquals(tla.getSkippedTests().size(), 1, "Actual test has been skipped.");
    }

    private TestListenerAdapter runTestNG(final Class<? extends AbstractTestNGCucumberTests> testClass) {
        TestNG testng = new TestNG();
        testng.setGroups("cucumber");
        TestListenerAdapter tla = new TestListenerAdapter();
        testng.setTestClasses(new Class[] { testClass });
        testng.addListener(tla);
        testng.run();
        return tla;
    }

    @CucumberOptions(features = { "cucumber/api/testng/unparseable.feature" })
    private static final class FailOnUnparseableFeaturesTest extends AbstractTestNGCucumberTests {
        public FailOnUnparseableFeaturesTest() {
            setFailOnUnparseableFeatures(true);
        }
    }

    @CucumberOptions(features = { "cucumber/api/testng/unparseable.feature" }, strict = true)
    private static final class FailOnUnparseableFeaturesIfStrictModeTest extends AbstractTestNGCucumberTests {
    }

    @CucumberOptions(features = { "cucumber/api/testng/unparseable.feature" })
    private static final class SucceedOnUnparseableFeaturesTest extends AbstractTestNGCucumberTests {
    }
}
