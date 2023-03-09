package io.cucumber.core.plugin;

import io.cucumber.core.eventbus.EventBus;
import io.cucumber.core.runtime.TimeServiceEventBus;
import io.cucumber.plugin.event.HookTestStep;
import io.cucumber.plugin.event.PickleStepTestStep;
import io.cucumber.plugin.event.Result;
import io.cucumber.plugin.event.TestCase;
import io.cucumber.plugin.event.TestRunFinished;
import io.cucumber.plugin.event.TestStepFinished;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.UUID;

import static io.cucumber.core.plugin.Bytes.bytes;
import static io.cucumber.plugin.event.Status.FAILED;
import static io.cucumber.plugin.event.Status.PASSED;
import static io.cucumber.plugin.event.Status.UNDEFINED;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.text.IsEqualCompressingWhiteSpace.equalToCompressingWhiteSpace;
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
        Result result = new Result(PASSED, Duration.ZERO, null);
        bus.send(new TestRunFinished(Instant.now(), result));
        assertThat(out, bytes(equalToCompressingWhiteSpace("\n")));
    }

    @Test
    void prints_empty_line_and_green_dot_for_passing_test_run() {
        Result result = new Result(PASSED, Duration.ZERO, null);
        bus.send(new TestStepFinished(Instant.now(), mock(TestCase.class), mock(PickleStepTestStep.class), result));
        bus.send(new TestRunFinished(Instant.now(), result));
        assertThat(out, bytes(equalToCompressingWhiteSpace(AnsiEscapes.GREEN + "." + AnsiEscapes.RESET + "\n")));
    }

    @Test
    void print_green_dot_for_passing_step() {
        Result result = new Result(PASSED, Duration.ZERO, null);
        bus.send(new TestStepFinished(Instant.now(), mock(TestCase.class), mock(PickleStepTestStep.class), result));
        assertThat(out, bytes(equalTo(AnsiEscapes.GREEN + "." + AnsiEscapes.RESET)));
    }

    @Test
    void print_yellow_U_for_undefined_step() {
        Result result = new Result(UNDEFINED, Duration.ZERO, null);
        bus.send(new TestStepFinished(Instant.now(), mock(TestCase.class), mock(PickleStepTestStep.class), result));
        assertThat(out, bytes(equalTo(AnsiEscapes.YELLOW + "U" + AnsiEscapes.RESET)));
    }

    @Test
    void print_nothing_for_passed_hook() {
        Result result = new Result(PASSED, Duration.ZERO, null);
        bus.send(new TestStepFinished(Instant.now(), mock(TestCase.class), mock(HookTestStep.class), result));
    }

    @Test
    void print_red_F_for_failed_step() {
        Result result = new Result(FAILED, Duration.ZERO, null);
        bus.send(new TestStepFinished(Instant.now(), mock(TestCase.class), mock(PickleStepTestStep.class), result));
        assertThat(out, bytes(equalTo(AnsiEscapes.RED + "F" + AnsiEscapes.RESET)));
    }

    @Test
    void print_red_F_for_failed_hook() {
        Result result = new Result(FAILED, Duration.ZERO, null);
        bus.send(new TestStepFinished(Instant.now(), mock(TestCase.class), mock(HookTestStep.class), result));
        assertThat(out, bytes(equalTo(AnsiEscapes.RED + "F" + AnsiEscapes.RESET)));
    }

}
