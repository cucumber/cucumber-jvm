package io.cucumber.testng;

import io.cucumber.core.gherkin.FeatureParserException;
import io.cucumber.core.options.Constants;
import io.cucumber.plugin.ConcurrentEventListener;
import io.cucumber.plugin.event.Event;
import io.cucumber.plugin.event.EventPublisher;
import io.cucumber.plugin.event.TestRunFinished;
import io.cucumber.plugin.event.TestRunStarted;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static io.cucumber.testng.TestNGCucumberRunnerTest.Plugin.events;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertThrows;

public class TestNGCucumberRunnerTest {

    private TestNGCucumberRunner testNGCucumberRunner;

    @Test
    public void runCucumberTest() {
        testNGCucumberRunner = new TestNGCucumberRunner(RunCucumberTest.class);

        for (Object[] scenario : testNGCucumberRunner.provideScenarios()) {
            PickleWrapper wrapper = (PickleWrapper) scenario[0];
            testNGCucumberRunner.runScenario(wrapper.getPickle());
        }
    }

    @Test
    public void runScenarioWithUndefinedSteps() {
        testNGCucumberRunner = new TestNGCucumberRunner(RunScenarioWithUndefinedSteps.class);
        Object[][] scenarios = testNGCucumberRunner.provideScenarios();

        // the feature file only contains one scenario
        assertEquals(scenarios.length, 1);
        Object[] scenario = scenarios[0];
        PickleWrapper wrapper = (PickleWrapper) scenario[0];

        assertThrows(
            UndefinedStepException.class,
            () -> testNGCucumberRunner.runScenario(wrapper.getPickle()));
    }

    @Test
    public void parse_error_propagated_to_testng_test_execution() {
        try {
            testNGCucumberRunner = new TestNGCucumberRunner(ParseError.class);
            Assert.fail("CucumberException not thrown");
        } catch (FeatureParserException e) {
            assertEquals(e.getMessage(),
                "Failed to parse resource at: classpath:io/cucumber/error/parse-error.feature\n" +
                        "(1:1): expected: #EOF, #Language, #TagLine, #FeatureLine, #Comment, #Empty, got 'Invalid syntax'");
        }
    }

    @Test
    public void provideScenariosIsIdempotent() {
        testNGCucumberRunner = new TestNGCucumberRunner(RunCucumberTestWithPlugin.class);

        testNGCucumberRunner.provideScenarios();
        testNGCucumberRunner.provideScenarios();
        testNGCucumberRunner.finish();

        assertEquals(1, events.stream()
                .map(Object::getClass)
                .filter(TestRunStarted.class::isAssignableFrom).count());
        assertEquals(1, events.stream()
                .map(Object::getClass)
                .filter(TestRunFinished.class::isAssignableFrom).count());
    }

    @Test
    public void runWithCustomeOptions() {

        Map<String, String> customOptions = new HashMap<String, String>();
        customOptions.put(Constants.FEATURES_PROPERTY_NAME, "classpath:io/cucumber/error/parse-error.feature");
        CucumberPropertiesProvider cucumberProperties = customOptions::get;

        try {
            testNGCucumberRunner = new TestNGCucumberRunner(RunWithCustomOptions.class, cucumberProperties);
            Assert.fail("CucumberException not thrown");
        } catch (FeatureParserException e) {
            assertEquals(e.getMessage(),
                "Failed to parse resource at: classpath:io/cucumber/error/parse-error.feature\n" +
                        "(1:1): expected: #EOF, #Language, #TagLine, #FeatureLine, #Comment, #Empty, got 'Invalid syntax'");
        }
    }

    @CucumberOptions(
            features = "classpath:io/cucumber/undefined/undefined_steps.feature")
    static class RunScenarioWithUndefinedSteps extends AbstractTestNGCucumberTests {

    }

    @CucumberOptions
    static class RunCucumberTest extends AbstractTestNGCucumberTests {

    }

    @CucumberOptions(plugin = "io.cucumber.testng.TestNGCucumberRunnerTest$Plugin")
    static class RunCucumberTestWithPlugin extends AbstractTestNGCucumberTests {

    }

    static class RunWithCustomOptions extends AbstractTestNGCucumberTests {

    }

    public static class Plugin implements ConcurrentEventListener {

        static List<Event> events = new ArrayList<>();

        @Override
        public void setEventPublisher(EventPublisher publisher) {
            publisher.registerHandlerFor(TestRunStarted.class, event -> events.add(event));
            publisher.registerHandlerFor(TestRunFinished.class, event -> events.add(event));
        }

    }

    @CucumberOptions(features = "classpath:io/cucumber/error/parse-error.feature")
    static class ParseError extends AbstractTestNGCucumberTests {

    }

}
