package io.cucumber.core.runtime;

import io.cucumber.core.eventbus.EventBus;
import io.cucumber.core.options.RuntimeOptionsBuilder;
import io.cucumber.core.plugin.Options;
import io.cucumber.plugin.event.Result;
import io.cucumber.plugin.event.Status;
import io.cucumber.plugin.event.TestRunFinished;
import io.cucumber.plugin.event.TestRunStarted;
import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

class CucumberExecutionContextTest {

    private final EventBus bus = new TimeServiceEventBus(Clock.systemUTC(), UUID::randomUUID);
    private final Options options = new RuntimeOptionsBuilder().build();
    private final ExitStatus exitStatus = new ExitStatus(options);

    @Test
    public void collects_and_rethrows_failures_in_runner() {
        RunnerSupplier runnerSupplier = () -> null;
        IllegalStateException failure = new IllegalStateException("failure runner");
        CucumberExecutionContext context = new CucumberExecutionContext(bus, exitStatus, runnerSupplier);
        IllegalStateException thrown = assertThrows(IllegalStateException.class, () ->
            context.runTestCase(runner -> {
                throw failure;
            }));
        assertThat(thrown, is(failure));
        assertThat(context.getException().getCause(), is(failure));
    }

    @Test
    public void emits_failures_in_events() {
        List<TestRunStarted> testRunStarted = new ArrayList<>();
        List<TestRunFinished> testRunFinished = new ArrayList<>();

        bus.registerHandlerFor(TestRunStarted.class, testRunStarted::add);
        bus.registerHandlerFor(TestRunFinished.class, testRunFinished::add);

        RunnerSupplier runnerSupplier = () -> null;
        IllegalStateException failure = new IllegalStateException("failure runner");
        CucumberExecutionContext context = new CucumberExecutionContext(bus, exitStatus, runnerSupplier);

        context.startTestRun();
        assertThrows(IllegalStateException.class, () ->
            context.runTestCase(runner -> {
                throw failure;
            }));
        context.finishTestRun();

        assertThat(testRunStarted.get(0), notNullValue());
        Result result = testRunFinished.get(0).getResult();
        assertThat(result.getStatus(), is(Status.FAILED));
        assertThat(result.getError().getCause(), is(failure));
    }

}
