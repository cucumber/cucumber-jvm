package io.cucumber.testng;

import io.cucumber.core.gherkin.FeatureParserException;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import static org.testng.Assert.assertThrows;

public class TestNGCucumberRunnerTest {
    private TestNGCucumberRunner testNGCucumberRunner;

    @BeforeMethod
    public void setUp() {
        testNGCucumberRunner = new TestNGCucumberRunner(io.cucumber.testng.RunCucumberTest.class);
    }

    @Test
    public void runCucumberTest() throws Throwable {
        testNGCucumberRunner = new TestNGCucumberRunner(RunCucumberTest.class);

        for (Object[] scenario : testNGCucumberRunner.provideScenarios()) {
            PickleWrapper wrapper = (PickleWrapper) scenario[0];
            testNGCucumberRunner.runScenario(wrapper.getPickle());
        }
    }

    @Test
    public void runScenarioWithUndefinedStepsStrict() {
        testNGCucumberRunner = new TestNGCucumberRunner(RunScenarioWithUndefinedStepsStrict.class);
        Object[][] scenarios = testNGCucumberRunner.provideScenarios();

        // the feature file only contains one scenario
        Assert.assertEquals(scenarios.length, 1);
        Object[] scenario = scenarios[0];
        PickleWrapper wrapper = (PickleWrapper) scenario[0];

        assertThrows(
            UndefinedStepException.class,
            () -> testNGCucumberRunner.runScenario(wrapper.getPickle())
        );
    }

    @Test
    public void parse_error_propagated_to_testng_test_execution() {
        testNGCucumberRunner = new TestNGCucumberRunner(ParseError.class);
        try {
            Object[][] scenarios = testNGCucumberRunner.provideScenarios(); // a CucumberException is caught
            PickleWrapper pickleWrapper = (PickleWrapper) scenarios[0][0];
            pickleWrapper.getPickle();
            Assert.fail("CucumberException not thrown");
        } catch (FeatureParserException e) {
            Assert.assertEquals(e.getMessage(), "Failed to parse resource at: classpath:io/cucumber/error/parse-error.feature");
        }
    }

    @CucumberOptions(
        features = "classpath:io/cucumber/undefined/undefined_steps.feature",
        strict = true
    )
    static class RunScenarioWithUndefinedStepsStrict extends AbstractTestNGCucumberTests {
    }

    @CucumberOptions(strict = true)
    static class RunCucumberTest extends AbstractTestNGCucumberTests {
    }

    @CucumberOptions(features = "classpath:io/cucumber/error/parse-error.feature")
    static class ParseError extends AbstractTestNGCucumberTests {
    }
}
