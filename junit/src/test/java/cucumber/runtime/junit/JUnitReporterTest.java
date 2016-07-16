package cucumber.runtime.junit;

import cucumber.api.PendingException;
import cucumber.api.Result;
import cucumber.runner.EventBus;
import gherkin.pickles.PickleStep;
import org.junit.Test;
import org.junit.internal.runners.model.EachTestNotifier;
import org.junit.runner.Description;
import org.junit.runner.notification.Failure;
import org.junit.runner.notification.RunNotifier;
import org.mockito.ArgumentCaptor;
import org.mockito.Matchers;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static java.util.Arrays.asList;
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
    public void match_allow_started_ignored() {
        createAllowStartedIgnoredReporter();
        PickleStep runnerStep = mockStep();
        Description runnerStepDescription = stepDescription(runnerStep);
        ExecutionUnitRunner executionUnitRunner = mockExecutionUnitRunner(runnerSteps(runnerStep));
        when(executionUnitRunner.describeChild(runnerStep)).thenReturn(runnerStepDescription);
        runNotifier = mock(RunNotifier.class);

        jUnitReporter.startExecutionUnit(executionUnitRunner, runNotifier);
        jUnitReporter.handleStepStarted(runnerStep);

        verify(runNotifier).fireTestStarted(executionUnitRunner.getDescription());
        verify(runNotifier).fireTestStarted(runnerStepDescription);
    }

    @Test
    public void resultWithError() {
        createNonStrictReporter();
        Result result = mock(Result.class);
        Throwable exception = mock(Throwable.class);
        when(result.getError()).thenReturn(exception);

        Description description = mock(Description.class);
        createRunNotifier(description);

        jUnitReporter.handleStepResult(result);

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

        jUnitReporter.handleStepResult(mockResult(Result.UNDEFINED));

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

        jUnitReporter.handleStepResult(mockResult(Result.UNDEFINED));

        verify(stepNotifier, times(1)).fireTestStarted();
        verify(stepNotifier, times(1)).fireTestFinished();
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

        jUnitReporter.handleStepResult(result);

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

        jUnitReporter.handleStepResult(result);

        verify(stepNotifier, times(1)).fireTestStarted();
        verify(stepNotifier, times(1)).fireTestFinished();
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

        jUnitReporter.handleStepResult(result);

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

        jUnitReporter.handleStepResult(result);

        verify(stepNotifier).fireTestStarted();
        verify(stepNotifier).fireTestFinished();
        verify(stepNotifier, times(0)).addFailure(Matchers.<Throwable>any(Throwable.class));
        verify(stepNotifier, times(0)).fireTestIgnored();
    }

    @Test
    public void result_without_error_allow_stared_ignored() {
        createAllowStartedIgnoredReporter();
        Result result = mock(Result.class);

        EachTestNotifier stepNotifier = mock(EachTestNotifier.class);
        jUnitReporter.stepNotifier = stepNotifier;

        jUnitReporter.handleStepResult(result);

        verify(stepNotifier, times(0)).fireTestStarted();
        verify(stepNotifier).fireTestFinished();
        verify(stepNotifier, times(0)).addFailure(Matchers.<Throwable>any(Throwable.class));
        verify(stepNotifier, times(0)).fireTestIgnored();
    }

    @Test
    public void hook_with_pending_exception_strict() {
        createStrictReporter();
        createDefaultRunNotifier();
        Result result = mock(Result.class);
        when(result.getStatus()).thenReturn("Pending");
        when(result.getError()).thenReturn(new PendingException());

        EachTestNotifier executionUnitNotifier = mock(EachTestNotifier.class);
        jUnitReporter.executionUnitNotifier = executionUnitNotifier;

        jUnitReporter.handleHookResult(result);

        verifyAddFailureWithPendingException(executionUnitNotifier);
    }

    @Test
    public void hook_with_pending_exception_non_strict() {
        createNonStrictReporter();
        createDefaultRunNotifier();
        Result result = mock(Result.class);
        when(result.getStatus()).thenReturn("Pending");
        when(result.getError()).thenReturn(new PendingException());

        EachTestNotifier executionUnitNotifier = mock(EachTestNotifier.class);
        jUnitReporter.executionUnitNotifier = executionUnitNotifier;

        jUnitReporter.handleHookResult(result);
        jUnitReporter.finishExecutionUnit();

        verify(executionUnitNotifier).fireTestIgnored();
    }

    @Test
    public void failed_step_and_after_with_pending_exception_non_strict() {
        createNonStrictReporter();
        createDefaultRunNotifier();
        Result stepResult = mock(Result.class);
        Throwable exception = mock(Throwable.class);
        when(stepResult.getError()).thenReturn(exception);
        Result hookResult = mock(Result.class);
        when(hookResult.getStatus()).thenReturn("Pending");
        when(hookResult.getError()).thenReturn(new PendingException());

        EachTestNotifier executionUnitNotifier = mock(EachTestNotifier.class);
        jUnitReporter.executionUnitNotifier = executionUnitNotifier;

        jUnitReporter.handleStepResult(stepResult);
        jUnitReporter.handleHookResult(hookResult);
        jUnitReporter.finishExecutionUnit();

        verify(executionUnitNotifier, times(0)).fireTestIgnored();
    }

    @Test
    public void creates_step_notifier_with_step_from_execution_unit_runner() throws Exception {
        PickleStep runnerStep = mockStep("Step Name");
        Description runnerStepDescription = stepDescription(runnerStep);
        ExecutionUnitRunner executionUnitRunner = mockExecutionUnitRunner(runnerSteps(runnerStep));
        when(executionUnitRunner.describeChild(runnerStep)).thenReturn(runnerStepDescription);
        RunNotifier notifier = mock(RunNotifier.class);
        jUnitReporter = new JUnitReporter(mock(EventBus.class), false, new JUnitOptions(Collections.<String>emptyList()));

        jUnitReporter.startExecutionUnit(executionUnitRunner, notifier);
        jUnitReporter.handleStepStarted(runnerStep);
        jUnitReporter.handleStepResult(mockResult());

        verify(notifier).fireTestFinished(runnerStepDescription);
    }

    private Result mockResult() {
        return mockResult("passed");
    }

    private Result mockResult(String status) {
        Result result = mock(Result.class);
        when(result.getStatus()).thenReturn(status);
        return result;
    }

    private ExecutionUnitRunner mockExecutionUnitRunner(List<PickleStep> runnerSteps) {
        ExecutionUnitRunner executionUnitRunner = mock(ExecutionUnitRunner.class);
        when(executionUnitRunner.getDescription()).thenReturn(mock(Description.class));
        return executionUnitRunner;
    }

    private List<PickleStep> runnerSteps(PickleStep step) {
        List<PickleStep> runnerSteps = new ArrayList<PickleStep>();
        runnerSteps.add(step);
        return runnerSteps;
    }

    private Description stepDescription(PickleStep runnerStep) {
        return Description.createTestDescription("", runnerStep.getText());
    }

    private PickleStep mockStep() {
        String stepName = "step name";
        return mockStep(stepName);
    }

    private PickleStep mockStep(String stepName) {
        PickleStep step = mock(PickleStep.class);
        when(step.getText()).thenReturn(stepName);
        return step;
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
        createReporter(true, false);
    }

    private void createNonStrictReporter() {
        createReporter(false, false);
    }

    private void createAllowStartedIgnoredReporter() {
        createReporter(false, true);
    }

    private void createReporter(boolean strict, boolean allowStartedIgnored) {
        String allowStartedIgnoredOption = allowStartedIgnored ? "--allow-started-ignored" : "--no-allow-started-ignored";
        jUnitReporter = new JUnitReporter(mock(EventBus.class), strict, new JUnitOptions(asList(allowStartedIgnoredOption)));
    }

}
