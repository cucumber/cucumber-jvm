package io.cucumber.testng;

import io.cucumber.core.gherkin.FeatureParserException;
import io.cucumber.plugin.ConcurrentEventListener;
import io.cucumber.plugin.event.Event;
import io.cucumber.plugin.event.EventPublisher;
import io.cucumber.plugin.event.TestRunFinished;
import io.cucumber.plugin.event.TestRunStarted;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static io.cucumber.core.options.Constants.PLUGIN_PROPERTY_NAME;
import static io.cucumber.testng.TestNGCucumberRunnerTest.Plugin.events;
import static java.util.Collections.singletonMap;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

final class TestNGCucumberRunnerTest {

    @BeforeEach
    void setup() {
        events.clear();
    }

    @Test
    void runCucumberTest() {
        var testNGCucumberRunner = new TestNGCucumberRunner(RunCucumberTest.class);

        for (Object[] scenario : testNGCucumberRunner.provideScenarios()) {
            PickleWrapper wrapper = (PickleWrapper) scenario[0];
            testNGCucumberRunner.runScenario(wrapper.getPickle());
        }
    }

    @Test
    void runScenarioWithUndefinedSteps() {
        var testNGCucumberRunner = new TestNGCucumberRunner(RunScenarioWithUndefinedSteps.class);
        Object[][] scenarios = testNGCucumberRunner.provideScenarios();

        // the feature file only contains one scenario
        assertThat(scenarios).hasDimensions(1, 2);
        Object[] scenario = scenarios[0];
        PickleWrapper wrapper = (PickleWrapper) scenario[0];

        assertThrows(
            UndefinedStepException.class,
            () -> testNGCucumberRunner.runScenario(wrapper.getPickle()));
    }

    @Test
    void parse_error_propagated_to_testng_test_execution() {
        FeatureParserException exception = assertThrows(FeatureParserException.class,
            () -> new TestNGCucumberRunner(ParseError.class));
        assertThat(exception).hasMessage("""
                Failed to parse resource at: classpath:io/cucumber/error/parse-error.feature
                (1:1): expected: #EOF, #Language, #TagLine, #FeatureLine, #Comment, #Empty, got 'Invalid syntax'""");
    }

    @Test
    void provideScenariosIsIdempotent() {
        var testNGCucumberRunner = new TestNGCucumberRunner(RunCucumberTestWithPlugin.class);

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
    void runWithCustomOptions() {
        Map<String, String> properties = singletonMap(
            PLUGIN_PROPERTY_NAME, "io.cucumber.testng.TestNGCucumberRunnerTest$Plugin");

        var testNGCucumberRunner = new TestNGCucumberRunner(RunCucumberTest.class, properties::get);

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
            features = "classpath:io/cucumber/undefined/undefined_steps.feature")
    static class RunScenarioWithUndefinedSteps extends AbstractTestNGCucumberTests {

    }

    @CucumberOptions
    static final class RunCucumberTest extends AbstractTestNGCucumberTests {

    }

    @CucumberOptions(plugin = "io.cucumber.testng.TestNGCucumberRunnerTest$Plugin")
    static final class RunCucumberTestWithPlugin extends AbstractTestNGCucumberTests {

    }

    public static final class Plugin implements ConcurrentEventListener {

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
