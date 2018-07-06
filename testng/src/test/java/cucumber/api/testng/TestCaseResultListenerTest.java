package cucumber.api.testng;

import cucumber.api.PendingException;
import cucumber.api.Result;
import cucumber.runner.EventBus;
import cucumber.runner.TimeService;
import cucumber.runner.TimeServiceEventBus;
import org.testng.SkipException;
import org.testng.annotations.Test;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertNull;
import static org.testng.Assert.assertTrue;

public class TestCaseResultListenerTest {

    private final EventBus bus = new TimeServiceEventBus(TimeService.SYSTEM);

    @Test
    public void should_be_passed_for_passed_result() throws Exception {
        TestCaseResultListener resultListener = new TestCaseResultListener(bus, false);

        resultListener.receiveResult(mockPassedResult());

        assertTrue(resultListener.isPassed());
        assertNull(resultListener.getError());
    }

    @Test
    public void should_not_be_passed_for_failed_result() throws Exception {
        Result result = mockFailedResult();
        TestCaseResultListener resultListener = new TestCaseResultListener(bus, false);

        resultListener.receiveResult(result);

        assertFalse(resultListener.isPassed());
        assertEquals(resultListener.getError(), result.getError());
    }

    @Test
    public void should_not_be_passed_for_ambiguous_result() throws Exception {
        Result result = mockAmbiguousResult();
        TestCaseResultListener resultListener = new TestCaseResultListener(bus, false);

        resultListener.receiveResult(result);

        assertFalse(resultListener.isPassed());
        assertEquals(resultListener.getError(), result.getError());
    }

    @Test
    public void should_be_skipped_for_undefined_result() throws Exception {
        TestCaseResultListener resultListener = new TestCaseResultListener(bus, false);

        resultListener.receiveResult(mockUndefinedResult());

        assertFalse(resultListener.isPassed());
        assertTrue(resultListener.getError() instanceof SkipException);
    }

    @Test
    public void should_not_be_skipped_for_undefined_result_in_strict_mode() throws Exception {
        TestCaseResultListener resultListener = new TestCaseResultListener(bus, true);

        resultListener.receiveResult(mockUndefinedResult());

        assertFalse(resultListener.isPassed());
        assertEquals(resultListener.getError().getMessage(), TestCaseResultListener.UNDEFINED_MESSAGE);
    }

    @Test
    public void should_be_skipped_for_pending_result() throws Exception {
        TestCaseResultListener resultListener = new TestCaseResultListener(bus, false);

        resultListener.receiveResult(mockPendingResult());

        assertFalse(resultListener.isPassed());
        assertTrue(resultListener.getError() instanceof SkipException);
    }

    @Test
    public void should_not_be_skipped_for_pending_result_in_strict_mode() throws Exception {
        TestCaseResultListener resultListener = new TestCaseResultListener(bus, true);

        resultListener.receiveResult(mockPendingResult());

        assertFalse(resultListener.isPassed());
        assertTrue(resultListener.getError() instanceof PendingException);
    }

    @Test
    public void should_be_skipped_for_skipped_result() throws Exception {
        TestCaseResultListener resultListener = new TestCaseResultListener(bus, false);

        resultListener.receiveResult(mockSkippedResult());

        assertFalse(resultListener.isPassed());
        assertTrue(resultListener.getError() instanceof SkipException);
    }

    private Result mockPassedResult() {
        Result result = mockResult(Result.Type.PASSED);
        return result;
    }

    private Result mockSkippedResult() {
        Result result = mockResult(Result.Type.SKIPPED);
        return result;
    }

    private Result mockUndefinedResult() {
        Result result = mockResult(Result.Type.UNDEFINED);
        return result;
    }

    private Result mockFailedResult() {
        Result result = mockResult(Result.Type.FAILED);
        when(result.getError()).thenReturn(mock(Throwable.class));
        return result;
    }

    private Result mockAmbiguousResult() {
        Result result = mockResult(Result.Type.AMBIGUOUS);
        when(result.getError()).thenReturn(mock(Throwable.class));
        return result;
    }

    private Result mockPendingResult() {
        Result result = mockResult(Result.Type.PENDING);
        when(result.getError()).thenReturn(new PendingException());
        return result;
    }

    private Result mockResult(Result.Type status) {
        Result result = mock(Result.class);
        when(result.getStatus()).thenReturn(status);
        for (Result.Type type : Result.Type.values()) {
            when(result.is(type)).thenReturn(type == status);
        }
        return result;
    }
}
