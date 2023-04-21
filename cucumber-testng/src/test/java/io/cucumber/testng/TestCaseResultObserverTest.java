package io.cucumber.testng;

import io.cucumber.core.eventbus.EventBus;
import io.cucumber.core.runtime.TimeServiceEventBus;
import io.cucumber.plugin.event.*;
import io.cucumber.plugin.event.SnippetsSuggestedEvent.Suggestion;
import org.testng.SkipException;
import org.testng.annotations.Test;

import java.net.URI;
import java.time.Clock;
import java.util.List;
import java.util.UUID;

import static io.cucumber.plugin.event.Status.AMBIGUOUS;
import static io.cucumber.plugin.event.Status.FAILED;
import static io.cucumber.plugin.event.Status.PASSED;
import static io.cucumber.plugin.event.Status.PENDING;
import static io.cucumber.plugin.event.Status.SKIPPED;
import static io.cucumber.plugin.event.Status.UNDEFINED;
import static java.time.Duration.ZERO;
import static java.time.Instant.now;
import static java.util.Collections.singletonList;
import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.expectThrows;

public class TestCaseResultObserverTest {

    private final EventBus bus = new TimeServiceEventBus(Clock.systemUTC(), UUID::randomUUID);

    private final URI uri = URI.create("file:path/to.feature");
    private final Location location = new Location(0, -1);
    private final Exception error = new Exception();
    private final TestCase testCase = new MockTestCase();
    private final PickleStepTestStep step = createPickleStepTestStep();

    private PickleStepTestStep createPickleStepTestStep() {
        return new MockPickleStepTestStep(new MockStep(location), uri);
    }

    @Test
    public void should_be_passed_for_passed_result() {
        TestCaseResultObserver resultListener = TestCaseResultObserver.observe(bus);

        Result stepResult = new Result(Status.PASSED, ZERO, null);
        bus.send(new TestStepFinished(now(), testCase, step, stepResult));

        Result testCaseResult = new Result(Status.PASSED, ZERO, null);
        bus.send(new TestCaseFinished(now(), testCase, testCaseResult));

        resultListener.assertTestCasePassed();
    }

    @Test
    public void should_not_be_passed_for_failed_result() {
        TestCaseResultObserver resultListener = TestCaseResultObserver.observe(bus);

        Result stepResult = new Result(FAILED, ZERO, error);
        bus.send(new TestStepFinished(now(), testCase, step, stepResult));

        Result testCaseResult = new Result(FAILED, ZERO, error);
        bus.send(new TestCaseFinished(now(), testCase, testCaseResult));

        Exception exception = expectThrows(Exception.class, resultListener::assertTestCasePassed);
        assertEquals(exception.getCause(), error);
    }

    @Test
    public void should_not_be_passed_for_ambiguous_result() {
        TestCaseResultObserver resultListener = TestCaseResultObserver.observe(bus);

        Result stepResult = new Result(AMBIGUOUS, ZERO, error);
        bus.send(new TestStepFinished(now(), testCase, step, stepResult));

        Result testCaseResult = new Result(AMBIGUOUS, ZERO, error);
        bus.send(new TestCaseFinished(now(), testCase, testCaseResult));

        Exception exception = expectThrows(Exception.class, resultListener::assertTestCasePassed);
        assertEquals(exception.getCause(), error);
    }

    @Test
    public void should_be_failed_for_undefined_result() {
        TestCaseResultObserver resultListener = TestCaseResultObserver.observe(bus);

        bus.send(new SnippetsSuggestedEvent(now(), uri, location, location,
            new Suggestion("some step", singletonList("stub snippet"))));

        Result stepResult = new Result(UNDEFINED, ZERO, error);
        bus.send(new TestStepFinished(now(), testCase, step, stepResult));

        Result testCaseResult = new Result(UNDEFINED, ZERO, error);
        bus.send(new TestCaseFinished(now(), testCase, testCaseResult));

        Exception exception = expectThrows(Exception.class, resultListener::assertTestCasePassed);
        assertThat(exception.getCause(), instanceOf(SkipException.class));
        SkipException skipException = (SkipException) exception.getCause();
        assertThat(skipException.isSkip(), is(false));
        assertThat(skipException.getMessage(), is("" +
                "The step 'some step' is undefined.\n" +
                "You can implement this step using the snippet(s) below:\n" +
                "\n" +
                "stub snippet\n"));
    }

    @Test
    public void should_not_be_skipped_for_undefined_result() {
        TestCaseResultObserver resultListener = TestCaseResultObserver.observe(bus);

        bus.send(new SnippetsSuggestedEvent(now(), uri, location,
            location, new SnippetsSuggestedEvent.Suggestion("some step", singletonList("stub snippet"))));

        Result stepResult = new Result(UNDEFINED, ZERO, error);
        bus.send(new TestStepFinished(now(), testCase, step, stepResult));

        Result testCaseResult = new Result(UNDEFINED, ZERO, error);
        bus.send(new TestCaseFinished(now(), testCase, testCaseResult));

        Exception exception = expectThrows(Exception.class, resultListener::assertTestCasePassed);
        assertThat(exception.getCause(), instanceOf(SkipException.class));
        SkipException skipException = (SkipException) exception.getCause();
        assertThat(skipException.isSkip(), is(false));
        assertThat(skipException.getMessage(), is("" +
                "The step 'some step' is undefined.\n" +
                "You can implement this step using the snippet(s) below:\n" +
                "\n" +
                "stub snippet\n"));
    }

    @Test
    public void should_be_passed_for_empty_scenario() {
        TestCaseResultObserver resultListener = TestCaseResultObserver.observe(bus);

        Result testCaseResult = new Result(PASSED, ZERO, error);
        bus.send(new TestCaseFinished(now(), testCase, testCaseResult));

        resultListener.assertTestCasePassed();
    }

    @Test
    public void should_be_skipped_for_pending_result() {
        TestCaseResultObserver resultListener = TestCaseResultObserver.observe(bus);

        Exception error = new TestPendingException();

        Result stepResult = new Result(PENDING, ZERO, error);
        bus.send(new TestStepFinished(now(), testCase, step, stepResult));

        Result testCaseResult = new Result(PENDING, ZERO, error);
        bus.send(new TestCaseFinished(now(), testCase, testCaseResult));

        Exception exception = expectThrows(Exception.class, resultListener::assertTestCasePassed);
        assertThat(exception.getCause(), is(error));
    }

    @Test
    public void should_not_be_skipped_for_pending_result() {
        TestCaseResultObserver resultListener = TestCaseResultObserver.observe(bus);

        TestPendingException error = new TestPendingException();

        Result stepResult = new Result(PENDING, ZERO, error);
        bus.send(new TestStepFinished(now(), testCase, step, stepResult));

        Result testCaseResult = new Result(PENDING, ZERO, error);
        bus.send(new TestCaseFinished(now(), testCase, testCaseResult));

        Exception exception = expectThrows(Exception.class, resultListener::assertTestCasePassed);
        assertEquals(exception.getCause(), error);
    }

    @Test
    public void should_be_skipped_for_skipped_result() {
        TestCaseResultObserver resultListener = TestCaseResultObserver.observe(bus);

        Result stepResult = new Result(SKIPPED, ZERO, null);
        bus.send(new TestStepFinished(now(), testCase, step, stepResult));

        Result testCaseResult = new Result(SKIPPED, ZERO, null);
        bus.send(new TestCaseFinished(now(), testCase, testCaseResult));

        Exception exception = expectThrows(Exception.class, resultListener::assertTestCasePassed);
        assertThat(exception.getCause(), instanceOf(SkipException.class));
    }

    private static final class MockStep implements Step {
        Location location;
        public MockStep(Location location) {
            this.location = location;
        }

        @Override
        public StepArgument getArgument() {
            return null;
        }

        @Override
        public String getKeyword() {
            return null;
        }

        @Override
        public String getText() {
            return null;
        }

        @Override
        public int getLine() {
            return 0;
        }

        @Override
        public Location getLocation() {
            return location;
        }
    }

    private static class MockPickleStepTestStep implements PickleStepTestStep {
        private final Step step;
        private final URI uri;

        public MockPickleStepTestStep(Step step, URI uri) {
            this.step = step;
            this.uri = uri;
        }

        @Override
        public String getPattern() {
            return null;
        }

        @Override
        public Step getStep() {
            return step;
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
            return uri;
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

    private static class MockTestCase implements TestCase {

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
}
