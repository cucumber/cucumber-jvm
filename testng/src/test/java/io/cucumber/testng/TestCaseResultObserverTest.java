package io.cucumber.testng;

import io.cucumber.core.eventbus.EventBus;
import io.cucumber.core.runtime.TimeServiceEventBus;
import io.cucumber.plugin.event.PickleStepTestStep;
import io.cucumber.plugin.event.Result;
import io.cucumber.plugin.event.SnippetsSuggestedEvent;
import io.cucumber.plugin.event.Status;
import io.cucumber.plugin.event.TestCase;
import io.cucumber.plugin.event.TestCaseFinished;
import io.cucumber.plugin.event.TestStepFinished;
import org.testng.SkipException;
import org.testng.annotations.Test;

import java.net.URI;
import java.time.Clock;
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
import static java.util.Objects.requireNonNull;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.isA;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertNull;
import static org.testng.Assert.assertThrows;
import static org.testng.Assert.assertTrue;
import static org.testng.Assert.expectThrows;

public class TestCaseResultObserverTest {

    private final EventBus bus = new TimeServiceEventBus(Clock.systemUTC(), UUID::randomUUID);

    private final URI uri = URI.create("file:path/to.feature");
    private final int line = 0;
    private final Exception error = new Exception();
    private final TestCase testCase = mock(TestCase.class);
    private final PickleStepTestStep step = createPickleStepTestStep();

    private PickleStepTestStep createPickleStepTestStep() {
        PickleStepTestStep step = mock(PickleStepTestStep.class);
        when(step.getStepLine()).thenReturn(line);
        when(step.getUri()).thenReturn(uri);
        when(step.getStepText()).thenReturn("some step");
        return step;
    }

    @Test
    public void should_be_passed_for_passed_result() throws Throwable {
        TestCaseResultObserver resultListener = TestCaseResultObserver.observe(bus, false);

        Result stepResult = new Result(Status.PASSED, ZERO, null);
        bus.send(new TestStepFinished(now(), testCase, step, stepResult));

        Result testCaseResult = new Result(Status.PASSED, ZERO, null);
        bus.send(new TestCaseFinished(now(), testCase, testCaseResult));

        resultListener.assertTestCasePassed();
    }

    @Test
    public void should_not_be_passed_for_failed_result() {
        TestCaseResultObserver resultListener = TestCaseResultObserver.observe(bus, false);

        Result stepResult = new Result(FAILED, ZERO, error);
        bus.send(new TestStepFinished(now(), testCase, step, stepResult));

        Result testCaseResult = new Result(FAILED, ZERO, error);
        bus.send(new TestCaseFinished(now(), testCase, testCaseResult));

        Exception exception = expectThrows(Exception.class, resultListener::assertTestCasePassed);
        assertEquals(exception, error);
    }

    @Test
    public void should_not_be_passed_for_ambiguous_result() {
        TestCaseResultObserver resultListener = TestCaseResultObserver.observe(bus, false);

        Result stepResult = new Result(AMBIGUOUS, ZERO, error);
        bus.send(new TestStepFinished(now(), testCase, step, stepResult));

        Result testCaseResult = new Result(AMBIGUOUS, ZERO, error);
        bus.send(new TestCaseFinished(now(), testCase, testCaseResult));

        Exception exception = expectThrows(Exception.class, resultListener::assertTestCasePassed);
        assertEquals(exception, error);
    }

    @Test
    public void should_be_skipped_for_undefined_result() {
        TestCaseResultObserver resultListener = TestCaseResultObserver.observe(bus, false);

        bus.send(new SnippetsSuggestedEvent(now(), uri, line, line, singletonList("stub snippet")));

        Result stepResult = new Result(UNDEFINED, ZERO, error);
        bus.send(new TestStepFinished(now(), testCase, step, stepResult));

        Result testCaseResult = new Result(UNDEFINED, ZERO, error);
        bus.send(new TestCaseFinished(now(), testCase, testCaseResult));

        SkipException skipException = expectThrows(SkipException.class, resultListener::assertTestCasePassed);
        assertThat(skipException.isSkip(), is(true));
        assertThat(skipException.getMessage(), is("" +
            "The step \"some step\" is undefined. You can implement it using the snippet(s) below:\n" +
            "\n" +
            "stub snippet\n"
        ));
    }

    @Test
    public void should_not_be_skipped_for_undefined_result_in_strict_mode() {
        TestCaseResultObserver resultListener = TestCaseResultObserver.observe(bus, true);

        bus.send(new SnippetsSuggestedEvent(now(), uri, line, line, singletonList("stub snippet")));

        Result stepResult = new Result(UNDEFINED, ZERO, error);
        bus.send(new TestStepFinished(now(), testCase, step, stepResult));

        Result testCaseResult = new Result(UNDEFINED, ZERO, error);
        bus.send(new TestCaseFinished(now(), testCase, testCaseResult));

        SkipException skipException = expectThrows(SkipException.class, resultListener::assertTestCasePassed);
        assertThat(skipException.isSkip(), is(false));
        assertThat(skipException.getMessage(), is("" +
            "The step \"some step\" is undefined. You can implement it using the snippet(s) below:\n" +
            "\n" +
            "stub snippet\n"
        ));
    }

    @Test
    public void should_be_passed_for_empty_scenario() throws Throwable {
        TestCaseResultObserver resultListener = TestCaseResultObserver.observe(bus, false);

        Result testCaseResult = new Result(PASSED, ZERO, error);
        bus.send(new TestCaseFinished(now(), testCase, testCaseResult));

        resultListener.assertTestCasePassed();
    }

    @Test
    public void should_be_skipped_for_pending_result() {
        TestCaseResultObserver resultListener = TestCaseResultObserver.observe(bus, false);

        Exception error = new TestPendingException();

        Result stepResult = new Result(PENDING, ZERO, error);
        bus.send(new TestStepFinished(now(), testCase, step, stepResult));

        Result testCaseResult = new Result(PENDING, ZERO, error);
        bus.send(new TestCaseFinished(now(), testCase, testCaseResult));

        expectThrows(SkipException.class, resultListener::assertTestCasePassed);
    }

    @Test
    public void should_not_be_skipped_for_pending_result_in_strict_mode() {
        TestCaseResultObserver resultListener = TestCaseResultObserver.observe(bus, true);

        TestPendingException error = new TestPendingException();

        Result stepResult = new Result(PENDING, ZERO, error);
        bus.send(new TestStepFinished(now(), testCase, step, stepResult));

        Result testCaseResult = new Result(PENDING, ZERO, error);
        bus.send(new TestCaseFinished(now(), testCase, testCaseResult));

        Exception exception = expectThrows(Exception.class, resultListener::assertTestCasePassed);
        assertEquals(exception, error);
    }

    @Test
    public void should_be_skipped_for_skipped_result() {
        TestCaseResultObserver resultListener = TestCaseResultObserver.observe(bus, false);

        Result stepResult = new Result(SKIPPED, ZERO, null);
        bus.send(new TestStepFinished(now(), testCase, step, stepResult));

        Result testCaseResult = new Result(SKIPPED, ZERO, null);
        bus.send(new TestCaseFinished(now(), testCase, testCaseResult));

        expectThrows(SkipException.class, resultListener::assertTestCasePassed);
    }
}
