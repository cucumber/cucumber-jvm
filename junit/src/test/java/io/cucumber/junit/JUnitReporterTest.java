package io.cucumber.junit;

import cucumber.api.PickleStepTestStep;
import cucumber.api.Result;
import cucumber.runner.EventBus;
import io.cucumber.junit.JUnitReporter.EachTestNotifier;
import io.cucumber.junit.JUnitReporter.NoTestNotifier;
import io.cucumber.junit.PickleRunners.PickleRunner;
import gherkin.pickles.PickleStep;
import org.junit.AssumptionViolatedException;
import org.junit.Test;
import org.junit.runner.Description;
import org.junit.runner.notification.Failure;
import org.junit.runner.notification.RunNotifier;
import org.mockito.ArgumentCaptor;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static java.util.Arrays.asList;


public class JUnitReporterTest {

    private JUnitReporter jUnitReporter;
    private RunNotifier runNotifier;

    @Test
    public void test_case_started_fires_test_started_for_pickle() {
        createNonStrictReporter();
        PickleRunner pickleRunner = mockPickleRunner(Collections.<PickleStep>emptyList());
        runNotifier = mock(RunNotifier.class);
        jUnitReporter.startExecutionUnit(pickleRunner, runNotifier);

        jUnitReporter.handleTestCaseStarted();

        verify(runNotifier).fireTestStarted(pickleRunner.getDescription());
    }

    @Test
    public void test_step_started_does_not_fire_test_started_for_step_by_default() {
        createNonStrictReporter();
        PickleStep runnerStep = mockStep();
        PickleRunner pickleRunner = mockPickleRunner(runnerSteps(runnerStep));
        runNotifier = mock(RunNotifier.class);
        jUnitReporter.startExecutionUnit(pickleRunner, runNotifier);

        jUnitReporter.handleStepStarted(runnerStep);

        verify(runNotifier, never()).fireTestStarted(pickleRunner.describeChild(runnerStep));
    }

    @Test
    public void test_step_started_fires_test_started_for_step_when_using_step_notifications() {
        createNonStrictReporter("--step-notifications");
        PickleStep runnerStep = mockStep();
        PickleRunner pickleRunner = mockPickleRunner(runnerSteps(runnerStep));
        runNotifier = mock(RunNotifier.class);
        jUnitReporter.startExecutionUnit(pickleRunner, runNotifier);

        jUnitReporter.handleStepStarted(runnerStep);

        verify(runNotifier).fireTestStarted(pickleRunner.describeChild(runnerStep));
    }

    @Test
    public void test_step_finished_fires_only_test_finished_for_passed_step() {
        createNonStrictReporter();
        createDefaultRunNotifier();
        Description description = mock(Description.class);
        setUpStepNotifierAndStepErrors(description);
        Result result = mockResult(Result.Type.PASSED);

        jUnitReporter.handleStepResult(mock(PickleStepTestStep.class), result);

        verify(runNotifier).fireTestFinished(description);
    }

    @Test
    public void test_step_finished_fires_assumption_failed_and_test_finished_for_skipped_step() {
        createNonStrictReporter();
        createDefaultRunNotifier();
        Description description = mock(Description.class);
        setUpStepNotifierAndStepErrors(description);
        Result result = mockResult(Result.Type.SKIPPED);

        jUnitReporter.handleStepResult(mock(PickleStepTestStep.class), result);

        ArgumentCaptor<Failure> failureArgumentCaptor = ArgumentCaptor.forClass(Failure.class);
        verify(runNotifier).fireTestAssumptionFailed(failureArgumentCaptor.capture());
        verify(runNotifier).fireTestFinished(description);

        Failure failure = failureArgumentCaptor.getValue();
        assertEquals(description, failure.getDescription());
        assertTrue(failure.getException() instanceof SkippedThrowable);
        assertEquals("This step is skipped", failure.getException().getMessage());
    }

    @Test
    public void test_step_finished_fires_assumption_failed_and_test_finished_for_skipped_step_with_assumption_violated() {
        createNonStrictReporter();
        createDefaultRunNotifier();
        Description description = mock(Description.class);
        setUpStepNotifierAndStepErrors(description);
        Throwable exception = new AssumptionViolatedException("Oops");
        Result result = mockResult(Result.Type.SKIPPED, exception);

        jUnitReporter.handleStepResult(mock(PickleStepTestStep.class), result);

        ArgumentCaptor<Failure> failureArgumentCaptor = ArgumentCaptor.forClass(Failure.class);
        verify(runNotifier).fireTestAssumptionFailed(failureArgumentCaptor.capture());
        verify(runNotifier).fireTestFinished(description);

        Failure failure = failureArgumentCaptor.getValue();
        assertEquals(description, failure.getDescription());
        assertEquals(exception, failure.getException());
    }

    @Test
    public void test_step_finished_adds_no_step_exeption_for_skipped_step_without_exception() {
        createNonStrictReporter();
        createDefaultRunNotifier();
        setUpNoStepNotifierAndStepErrors();
        Result result = mockResult(Result.Type.SKIPPED);

        jUnitReporter.handleStepResult(mock(PickleStepTestStep.class), result);

        assertTrue(jUnitReporter.stepErrors.isEmpty());
    }

    @Test
    public void test_step_finished_adds_the_step_exeption_for_skipped_step_with_assumption_violated() {
        createNonStrictReporter();
        createDefaultRunNotifier();
        setUpNoStepNotifierAndStepErrors();
        Throwable exception = new AssumptionViolatedException("Oops");
        Result result = mockResult(Result.Type.SKIPPED, exception);

        jUnitReporter.handleStepResult(mock(PickleStepTestStep.class), result);

        assertEquals(asList(exception), jUnitReporter.stepErrors);
    }

    @Test
    public void test_step_finished_fires_assumption_failed_and_test_finished_for_pending_step_in_non_strict_mode() {
        createNonStrictReporter();
        createDefaultRunNotifier();
        Description description = mock(Description.class);
        setUpStepNotifierAndStepErrors(description);
        Throwable exception = new TestPendingException();
        Result result = mockResult(Result.Type.PENDING, exception);

        jUnitReporter.handleStepResult(mock(PickleStepTestStep.class), result);

        ArgumentCaptor<Failure> failureArgumentCaptor = ArgumentCaptor.forClass(Failure.class);
        verify(runNotifier).fireTestAssumptionFailed(failureArgumentCaptor.capture());
        verify(runNotifier).fireTestFinished(description);

        Failure failure = failureArgumentCaptor.getValue();
        assertEquals(description, failure.getDescription());
        assertEquals(exception, failure.getException());
    }

    @Test
    public void test_step_finished_fires_assumption_failed_and_test_finished_for_pending_step_in_strict_mode() {
        createStrictReporter();
        createDefaultRunNotifier();
        Description description = mock(Description.class);
        setUpStepNotifierAndStepErrors(description);
        Throwable exception = new TestPendingException();
        Result result = mockResult(Result.Type.PENDING, exception);

        jUnitReporter.handleStepResult(mock(PickleStepTestStep.class), result);

        ArgumentCaptor<Failure> failureArgumentCaptor = ArgumentCaptor.forClass(Failure.class);
        verify(runNotifier).fireTestFailure(failureArgumentCaptor.capture());
        verify(runNotifier).fireTestFinished(description);

        Failure failure = failureArgumentCaptor.getValue();
        assertEquals(description, failure.getDescription());
        assertEquals(exception, failure.getException());
    }

    @Test
    public void test_step_finished_adds_the_step_exeption_for_pending_steps() {
        createNonStrictReporter();
        createDefaultRunNotifier();
        setUpNoStepNotifierAndStepErrors();
        Throwable exception = new TestPendingException();
        Result result = mockResult(Result.Type.PENDING, exception);

        jUnitReporter.handleStepResult(mock(PickleStepTestStep.class), result);

        assertEquals(asList(exception), jUnitReporter.stepErrors);
    }

    @Test
    public void test_step_finished_fires_assumption_failed_and_test_finished_for_undefined_step_in_non_strict_mode() {
        createNonStrictReporter();
        createDefaultRunNotifier();
        Description description = mock(Description.class);
        setUpStepNotifierAndStepErrors(description);
        Result result = mockResult(Result.Type.UNDEFINED);

        jUnitReporter.handleStepResult(mock(PickleStepTestStep.class), result);

        ArgumentCaptor<Failure> failureArgumentCaptor = ArgumentCaptor.forClass(Failure.class);
        verify(runNotifier).fireTestAssumptionFailed(failureArgumentCaptor.capture());
        verify(runNotifier).fireTestFinished(description);

        Failure failure = failureArgumentCaptor.getValue();
        assertEquals(description, failure.getDescription());
        assertTrue(failure.getException() instanceof UndefinedThrowable);
        assertEquals("This step is undefined", failure.getException().getMessage());
    }

    @Test
    public void test_step_finished_fires_failure_and_test_finished_for_undefined_step_in_strict_mode() {
        createStrictReporter();
        createDefaultRunNotifier();
        Description description = mock(Description.class);
        setUpStepNotifierAndStepErrors(description);
        Result result = mockResult(Result.Type.UNDEFINED);

        jUnitReporter.handleStepResult(mock(PickleStepTestStep.class), result);

        ArgumentCaptor<Failure> failureArgumentCaptor = ArgumentCaptor.forClass(Failure.class);
        verify(runNotifier).fireTestFailure(failureArgumentCaptor.capture());
        verify(runNotifier).fireTestFinished(description);

        Failure failure = failureArgumentCaptor.getValue();
        assertEquals(description, failure.getDescription());
        assertTrue(failure.getException() instanceof UndefinedThrowable);
        assertEquals("This step is undefined", failure.getException().getMessage());
    }

    @Test
    public void test_step_finished_adds_a_step_exeption_for_undefined_steps() {
        createNonStrictReporter();
        createDefaultRunNotifier();
        setUpNoStepNotifierAndStepErrors();
        PickleStepTestStep testStep = mockTestStep("XX");
        Result result = mockResult(Result.Type.UNDEFINED);

        jUnitReporter.handleStepResult(testStep, result);

        assertFalse(jUnitReporter.stepErrors.isEmpty());
        assertEquals("The step \"XX\" is undefined", jUnitReporter.stepErrors.get(0).getMessage());
    }

    @Test
    public void test_step_finished_fires_failure_and_test_finished_for_failed_step() {
        createNonStrictReporter();
        createDefaultRunNotifier();
        Description description = mock(Description.class);
        setUpStepNotifierAndStepErrors(description);
        Throwable exception = mock(Throwable.class);
        Result result = mockResult(Result.Type.FAILED, exception);

        jUnitReporter.handleStepResult(mock(PickleStepTestStep.class), result);

        ArgumentCaptor<Failure> failureArgumentCaptor = ArgumentCaptor.forClass(Failure.class);
        verify(runNotifier).fireTestFailure(failureArgumentCaptor.capture());
        verify(runNotifier).fireTestFinished(description);

        Failure failure = failureArgumentCaptor.getValue();
        assertEquals(description, failure.getDescription());
        assertEquals(exception, failure.getException());
    }

    @Test
    public void test_step_finished_adds_the_step_exeption_for_failed_steps() {
        createNonStrictReporter();
        createDefaultRunNotifier();
        setUpNoStepNotifierAndStepErrors();
        Throwable exception = new TestPendingException();
        Result result = mockResult(Result.Type.FAILED, exception);

        jUnitReporter.handleStepResult(mock(PickleStepTestStep.class), result);

        assertEquals(asList(exception), jUnitReporter.stepErrors);
    }

    @Test
    public void test_case_finished_fires_only_test_finished_for_passed_step() {
        createNonStrictReporter();
        Description description = mock(Description.class);
        createRunNotifier(description);
        Result result = mockResult(Result.Type.PASSED);

        jUnitReporter.handleTestCaseResult(result);

        verify(runNotifier).fireTestFinished(description);
    }

    @Test
    public void test_case_finished_fires_assumption_failed_and_test_finished_for_skipped_step() {
        createNonStrictReporter();
        Description description = mock(Description.class);
        createRunNotifier(description);
        populateStepErrors(Collections.<Throwable>emptyList());
        Result result = mockResult(Result.Type.SKIPPED);

        jUnitReporter.handleTestCaseResult(result);

        ArgumentCaptor<Failure> failureArgumentCaptor = ArgumentCaptor.forClass(Failure.class);
        verify(runNotifier).fireTestAssumptionFailed(failureArgumentCaptor.capture());
        verify(runNotifier).fireTestFinished(description);

        Failure failure = failureArgumentCaptor.getValue();
        assertEquals(description, failure.getDescription());
        assertTrue(failure.getException() instanceof SkippedThrowable);
    }

    @Test
    public void test_case_finished_fires_assumption_failed_and_test_finished_for_skipped_step_with_assumption_violated() {
        createNonStrictReporter();
        Description description = mock(Description.class);
        createRunNotifier(description);
        Throwable exception1 = mock(AssumptionViolatedException.class);
        Throwable exception2 = mock(AssumptionViolatedException.class);
        populateStepErrors(asList(exception1, exception2));
        Result result = mockResult(Result.Type.SKIPPED);

        jUnitReporter.handleTestCaseResult(result);

        ArgumentCaptor<Failure> failureArgumentCaptor = ArgumentCaptor.forClass(Failure.class);
        verify(runNotifier, times(2)).fireTestAssumptionFailed(failureArgumentCaptor.capture());
        verify(runNotifier).fireTestFinished(description);

        List<Failure> failures = failureArgumentCaptor.getAllValues();
        assertEquals(description, failures.get(0).getDescription());
        assertEquals(exception1, failures.get(0).getException());
        assertEquals(description, failures.get(1).getDescription());
        assertEquals(exception2, failures.get(1).getException());
    }

    @Test
    public void test_case_finished_fires_assumption_failed_and_test_finished_for_pending_step_in_non_strict_mode() {
        createNonStrictReporter();
        Description description = mock(Description.class);
        createRunNotifier(description);
        Throwable exception1 = mock(Throwable.class);
        Throwable exception2 = mock(Throwable.class);
        populateStepErrors(asList(exception1, exception2));
        Result result = mockResult(Result.Type.PENDING);

        jUnitReporter.handleTestCaseResult(result);

        ArgumentCaptor<Failure> failureArgumentCaptor = ArgumentCaptor.forClass(Failure.class);
        verify(runNotifier, times(2)).fireTestAssumptionFailed(failureArgumentCaptor.capture());
        verify(runNotifier).fireTestFinished(description);

        List<Failure> failures = failureArgumentCaptor.getAllValues();
        assertEquals(description, failures.get(0).getDescription());
        assertEquals(exception1, failures.get(0).getException());
        assertEquals(description, failures.get(1).getDescription());
        assertEquals(exception2, failures.get(1).getException());
    }

    @Test
    public void test_case_finished_fires_failure_and_test_finished_for_pending_step_in_strict_mode() {
        createStrictReporter();
        Description description = mock(Description.class);
        createRunNotifier(description);
        Throwable exception1 = mock(Throwable.class);
        Throwable exception2 = mock(Throwable.class);
        populateStepErrors(asList(exception1, exception2));
        Result result = mockResult(Result.Type.PENDING);

        jUnitReporter.handleTestCaseResult(result);

        ArgumentCaptor<Failure> failureArgumentCaptor = ArgumentCaptor.forClass(Failure.class);
        verify(runNotifier, times(2)).fireTestFailure(failureArgumentCaptor.capture());
        verify(runNotifier).fireTestFinished(description);

        List<Failure> failures = failureArgumentCaptor.getAllValues();
        assertEquals(description, failures.get(0).getDescription());
        assertEquals(exception1, failures.get(0).getException());
        assertEquals(description, failures.get(1).getDescription());
        assertEquals(exception2, failures.get(1).getException());
    }

    @Test
    public void test_case_finished_fires_assumption_failed_and_test_finished_for_undefined_step_in_non_strict_mode() {
        createNonStrictReporter();
        Description description = mock(Description.class);
        createRunNotifier(description);
        Throwable exception1 = mock(Throwable.class);
        Throwable exception2 = mock(Throwable.class);
        populateStepErrors(asList(exception1, exception2));
        Result result = mockResult(Result.Type.UNDEFINED);

        jUnitReporter.handleTestCaseResult(result);

        ArgumentCaptor<Failure> failureArgumentCaptor = ArgumentCaptor.forClass(Failure.class);
        verify(runNotifier, times(2)).fireTestAssumptionFailed(failureArgumentCaptor.capture());
        verify(runNotifier).fireTestFinished(description);

        List<Failure> failures = failureArgumentCaptor.getAllValues();
        assertEquals(description, failures.get(0).getDescription());
        assertEquals(exception1, failures.get(0).getException());
        assertEquals(description, failures.get(1).getDescription());
        assertEquals(exception2, failures.get(1).getException());
    }

    @Test
    public void test_case_finished_fires_failure_and_test_finished_for_undefined_step_in_strict_mode() {
        createStrictReporter();
        Description description = mock(Description.class);
        createRunNotifier(description);
        Throwable exception1 = mock(Throwable.class);
        Throwable exception2 = mock(Throwable.class);
        populateStepErrors(asList(exception1, exception2));
        Result result = mockResult(Result.Type.UNDEFINED);

        jUnitReporter.handleTestCaseResult(result);

        ArgumentCaptor<Failure> failureArgumentCaptor = ArgumentCaptor.forClass(Failure.class);
        verify(runNotifier, times(2)).fireTestFailure(failureArgumentCaptor.capture());
        verify(runNotifier).fireTestFinished(description);

        List<Failure> failures = failureArgumentCaptor.getAllValues();
        assertEquals(description, failures.get(0).getDescription());
        assertEquals(exception1, failures.get(0).getException());
        assertEquals(description, failures.get(1).getDescription());
        assertEquals(exception2, failures.get(1).getException());
    }

    @Test
    public void test_case_finished_fires_failure_and_test_finished_for_failed_step() {
        createNonStrictReporter();
        Description description = mock(Description.class);
        createRunNotifier(description);
        Throwable exception1 = mock(Throwable.class);
        Throwable exception2 = mock(Throwable.class);
        populateStepErrors(asList(exception1, exception2));
        Result result = mockResult(Result.Type.FAILED);

        jUnitReporter.handleTestCaseResult(result);

        ArgumentCaptor<Failure> failureArgumentCaptor = ArgumentCaptor.forClass(Failure.class);
        verify(runNotifier, times(2)).fireTestFailure(failureArgumentCaptor.capture());
        verify(runNotifier).fireTestFinished(description);

        List<Failure> failures = failureArgumentCaptor.getAllValues();
        assertEquals(description, failures.get(0).getDescription());
        assertEquals(exception1, failures.get(0).getException());
        assertEquals(description, failures.get(1).getDescription());
        assertEquals(exception2, failures.get(1).getException());
    }

    private Result mockResult(Result.Type status, Throwable exception) {
        return new Result(status, 0L, exception);
    }

    private Result mockResult(Result.Type status) {
        return new Result(status, 0L, null);
    }

    private PickleRunner mockPickleRunner(List<PickleStep> runnerSteps) {
        PickleRunner pickleRunner = mock(PickleRunner.class);
        when(pickleRunner.getDescription()).thenReturn(mock(Description.class));
        for (PickleStep runnerStep : runnerSteps) {
            Description runnerStepDescription = stepDescription(runnerStep);
            when(pickleRunner.describeChild(runnerStep)).thenReturn(runnerStepDescription);
        }
        return pickleRunner;
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

    private PickleStepTestStep mockTestStep(String stepText) {
        PickleStepTestStep testStep = mock(PickleStepTestStep.class);
        when(testStep.getStepText()).thenReturn(stepText);
        return testStep;
    }

    private void createDefaultRunNotifier() {
        createRunNotifier(mock(Description.class));
    }

    private void createRunNotifier(Description description) {
        runNotifier = mock(RunNotifier.class);
        PickleRunner pickleRunner = mock(PickleRunner.class);
        when(pickleRunner.getDescription()).thenReturn(description);
        jUnitReporter.startExecutionUnit(pickleRunner, runNotifier);
    }

    private void createStrictReporter(String... options) {
        jUnitReporter = new JUnitReporter(mock(EventBus.class), new JUnitOptions(true, asList(options)));
    }

    private void createNonStrictReporter(String... options) {
        jUnitReporter = new JUnitReporter(mock(EventBus.class), new JUnitOptions(false, asList(options)));
    }

    private void setUpStepNotifierAndStepErrors(Description description) {
        jUnitReporter.stepNotifier = new EachTestNotifier(runNotifier, description);
        jUnitReporter.stepErrors = new ArrayList<Throwable>();
    }

    private void setUpNoStepNotifierAndStepErrors() {
        jUnitReporter.stepNotifier = new NoTestNotifier();
        jUnitReporter.stepErrors = new ArrayList<Throwable>();
    }

    private void populateStepErrors(List<Throwable> exceptions) {
        jUnitReporter.stepErrors = new ArrayList<Throwable>();
        for (Throwable exception : exceptions) {
            jUnitReporter.stepErrors.add(exception);
        }
    }
}
