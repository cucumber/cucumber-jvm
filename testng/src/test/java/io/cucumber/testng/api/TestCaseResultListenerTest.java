package io.cucumber.testng.api;

import io.cucumber.core.api.event.Result;
import io.cucumber.core.event.EventBus;
import io.cucumber.core.runner.TimeServiceEventBus;
import org.testng.SkipException;
import org.testng.annotations.Test;

import static java.time.Duration.ZERO;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertNull;
import static org.testng.Assert.assertTrue;

import java.time.Clock;

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
        assertTrue(resultListener.getError() instanceof SkipException);
    }

    @Test
    public void should_not_be_skipped_for_undefined_result_in_strict_mode() {
        TestCaseResultListener resultListener = new TestCaseResultListener(bus, true);

        resultListener.receiveResult(mockUndefinedResult());

        assertFalse(resultListener.isPassed());
        assertEquals(resultListener.getError().getMessage(), TestCaseResultListener.UNDEFINED_MESSAGE);
    }

    @Test
    public void should_be_skipped_for_pending_result() {
        TestCaseResultListener resultListener = new TestCaseResultListener(bus, false);

        resultListener.receiveResult(mockPendingResult());

        assertFalse(resultListener.isPassed());
        assertTrue(resultListener.getError() instanceof SkipException);
    }

    @Test
    public void should_not_be_skipped_for_pending_result_in_strict_mode() {
        TestCaseResultListener resultListener = new TestCaseResultListener(bus, true);

        resultListener.receiveResult(mockPendingResult());

        assertFalse(resultListener.isPassed());
        assertTrue(resultListener.getError() instanceof TestPendingException);
    }

    @Test
    public void should_be_skipped_for_skipped_result() {
        TestCaseResultListener resultListener = new TestCaseResultListener(bus, false);

        resultListener.receiveResult(mockSkippedResult());

        assertFalse(resultListener.isPassed());
        assertTrue(resultListener.getError() instanceof SkipException);
    }

    private Result mockPassedResult() {
        return new Result(Result.Type.PASSED, ZERO, null);
    }

    private Result mockSkippedResult() {
        return new Result(Result.Type.SKIPPED, ZERO, null);
    }

    private Result mockUndefinedResult() {
        return new Result(Result.Type.UNDEFINED, ZERO, null);
    }

    private Result mockFailedResult() {
        return new Result(Result.Type.FAILED, ZERO, new Exception());
    }

    private Result mockAmbiguousResult() {
        return new Result(Result.Type.AMBIGUOUS, ZERO, new Exception());
    }

    private Result mockPendingResult() {
        return new Result(Result.Type.PENDING, ZERO, new TestPendingException());
    }

}
