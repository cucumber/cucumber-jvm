package cucumber.runtime.junit;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import gherkin.formatter.Formatter;
import gherkin.formatter.Reporter;
import gherkin.formatter.model.Result;

import org.junit.Test;
import org.junit.runner.Description;
import org.junit.runner.notification.Failure;
import org.junit.runner.notification.RunNotifier;
import org.mockito.ArgumentCaptor;

import cucumber.api.PendingException;

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
        jUnitReporter.finishExecutionUnit();

        ArgumentCaptor<Failure> failureArgumentCaptor = ArgumentCaptor.forClass(Failure.class);
        verify(runNotifier).fireTestFailure(failureArgumentCaptor.capture());

        Failure failure = failureArgumentCaptor.getValue();
        assertEquals(description, failure.getDescription());
        assertEquals(exception, failure.getException());
    }

    @Test
    public void result_with_undefined_step_non_strict() {
        createNonStrictReporter();
        createDefaultRunNotifier();
        jUnitReporter.result(Result.UNDEFINED);
        jUnitReporter.finishExecutionUnit();

        verify(runNotifier, never()).fireTestStarted(any(Description.class));
        verify(runNotifier, never()).fireTestFinished(any(Description.class));
        verify(runNotifier, never()).fireTestFailure(any(Failure.class));
        verify(runNotifier).fireTestIgnored(any(Description.class));
    }

    @Test
    public void result_with_undefined_step_strict() {
        createStrictReporter();
        createDefaultRunNotifier();

        jUnitReporter.result(Result.UNDEFINED);
        jUnitReporter.finishExecutionUnit();

        verify(runNotifier).fireTestStarted(any(Description.class));
        verify(runNotifier).fireTestFinished(any(Description.class));
        verify(runNotifier).fireTestFailure(any(Failure.class));
        verify(runNotifier, never()).fireTestIgnored(any(Description.class));

    }

    @Test
    public void result_with_pending_step_non_strict() {
        createNonStrictReporter();
        createDefaultRunNotifier();
        Result result = mock(Result.class);
        when(result.getError()).thenReturn(new PendingException());

        
        jUnitReporter.result(result);
        jUnitReporter.finishExecutionUnit();

        verify(runNotifier, never()).fireTestStarted(any(Description.class));
        verify(runNotifier, never()).fireTestFinished(any(Description.class));
        verify(runNotifier, never()).fireTestFailure(any(Failure.class));
        verify(runNotifier).fireTestIgnored(any(Description.class));
    }

    @Test
    public void result_with_pending_step_strict() {
        createStrictReporter();
        createDefaultRunNotifier();
        Result result = mock(Result.class);
        when(result.getError()).thenReturn(new PendingException());

        jUnitReporter.result(result);
        jUnitReporter.finishExecutionUnit();

        verify(runNotifier).fireTestStarted(any(Description.class));
        verify(runNotifier).fireTestFinished(any(Description.class));
        verify(runNotifier).fireTestFailure(any(Failure.class));
        verify(runNotifier, never()).fireTestIgnored(any(Description.class));
    }

    @Test
    public void result_without_error_non_strict() {
        createNonStrictReporter();
        createDefaultRunNotifier();
        Result result = mock(Result.class);

        jUnitReporter.result(result);
        jUnitReporter.finishExecutionUnit();

        verify(runNotifier).fireTestStarted(any(Description.class));
        verify(runNotifier).fireTestFinished(any(Description.class));
        verify(runNotifier, never()).fireTestFailure(any(Failure.class));
        verify(runNotifier, never()).fireTestIgnored(any(Description.class));
    }

    @Test
    public void result_without_error_strict() {
        createStrictReporter();
        createDefaultRunNotifier();
        Result result = mock(Result.class);

        jUnitReporter.result(result);
        jUnitReporter.finishExecutionUnit();

        verify(runNotifier).fireTestStarted(any(Description.class));
        verify(runNotifier).fireTestFinished(any(Description.class));
        verify(runNotifier, never()).fireTestFailure(any(Failure.class));
        verify(runNotifier, never()).fireTestIgnored(any(Description.class));
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
