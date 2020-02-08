package io.cucumber.testng;

import io.cucumber.core.gherkin.FeatureParserException;
import io.cucumber.plugin.ConcurrentEventListener;
import io.cucumber.plugin.event.Event;
import io.cucumber.plugin.event.EventPublisher;
import io.cucumber.plugin.event.TestRunFinished;
import io.cucumber.plugin.event.TestRunStarted;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.List;

import static io.cucumber.testng.TestNGCucumberRunnerTest.Plugin.events;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertThrows;

public class TestNGCucumberRunnerTest {

    private TestNGCucumberRunner testNGCucumberRunner;

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
        assertEquals(scenarios.length, 1);
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
            assertEquals(e.getMessage(), "Failed to parse resource at: classpath:io/cucumber/error/parse-error.feature");
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

    @CucumberOptions(
        features = "classpath:io/cucumber/undefined/undefined_steps.feature",
        strict = true
    )
    static class RunScenarioWithUndefinedStepsStrict extends AbstractTestNGCucumberTests {
    }

    @CucumberOptions(strict = true)
    static class RunCucumberTest extends AbstractTestNGCucumberTests {
    }

    @CucumberOptions(strict = true, plugin = "io.cucumber.testng.TestNGCucumberRunnerTest$Plugin")
    static class RunCucumberTestWithPlugin extends AbstractTestNGCucumberTests {
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
