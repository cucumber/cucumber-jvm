package io.cucumber.junit.platform.engine;

import io.cucumber.core.eventbus.EventBus;
import io.cucumber.core.runtime.TimeServiceEventBus;
import io.cucumber.plugin.event.Argument;
import io.cucumber.plugin.event.CucumberStep;
import io.cucumber.plugin.event.PickleStepTestStep;
import io.cucumber.plugin.event.Result;
import io.cucumber.plugin.event.SnippetsSuggestedEvent;
import io.cucumber.plugin.event.Status;
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
import org.opentest4j.TestSkippedException;

import java.net.URI;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.List;

import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

class TestCaseResultObserverTest {

    private final URI uri = URI.create("classpath:io/cucumber/junit/platform/engine.feature");
    private final EventBus bus = new TimeServiceEventBus(Clock.systemUTC());
    private final TestCaseResultObserver observer = TestCaseResultObserver.observe(bus);

    private final TestCase testCase = new TestCase() {
        @Override
        public Integer getLine() {
            return 12;
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
    };
    private PickleStepTestStep testStep = new PickleStepTestStep() {
        CucumberStep cucumberStep = new CucumberStep() {
            @Override
            public StepArgument getArgument() {
                return null;
            }

            @Override
            public String getKeyWord() {
                return "Given";
            }

            @Override
            public String getText() {
                return "mocked";
            }

            @Override
            public int getStepLine() {
                return 15;
            }
        };

        @Override
        public String getPattern() {
            return "mocked";
        }

        @Override
        public CucumberStep getStep() {
            return cucumberStep;
        }

        @Override
        public List<Argument> getDefinitionArgument() {
            return emptyList();
        }

        @Override
        public StepArgument getStepArgument() {
            return cucumberStep.getArgument();
        }

        @Override
        public int getStepLine() {
            return cucumberStep.getStepLine();
        }

        @Override
        public URI getUri() {
            return uri;
        }

        @Override
        public String getStepText() {
            return cucumberStep.getText();
        }

        @Override
        public String getCodeLocation() {
            return null;
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
        Result result = new Result(Status.FAILED, Duration.ZERO, new AssertionFailedError("Mocked"));
        bus.send(new TestStepFinished(Instant.now(), testCase, testStep, result));
        bus.send(new TestCaseFinished(Instant.now(), testCase, result));
        assertThrows(
            AssertionFailedError.class,
            observer::assertTestCasePassed
        );
    }

    @Test
    void skippedByDryRun() {
        bus.send(new TestCaseStarted(Instant.now(), testCase));
        bus.send(new TestStepStarted(Instant.now(), testCase, testStep));
        Result result = new Result(Status.SKIPPED, Duration.ZERO, null);
        bus.send(new TestStepFinished(Instant.now(), testCase, testStep, result));
        bus.send(new TestCaseFinished(Instant.now(), testCase, result));
        assertThrows(
            TestAbortedException.class,
            observer::assertTestCasePassed
        );
    }

    @Test
    void skippedByUser() {
        bus.send(new TestCaseStarted(Instant.now(), testCase));
        bus.send(new TestStepStarted(Instant.now(), testCase, testStep));
        Result result = new Result(Status.SKIPPED, Duration.ZERO, new TestAbortedException("thrown by user"));
        bus.send(new TestStepFinished(Instant.now(), testCase, testStep, result));
        bus.send(new TestCaseFinished(Instant.now(), testCase, result));
        assertThrows(
            TestAbortedException.class,
            observer::assertTestCasePassed
        );
    }

    @Test
    void undefined() {
        bus.send(new TestCaseStarted(Instant.now(), testCase));
        bus.send(new TestStepStarted(Instant.now(), testCase, testStep));
        bus.send(new SnippetsSuggestedEvent(Instant.now(), uri, testStep.getStepLine(), asList(
            "mocked snippet 1",
            "mocked snippet 2",
            "mocked snippet 3"
        )));
        Result result = new Result(Status.UNDEFINED, Duration.ZERO, null);
        bus.send(new TestStepFinished(Instant.now(), testCase, testStep, result));
        bus.send(new TestCaseFinished(Instant.now(), testCase, result));
        UndefinedStepException exception = assertThrows(
            UndefinedStepException.class,
            observer::assertTestCasePassed
        );
        assertThat(exception.getMessage(), is("" +
            "The step \"mocked\" is undefined. You can implement it using tne snippet(s) below:\n" +
            "\n" +
            "mocked snippet 1\n" +
            "---\n" +
            "mocked snippet 2\n" +
            "---\n" +
            "mocked snippet 3\n"));
    }


}