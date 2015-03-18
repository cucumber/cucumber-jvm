package cucumber.api.testng;

import gherkin.formatter.Reporter;
import gherkin.formatter.model.Match;
import gherkin.formatter.model.Result;
import org.testng.annotations.Test;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertNull;
import static org.testng.Assert.assertTrue;

public class FeatureResultListenerTest {

    @Test
    public void should_be_passed_for_passed_result() throws Exception {
        FeatureResultListener resultListener = new FeatureResultListener(mock(Reporter.class), false);

        resultListener.result(mockPassedResult());

        assertTrue(resultListener.isPassed());
        assertNull(resultListener.getFirstError());
    }

    @Test
    public void should_not_be_passed_for_failed_result() throws Exception {
        Result result = mockFailedResult();
        FeatureResultListener resultListener = new FeatureResultListener(mock(Reporter.class), false);

        resultListener.result(result);

        assertFalse(resultListener.isPassed());
        assertEquals(resultListener.getFirstError(), result.getError());
    }

    @Test
    public void should_be_passed_for_undefined_result() throws Exception {
        FeatureResultListener resultListener = new FeatureResultListener(mock(Reporter.class), false);

        resultListener.result(Result.UNDEFINED);

        assertTrue(resultListener.isPassed());
        assertNull(resultListener.getFirstError());
    }

    @Test
    public void should_not_be_passed_for_undefined_result_in_strict_mode() throws Exception {
        FeatureResultListener resultListener = new FeatureResultListener(mock(Reporter.class), true);

        resultListener.result(Result.UNDEFINED);

        assertFalse(resultListener.isPassed());
        assertEquals(resultListener.getFirstError().getMessage(), FeatureResultListener.UNDEFINED_MESSAGE);
    }

    @Test
    public void should_be_passed_for_pending_result() throws Exception {
        FeatureResultListener resultListener = new FeatureResultListener(mock(Reporter.class), false);

        resultListener.result(mockPendingResult());

        assertTrue(resultListener.isPassed());
        assertNull(resultListener.getFirstError());
    }

    @Test
    public void should_not_be_passed_for_pending_result_in_strict_mode() throws Exception {
        FeatureResultListener resultListener = new FeatureResultListener(mock(Reporter.class), true);

        resultListener.result(mockPendingResult());

        assertFalse(resultListener.isPassed());
        assertEquals(resultListener.getFirstError().getMessage(), FeatureResultListener.PENDING_MESSAGE);
    }

    @Test
    public void should_collect_first_error() throws Exception {
        Result result = mockFailedResult();
        FeatureResultListener resultListener = new FeatureResultListener(mock(Reporter.class), false);

        resultListener.result(result);
        resultListener.result(mockFailedResult());
        resultListener.result(mockPendingResult());
        resultListener.result(Result.UNDEFINED);

        assertEquals(resultListener.getFirstError(), result.getError());
    }

    @Test
    public void should_collect_error_after_undefined() throws Exception {
        Result result = mockFailedResult();
        FeatureResultListener resultListener = new FeatureResultListener(mock(Reporter.class), true);

        resultListener.result(Result.UNDEFINED);
        resultListener.result(result);

        assertEquals(resultListener.getFirstError(), result.getError());
    }

    @Test
    public void should_collect_error_after_pending() throws Exception {
        Result result = mockFailedResult();
        FeatureResultListener resultListener = new FeatureResultListener(mock(Reporter.class), true);

        resultListener.result(mockPendingResult());
        resultListener.result(result);

        assertEquals(resultListener.getFirstError(), result.getError());
    }

    @Test
    public void should_collect_pending_after_undefined() throws Exception {
        Result result = mockPendingResult();
        FeatureResultListener resultListener = new FeatureResultListener(mock(Reporter.class), true);

        resultListener.result(Result.UNDEFINED);
        resultListener.result(result);

        assertEquals(resultListener.getFirstError().getMessage(), FeatureResultListener.PENDING_MESSAGE);
    }

    @Test
    public void should_not_be_passed_for_failed_before_hook() throws Exception {
        Result result = mockFailedResult();
        FeatureResultListener resultListener = new FeatureResultListener(mock(Reporter.class), false);

        resultListener.before(mock(Match.class), result);

        assertFalse(resultListener.isPassed());
        assertEquals(resultListener.getFirstError(), result.getError());
    }

    @Test
    public void should_not_be_passed_for_failed_after_hook() throws Exception {
        Result result = mockFailedResult();
        FeatureResultListener resultListener = new FeatureResultListener(mock(Reporter.class), false);

        resultListener.after(mock(Match.class), result);

        assertFalse(resultListener.isPassed());
        assertEquals(resultListener.getFirstError(), result.getError());
    }

    @Test
    public void should_reset_errors() throws Exception {
        FeatureResultListener resultListener = new FeatureResultListener(mock(Reporter.class), false);
        resultListener.result(mockFailedResult());

        resultListener.startFeature();

        assertTrue(resultListener.isPassed());
        assertNull(resultListener.getFirstError());
    }

    @Test
    public void should_forward_calls_to_reporter_interface_methods() throws Exception {
        Match match = mock(Match.class);
        Result result = mockPassedResult();
        String mimeType = "mimeType";
        byte data[] = new byte[] {1};
        String text = "text";
        Reporter reporter = mock(Reporter.class);
        FeatureResultListener resultListener = new FeatureResultListener(reporter, false);

        resultListener.before(match, result);
        resultListener.match(match);
        resultListener.embedding(mimeType, data);
        resultListener.write(text);
        resultListener.result(result);
        resultListener.after(match, result);

        verify(reporter).before(match, result);
        verify(reporter).match(match);
        verify(reporter).embedding(mimeType, data);
        verify(reporter).write(text);
        verify(reporter).result(result);
        verify(reporter).after(match, result);
    }

    private Result mockPassedResult() {
        Result result = mock(Result.class);
        when(result.getStatus()).thenReturn(Result.PASSED);
        return result;
    }

    private Result mockFailedResult() {
        Result result = mock(Result.class);
        when(result.getStatus()).thenReturn(Result.FAILED);
        when(result.getError()).thenReturn(mock(Throwable.class));
        return result;
    }

    private Result mockPendingResult() {
        Result result = mock(Result.class);
        when(result.getStatus()).thenReturn(FeatureResultListener.PENDING_STATUS);
        return result;
    }
}
