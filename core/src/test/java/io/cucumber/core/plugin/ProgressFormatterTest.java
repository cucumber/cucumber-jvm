package io.cucumber.core.plugin;

import io.cucumber.core.eventbus.EventBus;
import io.cucumber.core.runtime.TimeServiceEventBus;
import io.cucumber.plugin.event.HookTestStep;
import io.cucumber.plugin.event.PickleStepTestStep;
import io.cucumber.plugin.event.Result;
import io.cucumber.plugin.event.TestCase;
import io.cucumber.plugin.event.TestRunFinished;
import io.cucumber.plugin.event.TestRunStarted;
import io.cucumber.plugin.event.TestStepFinished;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.UUID;

import static io.cucumber.core.plugin.BytesEqualTo.isBytesEqualTo;
import static io.cucumber.plugin.event.Status.FAILED;
import static io.cucumber.plugin.event.Status.PASSED;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.mock;

class ProgressFormatterTest {

    final EventBus bus = new TimeServiceEventBus(Clock.systemUTC(), UUID::randomUUID);
    final ByteArrayOutputStream out = new ByteArrayOutputStream();
    final ProgressFormatter formatter = new ProgressFormatter(out);

    @BeforeEach
    void setup() {
        formatter.setEventPublisher(bus);
    }

    @Test
    void prints_empty_line_for_empty_test_run() {
        bus.send(new TestRunStarted(Instant.now()));
        Result runResult = new Result(PASSED, Duration.ZERO, null);
        bus.send(new TestRunFinished(Instant.now(), runResult));
        assertThat(out, isBytesEqualTo("\n"));
    }

    @Test
    void print_green_dot_for_passing_scenario() {
        bus.send(new TestRunStarted(Instant.now()));
        Result stepResult = new Result(PASSED, Duration.ZERO, null);
        bus.send(new TestStepFinished(Instant.now(), mock(TestCase.class), mock(PickleStepTestStep.class), stepResult));
        Result hookResult = new Result(PASSED, Duration.ZERO, null);
        bus.send(new TestStepFinished(Instant.now(), mock(TestCase.class), mock(HookTestStep.class), hookResult));
        Result runResult = new Result(PASSED, Duration.ZERO, null);
        bus.send(new TestRunFinished(Instant.now(), runResult));
        assertThat(out, isBytesEqualTo(AnsiEscapes.GREEN + "." + AnsiEscapes.RESET + "\n"));
    }

    @Test
    void print_red_F_for_failed_scenario() {
        bus.send(new TestRunStarted(Instant.now()));
        Result stepResult = new Result(FAILED, Duration.ZERO, null);
        bus.send(new TestStepFinished(Instant.now(), mock(TestCase.class), mock(PickleStepTestStep.class), stepResult));
        Result hookResult = new Result(PASSED, Duration.ZERO, null);
        bus.send(new TestStepFinished(Instant.now(), mock(TestCase.class), mock(HookTestStep.class), hookResult));
        Result runResult = new Result(FAILED, Duration.ZERO, null);
        bus.send(new TestRunFinished(Instant.now(), runResult));
        assertThat(out, isBytesEqualTo(AnsiEscapes.RED + "F" + AnsiEscapes.RESET + "\n"));
    }

    @Test
    void print_red_F_for_failed_hook() {
        bus.send(new TestRunStarted(Instant.now()));
        Result stepResult = new Result(PASSED, Duration.ZERO, null);
        bus.send(new TestStepFinished(Instant.now(), mock(TestCase.class), mock(PickleStepTestStep.class), stepResult));
        Result hookResult = new Result(FAILED, Duration.ZERO, null);
        bus.send(new TestStepFinished(Instant.now(), mock(TestCase.class), mock(HookTestStep.class), hookResult));
        Result runResult = new Result(FAILED, Duration.ZERO, null);
        bus.send(new TestRunFinished(Instant.now(), runResult));
        assertThat(out, isBytesEqualTo(AnsiEscapes.GREEN + "." + AnsiEscapes.RESET + AnsiEscapes.RED + "F" + AnsiEscapes.RESET + "\n"));
    }

}
