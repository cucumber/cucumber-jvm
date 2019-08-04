package io.cucumber.testng;

import io.cucumber.core.event.Result;
import io.cucumber.core.event.Status;
import io.cucumber.core.eventbus.EventBus;
import io.cucumber.core.runtime.TimeServiceEventBus;
import org.testng.SkipException;
import org.testng.annotations.Test;

import java.time.Clock;

import static java.time.Duration.ZERO;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.isA;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertNull;
import static org.testng.Assert.assertTrue;

public class TestCaseResultListenerTest {

    private final EventBus bus = new TimeServiceEventBus(Clock.systemUTC());

    @Test
    public void should_be_passed_for_passed_result() {
        TestCaseResultListener resultListener = new TestCaseResultListener(bus, false);

        resultListener.receiveResult(mockPassedResult());

        assertTrue(resultListener.isPassed());
        assertNull(resultListener.getError());
    }

    @Test
    public void should_not_be_passed_for_failed_result() {
        Result result = mockFailedResult();
        TestCaseResultListener resultListener = new TestCaseResultListener(bus, false);

        resultListener.receiveResult(result);

        assertFalse(resultListener.isPassed());
        assertEquals(resultListener.getError(), result.getError());
    }

    @Test
    public void should_not_be_passed_for_ambiguous_result() {
        Result result = mockAmbiguousResult();
        TestCaseResultListener resultListener = new TestCaseResultListener(bus, false);

        resultListener.receiveResult(result);

        assertFalse(resultListener.isPassed());
        assertEquals(resultListener.getError(), result.getError());
    }

    @Test
    public void should_be_skipped_for_undefined_result() {
        TestCaseResultListener resultListener = new TestCaseResultListener(bus, false);

        resultListener.receiveResult(mockUndefinedResult());

        assertFalse(resultListener.isPassed());
        assertThat(resultListener.getError(), isA(SkipException.class));
    }

    @Test
    public void should_not_be_skipped_for_undefined_result_in_strict_mode() {
        TestCaseResultListener resultListener = new TestCaseResultListener(bus, true);

        resultListener.receiveResult(mockUndefinedResult());

        assertFalse(resultListener.isPassed());
        assertEquals(resultListener.getError().getMessage(), "There are undefined steps");
    }

    @Test
    public void should_be_skipped_for_pending_result() {
        TestCaseResultListener resultListener = new TestCaseResultListener(bus, false);

        resultListener.receiveResult(mockPendingResult());

        assertFalse(resultListener.isPassed());
        assertThat(resultListener.getError(), isA(SkipException.class));
    }

    @Test
    public void should_not_be_skipped_for_pending_result_in_strict_mode() {
        TestCaseResultListener resultListener = new TestCaseResultListener(bus, true);

        resultListener.receiveResult(mockPendingResult());

        assertFalse(resultListener.isPassed());
        assertThat(resultListener.getError(), isA(TestPendingException.class));
    }

    @Test
    public void should_be_skipped_for_skipped_result() {
        TestCaseResultListener resultListener = new TestCaseResultListener(bus, false);

        resultListener.receiveResult(mockSkippedResult());

        assertFalse(resultListener.isPassed());
        assertThat(resultListener.getError(), isA(SkipException.class));
    }

    private Result mockPassedResult() {
        return new Result(Status.PASSED, ZERO, null);
    }

    private Result mockSkippedResult() {
        return new Result(Status.SKIPPED, ZERO, null);
    }

    private Result mockUndefinedResult() {
        return new Result(Status.UNDEFINED, ZERO, null);
    }

    private Result mockFailedResult() {
        return new Result(Status.FAILED, ZERO, new Exception());
    }

    private Result mockAmbiguousResult() {
        return new Result(Status.AMBIGUOUS, ZERO, new Exception());
    }

    private Result mockPendingResult() {
        return new Result(Status.PENDING, ZERO, new TestPendingException());
    }

}
