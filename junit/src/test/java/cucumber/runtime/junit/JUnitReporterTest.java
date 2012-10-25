package cucumber.runtime.junit;

import cucumber.runtime.PendingException;
import gherkin.formatter.Formatter;
import gherkin.formatter.Reporter;
import gherkin.formatter.model.Result;
import org.junit.Test;
import org.junit.internal.runners.model.EachTestNotifier;
import org.junit.runner.Description;
import org.junit.runner.notification.Failure;
import org.junit.runner.notification.RunNotifier;
import org.mockito.ArgumentCaptor;
import org.mockito.Matchers;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class JUnitReporterTest {

    private JUnitReporter jUnitReporter;
    private RunNotifier runNotifier;

    @Test
    public void resultWithError() {
        createNonStrictReporter();
        Result result = mock(Result.class);
        Throwable exception = mock(Throwable.class);
        when(result.getError()).thenReturn(exception);

        Description description = mock(Description.class);
        createRunNotifier(description);

        jUnitReporter.result(result);

        ArgumentCaptor<Failure> failureArgumentCaptor = ArgumentCaptor.forClass(Failure.class);
        verify(runNotifier).fireTestFailure(failureArgumentCaptor.capture());

        Failure failure = failureArgumentCaptor.getValue();
        assertEquals(description, failure.getDescription());
        assertEquals(exception, failure.getException());
    }

    @Test
    public void result_with_undefined_step_non_strict() {
        createNonStrictReporter();
        EachTestNotifier stepNotifier = mock(EachTestNotifier.class);
        jUnitReporter.stepNotifier = stepNotifier;

        jUnitReporter.result(Result.UNDEFINED);

        verify(stepNotifier, times(0)).fireTestStarted();
        verify(stepNotifier, times(0)).fireTestFinished();
        verify(stepNotifier, times(0)).addFailure(Matchers.<Throwable>any(Throwable.class));
        verify(stepNotifier).fireTestIgnored();
    }

    @Test
    public void result_with_undefined_step_strict() {
        createStrictReporter();
        createDefaultRunNotifier();
        EachTestNotifier stepNotifier = mock(EachTestNotifier.class);
        jUnitReporter.stepNotifier = stepNotifier;
        EachTestNotifier executionUnitNotifier = mock(EachTestNotifier.class);
        jUnitReporter.executionUnitNotifier = executionUnitNotifier;

        jUnitReporter.result(Result.UNDEFINED);

        verify(stepNotifier, times(0)).fireTestStarted();
        verify(stepNotifier, times(0)).fireTestFinished();
        verifyAddFailureWithPendingException(stepNotifier);
        verifyAddFailureWithPendingException(executionUnitNotifier);
        verify(stepNotifier, times(0)).fireTestIgnored();
    }

    private void verifyAddFailureWithPendingException(EachTestNotifier stepNotifier) {
        ArgumentCaptor<Throwable> captor = ArgumentCaptor.forClass(Throwable.class);
        verify(stepNotifier).addFailure(captor.capture());
        Throwable error = captor.getValue();
        assertTrue(error instanceof PendingException);
    }

    @Test
    public void result_with_pending_step_non_strict() {
        createNonStrictReporter();
        Result result = mock(Result.class);
        when(result.getError()).thenReturn(new PendingException());

        EachTestNotifier stepNotifier = mock(EachTestNotifier.class);
        jUnitReporter.stepNotifier = stepNotifier;

        jUnitReporter.result(result);

        verify(stepNotifier, times(0)).fireTestStarted();
        verify(stepNotifier, times(0)).fireTestFinished();
        verify(stepNotifier, times(0)).addFailure(Matchers.<Throwable>any(Throwable.class));
        verify(stepNotifier).fireTestIgnored();
    }

    @Test
    public void result_with_pending_step_strict() {
        createStrictReporter();
        createDefaultRunNotifier();
        Result result = mock(Result.class);
        when(result.getError()).thenReturn(new PendingException());

        EachTestNotifier stepNotifier = mock(EachTestNotifier.class);
        jUnitReporter.stepNotifier = stepNotifier;
        EachTestNotifier executionUnitNotifier = mock(EachTestNotifier.class);
        jUnitReporter.executionUnitNotifier = executionUnitNotifier;

        jUnitReporter.result(result);

        verify(stepNotifier, times(0)).fireTestStarted();
        verify(stepNotifier, times(0)).fireTestFinished();
        verifyAddFailureWithPendingException(stepNotifier);
        verifyAddFailureWithPendingException(executionUnitNotifier);
        verify(stepNotifier, times(0)).fireTestIgnored();
    }

    @Test
    public void result_without_error_non_strict() {
        createNonStrictReporter();
        Result result = mock(Result.class);

        EachTestNotifier stepNotifier = mock(EachTestNotifier.class);
        jUnitReporter.stepNotifier = stepNotifier;

        jUnitReporter.result(result);

        verify(stepNotifier).fireTestStarted();
        verify(stepNotifier).fireTestFinished();
        verify(stepNotifier, times(0)).addFailure(Matchers.<Throwable>any(Throwable.class));
        verify(stepNotifier, times(0)).fireTestIgnored();
    }

    @Test
    public void result_without_error_strict() {
        createStrictReporter();
        Result result = mock(Result.class);

        EachTestNotifier stepNotifier = mock(EachTestNotifier.class);
        jUnitReporter.stepNotifier = stepNotifier;

        jUnitReporter.result(result);

        verify(stepNotifier).fireTestStarted();
        verify(stepNotifier).fireTestFinished();
        verify(stepNotifier, times(0)).addFailure(Matchers.<Throwable>any(Throwable.class));
        verify(stepNotifier, times(0)).fireTestIgnored();
    }

    private void createDefaultRunNotifier() {
        createRunNotifier(mock(Description.class));
    }

    private void createRunNotifier(Description description) {
        runNotifier = mock(RunNotifier.class);
        ExecutionUnitRunner executionUnitRunner = mock(ExecutionUnitRunner.class);
        when(executionUnitRunner.getDescription()).thenReturn(description);
        jUnitReporter.startExecutionUnit(executionUnitRunner, runNotifier);
    }

    private void createStrictReporter() {
        createReporter(true);
    }

    private void createNonStrictReporter() {
        createReporter(false);
    }

    private void createReporter(boolean strict) {
        Formatter formatter = mock(Formatter.class);
        Reporter reporter = mock(Reporter.class);

        jUnitReporter = new JUnitReporter(reporter, formatter, strict);
    }

}
