package cucumber.api.testng;

import cucumber.api.Result;
import org.testng.annotations.Test;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertNull;
import static org.testng.Assert.assertTrue;

public class FeatureResultListenerTest {

    @Test
    public void should_be_passed_for_passed_result() throws Exception {
        FeatureResultListener resultListener = new FeatureResultListener(false);

        resultListener.collectError(mockPassedResult());

        assertTrue(resultListener.isPassed());
        assertNull(resultListener.getFirstError());
    }

    @Test
    public void should_not_be_passed_for_failed_result() throws Exception {
        Result result = mockFailedResult();
        FeatureResultListener resultListener = new FeatureResultListener(false);

        resultListener.collectError(result);


        assertFalse(resultListener.isPassed());
        assertEquals(resultListener.getFirstError(), result.getError());
    }

    @Test
    public void should_be_passed_for_undefined_result() throws Exception {
        FeatureResultListener resultListener = new FeatureResultListener(false);

        resultListener.collectError(mockUndefinedResult());

        assertTrue(resultListener.isPassed());
        assertNull(resultListener.getFirstError());
    }

    @Test
    public void should_not_be_passed_for_undefined_result_in_strict_mode() throws Exception {
        FeatureResultListener resultListener = new FeatureResultListener(true);

        resultListener.collectError(mockUndefinedResult());


        assertFalse(resultListener.isPassed());
        assertEquals(resultListener.getFirstError().getMessage(), FeatureResultListener.UNDEFINED_MESSAGE);
    }

    @Test
    public void should_be_passed_for_pending_result() throws Exception {
        FeatureResultListener resultListener = new FeatureResultListener(false);

        resultListener.collectError(mockPendingResult());

        assertTrue(resultListener.isPassed());
        assertNull(resultListener.getFirstError());
    }

    @Test
    public void should_not_be_passed_for_pending_result_in_strict_mode() throws Exception {
        FeatureResultListener resultListener = new FeatureResultListener(true);

        resultListener.collectError(mockPendingResult());

        assertFalse(resultListener.isPassed());
        assertEquals(resultListener.getFirstError().getMessage(), FeatureResultListener.PENDING_MESSAGE);
    }

    @Test
    public void should_collect_first_error() throws Exception {
        Result result = mockFailedResult();
        FeatureResultListener resultListener = new FeatureResultListener(false);

        resultListener.collectError(result);
        resultListener.collectError(mockFailedResult());
        resultListener.collectError(mockPendingResult());
        resultListener.collectError(mockUndefinedResult());

        assertEquals(resultListener.getFirstError(), result.getError());
    }

    @Test
    public void should_collect_error_after_undefined() throws Exception {
        Result result = mockFailedResult();
        FeatureResultListener resultListener = new FeatureResultListener(true);

        resultListener.collectError(mockUndefinedResult());
        resultListener.collectError(result);

        assertEquals(resultListener.getFirstError(), result.getError());
    }

    @Test
    public void should_collect_error_after_pending() throws Exception {
        Result result = mockFailedResult();
        FeatureResultListener resultListener = new FeatureResultListener(true);

        resultListener.collectError(mockPendingResult());
        resultListener.collectError(result);

        assertEquals(resultListener.getFirstError(), result.getError());
    }

    @Test
    public void should_collect_pending_after_undefined() throws Exception {
        Result result = mockPendingResult();
        FeatureResultListener resultListener = new FeatureResultListener(true);

        resultListener.collectError(mockUndefinedResult());
        resultListener.collectError(result);

        assertEquals(resultListener.getFirstError().getMessage(), FeatureResultListener.PENDING_MESSAGE);
    }

    @Test
    public void should_reset_errors() throws Exception {
        FeatureResultListener resultListener = new FeatureResultListener(false);
        resultListener.collectError(mockFailedResult());

        resultListener.startFeature();

        assertTrue(resultListener.isPassed());
        assertNull(resultListener.getFirstError());
    }

    private Result mockPassedResult() {
        Result result = mockResult(Result.Type.PASSED);
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

    private Result mockPendingResult() {
        Result result = mockResult(Result.Type.PENDING);
        return result;
    }

    private Result mockResult(Result.Type status) {
        Result result = mock(Result.class);
        for (Result.Type type : Result.Type.values()) {
            when(result.is(type)).thenReturn(type == status);
        }
        return result;
    }
}
