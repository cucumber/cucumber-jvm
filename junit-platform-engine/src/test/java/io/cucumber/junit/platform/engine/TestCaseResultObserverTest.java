package io.cucumber.junit.platform.engine;

import io.cucumber.core.eventbus.EventBus;
import io.cucumber.core.runtime.TimeServiceEventBus;
import io.cucumber.plugin.event.Argument;
import io.cucumber.plugin.event.Location;
import io.cucumber.plugin.event.PickleStepTestStep;
import io.cucumber.plugin.event.Result;
import io.cucumber.plugin.event.SnippetsSuggestedEvent;
import io.cucumber.plugin.event.Status;
import io.cucumber.plugin.event.Step;
import io.cucumber.plugin.event.StepArgument;
import io.cucumber.plugin.event.TestCase;
import io.cucumber.plugin.event.TestCaseFinished;
import io.cucumber.plugin.event.TestCaseStarted;
import io.cucumber.plugin.event.TestStep;
import io.cucumber.plugin.event.TestStepFinished;
import io.cucumber.plugin.event.TestStepStarted;
import org.junit.jupiter.api.Test;
import org.opentest4j.AssertionFailedError;
import org.opentest4j.TestAbortedException;

import java.net.URI;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

class TestCaseResultObserverTest {

    private final URI uri = URI.create("classpath:io/cucumber/junit/platform/engine.feature");
    private final EventBus bus = new TimeServiceEventBus(Clock.systemUTC(), UUID::randomUUID);
    private final TestCaseResultObserver observer = TestCaseResultObserver.observe(bus);

    private final TestCase testCase = new TestCase() {
        @Override
        public Integer getLine() {
            return 12;
        }

        @Override
        public Location getLocation() {
            return new Location(12, 4);
        }

        @Override
        public String getKeyword() {
            return "Scenario";
        }

        @Override
        public String getName() {
            return "Mocked test case";
        }

        @Override
        public String getScenarioDesignation() {
            return "mock-test-case:12";
        }

        @Override
        public List<String> getTags() {
            return emptyList();
        }

        @Override
        public List<TestStep> getTestSteps() {
            return emptyList();
        }

        @Override
        public URI getUri() {
            return uri;
        }

        @Override
        public UUID getId() {
            return UUID.randomUUID();
        }
    };
    private final PickleStepTestStep testStep = new PickleStepTestStep() {
        final Step step = new Step() {
            @Override
            public StepArgument getArgument() {
                return null;
            }

            @Override
            public String getKeyword() {
                return "Given";
            }

            @Override
            public String getText() {
                return "mocked";
            }

            @Override
            public int getLine() {
                return 15;
            }

            @Override
            public Location getLocation() {
                return new Location(15, 8);
            }
        };

        @Override
        public String getPattern() {
            return "mocked";
        }

        @Override
        public Step getStep() {
            return step;
        }

        @Override
        public List<Argument> getDefinitionArgument() {
            return emptyList();
        }

        @Override
        public StepArgument getStepArgument() {
            return step.getArgument();
        }

        @Override
        public int getStepLine() {
            return step.getLine();
        }

        @Override
        public URI getUri() {
            return uri;
        }

        @Override
        public String getStepText() {
            return step.getText();
        }

        @Override
        public String getCodeLocation() {
            return null;
        }

        @Override
        public UUID getId() {
            return UUID.randomUUID();
        }

    };

    @Test
    void passed() {
        bus.send(new TestCaseStarted(Instant.now(), testCase));
        bus.send(new TestStepStarted(Instant.now(), testCase, testStep));
        Result result = new Result(Status.PASSED, Duration.ZERO, null);
        bus.send(new TestStepFinished(Instant.now(), testCase, testStep, result));
        bus.send(new TestCaseFinished(Instant.now(), testCase, result));
        observer.assertTestCasePassed();
    }

    @Test
    void failed() {
        bus.send(new TestCaseStarted(Instant.now(), testCase));
        bus.send(new TestStepStarted(Instant.now(), testCase, testStep));
        Throwable error = new AssertionFailedError("Mocked");
        Result result = new Result(Status.FAILED, Duration.ZERO, error);
        bus.send(new TestStepFinished(Instant.now(), testCase, testStep, result));
        bus.send(new TestCaseFinished(Instant.now(), testCase, result));
        Exception exception = assertThrows(Exception.class, observer::assertTestCasePassed);
        assertThat(exception.getCause(), is(error));
    }

    @Test
    void skippedByDryRun() {
        bus.send(new TestCaseStarted(Instant.now(), testCase));
        bus.send(new TestStepStarted(Instant.now(), testCase, testStep));
        Result result = new Result(Status.SKIPPED, Duration.ZERO, null);
        bus.send(new TestStepFinished(Instant.now(), testCase, testStep, result));
        bus.send(new TestCaseFinished(Instant.now(), testCase, result));
        Exception exception = assertThrows(Exception.class, observer::assertTestCasePassed);
        assertThat(exception.getCause(), instanceOf(TestAbortedException.class));
    }

    @Test
    void skippedByUser() {
        bus.send(new TestCaseStarted(Instant.now(), testCase));
        bus.send(new TestStepStarted(Instant.now(), testCase, testStep));
        Result result = new Result(Status.SKIPPED, Duration.ZERO, new TestAbortedException("thrown by user"));
        bus.send(new TestStepFinished(Instant.now(), testCase, testStep, result));
        bus.send(new TestCaseFinished(Instant.now(), testCase, result));
        Exception exception = assertThrows(Exception.class, observer::assertTestCasePassed);
        assertThat(exception.getCause(), instanceOf(TestAbortedException.class));

    }

    @Test
    void undefined() {
        bus.send(new TestCaseStarted(Instant.now(), testCase));
        bus.send(new TestStepStarted(Instant.now(), testCase, testStep));
        bus.send(new SnippetsSuggestedEvent(
            Instant.now(),
            uri,
            testCase.getLocation(),
            testStep.getStep().getLocation(),
            asList(
                "mocked snippet 1",
                "mocked snippet 2",
                "mocked snippet 3")));
        Result result = new Result(Status.UNDEFINED, Duration.ZERO, null);
        bus.send(new TestStepFinished(Instant.now(), testCase, testStep, result));
        bus.send(new TestCaseFinished(Instant.now(), testCase, result));
        Exception exception = assertThrows(Exception.class, observer::assertTestCasePassed);
        assertThat(exception.getCause(), instanceOf(UndefinedStepException.class));

        assertThat(exception.getCause().getMessage(), is("" +
                "The step \"mocked\" is undefined. You can implement it using the snippet(s) below:\n" +
                "\n" +
                "mocked snippet 1\n" +
                "---\n" +
                "mocked snippet 2\n" +
                "---\n" +
                "mocked snippet 3\n"));
    }

    @Test
    void empty() {
        bus.send(new TestCaseStarted(Instant.now(), testCase));
        Result result = new Result(Status.PASSED, Duration.ZERO, null);
        bus.send(new TestCaseFinished(Instant.now(), testCase, result));
        observer.assertTestCasePassed();
    }

}
