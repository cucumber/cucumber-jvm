package io.cucumber.core.runtime;

import io.cucumber.core.eventbus.EventBus;
import io.cucumber.core.feature.TestFeatureParser;
import io.cucumber.core.gherkin.Feature;
import io.cucumber.core.options.RuntimeOptions;
import io.cucumber.core.options.RuntimeOptionsBuilder;
import io.cucumber.plugin.event.Result;
import io.cucumber.plugin.event.Status;
import io.cucumber.plugin.event.TestCase;
import io.cucumber.plugin.event.TestCaseFinished;
import io.cucumber.plugin.event.TestRunFinished;
import io.cucumber.plugin.event.TestRunStarted;
import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.function.Function;
import java.util.function.Supplier;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;

class CucumberExecutionContextTest {

    private final EventBus bus = new TimeServiceEventBus(Clock.systemUTC(), UUID::randomUUID);
    private final RuntimeOptions options = new RuntimeOptionsBuilder().build();
    private final ExitStatus exitStatus = new ExitStatus(options);
    private final RuntimeException failure = new IllegalStateException("failure runner");
    private final BackendSupplier backendSupplier = new StubBackendSupplier();
    private final Supplier<ClassLoader> classLoader = CucumberExecutionContext.class::getClassLoader;
    private final ObjectFactoryServiceLoader objectFactoryServiceLoader = new ObjectFactoryServiceLoader(classLoader,
        options);
    private final ObjectFactorySupplier objectFactorySupplier = new SingletonObjectFactorySupplier(
        objectFactoryServiceLoader);
    private final RunnerSupplier runnerSupplier = new SingletonRunnerSupplier(options, bus, backendSupplier,
        objectFactorySupplier);
    private final CucumberExecutionContext context = new CucumberExecutionContext(bus, exitStatus, runnerSupplier);

    @Test
    public void collects_and_rethrows_failures_in_runner() {
        IllegalStateException thrown = assertThrows(IllegalStateException.class, () -> context.runTestCase(runner -> {
            throw failure;
        }));
        assertThat(thrown, is(failure));
        assertThat(context.getThrowable(), is(failure));
    }

    @Test
    public void rethrows_but_does_not_collect_failures_in_test_case() {
        IllegalStateException thrown = assertThrows(IllegalStateException.class, () -> context.runTestCase(runner -> {
            try (TestCaseResultObserver r = new TestCaseResultObserver(bus)) {
                bus.send(new TestCaseFinished(bus.getInstant(), mock(TestCase.class),
                    new Result(Status.FAILED, Duration.ZERO, failure)));
                r.assertTestCasePassed(
                    Exception::new,
                    Function.identity(),
                    suggestions -> new Exception(),
                    Function.identity());
            }
        }));
        assertThat(thrown, is(failure));
        assertThat(context.getThrowable(), nullValue());
    }

    @Test
    public void rethrows_event_handler_failures_emitted_while_processing_features() {
        // Reproduces #2748: a plugin that throws while handling an Envelope
        // emitted during feature processing (e.g. the GherkinDocument envelope)
        // had its exception swallowed, while the same plugin throwing on the
        // TestRunFinished envelope did not. The behavior must be consistent:
        // the exception should always be rethrown.
        Feature feature = TestFeatureParser.parse("test.feature", "" +
                "Feature: feature name\n" +
                "  Scenario: scenario name\n" +
                "    Given first step\n");

        RuntimeException failure = new IllegalStateException("handler failure");
        bus.registerHandlerFor(io.cucumber.messages.types.Envelope.class, envelope -> {
            if (envelope.getGherkinDocument().isPresent()) {
                throw failure;
            }
        });

        IllegalStateException thrown = assertThrows(IllegalStateException.class,
            () -> context.runFeatures(() -> context.beforeFeature(feature)));
        assertThat(thrown, is(failure));
    }

    @Test
    public void emits_failures_in_events() {
        List<TestRunStarted> testRunStarted = new ArrayList<>();
        List<TestRunFinished> testRunFinished = new ArrayList<>();

        bus.registerHandlerFor(TestRunStarted.class, testRunStarted::add);
        bus.registerHandlerFor(TestRunFinished.class, testRunFinished::add);

        context.startTestRun();
        assertThrows(IllegalStateException.class, () -> context.runTestCase(runner -> {
            throw failure;
        }));
        context.finishTestRun();

        assertThat(testRunStarted.get(0), notNullValue());
        Result result = testRunFinished.get(0).getResult();
        assertNotNull(result);
        assertThat(result.getStatus(), is(Status.FAILED));
        assertThat(result.getError(), is(failure));
    }

}
