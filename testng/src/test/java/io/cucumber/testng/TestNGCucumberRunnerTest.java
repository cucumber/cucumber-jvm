package io.cucumber.testng;

import cucumber.api.CucumberOptions;
import cucumber.runtime.CucumberException;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class TestNGCucumberRunnerTest {
    private TestNGCucumberRunner testNGCucumberRunner;

    @BeforeMethod
    public void setUp() {
        testNGCucumberRunner = new TestNGCucumberRunner(RunCukesTest.class);
    }

    @Test(expectedExceptions = CucumberException.class)
    public void runCukesStrict() throws Throwable {
        testNGCucumberRunner = new TestNGCucumberRunner(RunCukesStrict.class);

        for (Object[] scenario : testNGCucumberRunner.provideScenarios()) {
            PickleEventWrapper pickleEvent = (PickleEventWrapper) scenario[0];
            testNGCucumberRunner.runScenario(pickleEvent.getPickleEvent());
        }
    }

    @Test(expectedExceptions = CucumberException.class)
    public void runScenarioWithUndefinedStepsStrict() throws Throwable {
        testNGCucumberRunner = new TestNGCucumberRunner(RunScenarioWithUndefinedStepsStrict.class);
        Object[][] scenarios = testNGCucumberRunner.provideScenarios();

        // the feature file only contains one scenario
        Assert.assertEquals(scenarios.length, 1);
        Object[] scenario = scenarios[0];
        PickleEventWrapper pickleEvent = (PickleEventWrapper) scenario[0];

        testNGCucumberRunner.runScenario(pickleEvent.getPickleEvent()); // runScenario() throws CucumberException
    }

    @Test
    public void parse_error_propagated_to_testng_test_execution() throws Exception {
        testNGCucumberRunner = new TestNGCucumberRunner(ParseError.class);
        Object[][] scenarios = testNGCucumberRunner.provideScenarios(); // a CucumberException is caught
        try {
            ((PickleEventWrapper) scenarios[0][0]).getPickleEvent();
            Assert.fail("CucumberException not thrown");
        } catch (CucumberException e) {
            Assert.assertEquals(e.getMessage(), "Failed to parse resource at: classpath:io/cucumber/error/parse-error.feature");
        }
    }

    @CucumberOptions(
        features = "classpath:io/cucumber/testng/undefined_steps.feature",
        strict = true
    )
    static class RunScenarioWithUndefinedStepsStrict extends AbstractTestNGCucumberTests {
    }

    @CucumberOptions(strict = true)
    static class RunCukesStrict extends AbstractTestNGCucumberTests {
    }

    @CucumberOptions(features = "classpath:io/cucumber/error/parse-error.feature")
    static class ParseError extends AbstractTestNGCucumberTests {
    }
}
