package io.cucumber.core.plugin;

import io.cucumber.core.eventbus.EventBus;
import io.cucumber.core.runtime.TimeServiceEventBus;
import io.cucumber.plugin.event.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.net.URI;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static io.cucumber.core.plugin.Bytes.bytes;
import static io.cucumber.plugin.event.Status.FAILED;
import static io.cucumber.plugin.event.Status.PASSED;
import static io.cucumber.plugin.event.Status.UNDEFINED;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.text.IsEqualCompressingWhiteSpace.equalToCompressingWhiteSpace;

class ProgressFormatterTest {

    final EventBus bus = new TimeServiceEventBus(Clock.systemUTC(), UUID::randomUUID);
    final ByteArrayOutputStream out = new ByteArrayOutputStream();
    final ProgressFormatter formatter = new ProgressFormatter(out);
    private final MockTestCase mocktestCase = new MockTestCase();
    private final MockPickleStepTestStep mockPickleStepTestStep = new MockPickleStepTestStep();

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
        bus.send(new TestStepFinished(Instant.now(), mocktestCase, mockPickleStepTestStep, result));
        bus.send(new TestRunFinished(Instant.now(), result));
        assertThat(out, bytes(equalToCompressingWhiteSpace(AnsiEscapes.GREEN + "." + AnsiEscapes.RESET + "\n")));
    }

    @Test
    void print_green_dot_for_passing_step() {
        Result result = new Result(PASSED, Duration.ZERO, null);
        bus.send(new TestStepFinished(Instant.now(), mocktestCase, mockPickleStepTestStep, result));
        assertThat(out, bytes(equalTo(AnsiEscapes.GREEN + "." + AnsiEscapes.RESET)));
    }

    @Test
    void print_yellow_U_for_undefined_step() {
        Result result = new Result(UNDEFINED, Duration.ZERO, null);
        bus.send(new TestStepFinished(Instant.now(), mocktestCase, mockPickleStepTestStep, result));
        assertThat(out, bytes(equalTo(AnsiEscapes.YELLOW + "U" + AnsiEscapes.RESET)));
    }

    @Test
    void print_nothing_for_passed_hook() {
        Result result = new Result(PASSED, Duration.ZERO, null);
        bus.send(new TestStepFinished(Instant.now(), mocktestCase, mockPickleStepTestStep, result));
    }

    @Test
    void print_red_F_for_failed_step() {
        Result result = new Result(FAILED, Duration.ZERO, null);
        bus.send(new TestStepFinished(Instant.now(), mocktestCase, mockPickleStepTestStep, result));
        assertThat(out, bytes(equalTo(AnsiEscapes.RED + "F" + AnsiEscapes.RESET)));
    }

    @Test
    void print_red_F_for_failed_hook() {
        Result result = new Result(FAILED, Duration.ZERO, null);
        bus.send(new TestStepFinished(Instant.now(), mocktestCase, mockPickleStepTestStep, result));
        assertThat(out, bytes(equalTo(AnsiEscapes.RED + "F" + AnsiEscapes.RESET)));
    }

    private class MockTestCase implements TestCase {
        @Override
        public Integer getLine() {
            return null;
        }

        @Override
        public Location getLocation() {
            return null;
        }

        @Override
        public String getKeyword() {
            return null;
        }

        @Override
        public String getName() {
            return null;
        }

        @Override
        public String getScenarioDesignation() {
            return null;
        }

        @Override
        public List<String> getTags() {
            return null;
        }

        @Override
        public List<TestStep> getTestSteps() {
            return null;
        }

        @Override
        public URI getUri() {
            return null;
        }

        @Override
        public UUID getId() {
            return null;
        }
    }

    private class MockPickleStepTestStep implements PickleStepTestStep {
        @Override
        public String getPattern() {
            return null;
        }

        @Override
        public Step getStep() {
            return null;
        }

        @Override
        public List<Argument> getDefinitionArgument() {
            return null;
        }

        @Override
        public StepArgument getStepArgument() {
            return null;
        }

        @Override
        public int getStepLine() {
            return 0;
        }

        @Override
        public URI getUri() {
            return null;
        }

        @Override
        public String getStepText() {
            return null;
        }

        @Override
        public String getCodeLocation() {
            return null;
        }

        @Override
        public UUID getId() {
            return null;
        }
    }
}
