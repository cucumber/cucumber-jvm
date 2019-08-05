package io.cucumber.junit;

import gherkin.pickles.PickleStep;
import io.cucumber.core.event.PickleStepTestStep;
import io.cucumber.core.event.Result;
import io.cucumber.core.event.Status;
import io.cucumber.core.event.TestCase;
import io.cucumber.core.event.TestCaseFinished;
import io.cucumber.core.event.TestCaseStarted;
import io.cucumber.core.eventbus.EventBus;
import io.cucumber.junit.JUnitReporter.EachTestNotifier;
import io.cucumber.junit.JUnitReporter.NoTestNotifier;
import io.cucumber.junit.PickleRunners.PickleRunner;
import org.junit.AssumptionViolatedException;
import org.junit.Test;
import org.junit.runner.Description;
import org.junit.runner.notification.Failure;
import org.junit.runner.notification.RunNotifier;
import org.mockito.ArgumentCaptor;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static java.time.Duration.ZERO;
import static java.util.Arrays.asList;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class JUnitReporterTest {

    private JUnitReporter jUnitReporter;
    private RunNotifier runNotifier;

    @Test
    public void test_case_started_fires_test_started_for_pickle() {
        createNonStrictReporter();
        PickleRunner pickleRunner = mockPickleRunner(Collections.emptyList());
        runNotifier = mock(RunNotifier.class);
        jUnitReporter.startExecutionUnit(pickleRunner, runNotifier);

        jUnitReporter.handleTestCaseStarted(new TestCaseStarted(Instant.now(), mock(TestCase.class)));

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
        createReporter(new JUnitOptionsBuilder().setStepNotifications(true).build());
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
        Result result = mockResult(Status.PASSED);

        jUnitReporter.handleStepResult(mock(PickleStepTestStep.class), result);

        verify(runNotifier).fireTestFinished(description);
    }

    @Test
    public void test_step_finished_fires_assumption_failed_and_test_finished_for_skipped_step() {
        createNonStrictReporter();
        createDefaultRunNotifier();
        Description description = mock(Description.class);
        setUpStepNotifierAndStepErrors(description);
        Result result = mockResult(Status.SKIPPED);

        jUnitReporter.handleStepResult(mock(PickleStepTestStep.class), result);

        ArgumentCaptor<Failure> failureArgumentCaptor = ArgumentCaptor.forClass(Failure.class);
        verify(runNotifier).fireTestAssumptionFailed(failureArgumentCaptor.capture());
        verify(runNotifier).fireTestFinished(description);

        Failure failure = failureArgumentCaptor.getValue();
        assertThat(failure.getDescription(), is(equalTo(description)));
        assertThat(failure.getException(), instanceOf(SkippedThrowable.class));
        assertThat(failure.getException().getMessage(), is(equalTo("This step is skipped")));
    }

    @Test
    public void test_step_finished_fires_assumption_failed_and_test_finished_for_skipped_step_with_assumption_violated() {
        createNonStrictReporter();
        createDefaultRunNotifier();
        Description description = mock(Description.class);
        setUpStepNotifierAndStepErrors(description);
        Throwable exception = new AssumptionViolatedException("Oops");
        Result result = mockResult(Status.SKIPPED, exception);

        jUnitReporter.handleStepResult(mock(PickleStepTestStep.class), result);

        ArgumentCaptor<Failure> failureArgumentCaptor = ArgumentCaptor.forClass(Failure.class);
        verify(runNotifier).fireTestAssumptionFailed(failureArgumentCaptor.capture());
        verify(runNotifier).fireTestFinished(description);

        Failure failure = failureArgumentCaptor.getValue();
        assertThat(failure.getDescription(), is(equalTo(description)));
        assertThat(failure.getException(), is(equalTo(exception)));
    }

    @Test
    public void test_step_finished_adds_no_step_exeption_for_skipped_step_without_exception() {
        createNonStrictReporter();
        createDefaultRunNotifier();
        setUpNoStepNotifierAndStepErrors();
        Result result = mockResult(Status.SKIPPED);

        jUnitReporter.handleStepResult(mock(PickleStepTestStep.class), result);

        assertTrue(jUnitReporter.stepErrors.isEmpty());
    }

    @Test
    public void test_step_finished_adds_the_step_exeption_for_skipped_step_with_assumption_violated() {
        createNonStrictReporter();
        createDefaultRunNotifier();
        setUpNoStepNotifierAndStepErrors();
        Throwable exception = new AssumptionViolatedException("Oops");
        Result result = mockResult(Status.SKIPPED, exception);

        jUnitReporter.handleStepResult(mock(PickleStepTestStep.class), result);

        assertThat(jUnitReporter.stepErrors, is(equalTo(asList(exception))));
    }

    @Test
    public void test_step_finished_fires_assumption_failed_and_test_finished_for_pending_step_in_non_strict_mode() {
        createNonStrictReporter();
        createDefaultRunNotifier();
        Description description = mock(Description.class);
        setUpStepNotifierAndStepErrors(description);
        Throwable exception = new TestPendingException();
        Result result = mockResult(Status.PENDING, exception);

        jUnitReporter.handleStepResult(mock(PickleStepTestStep.class), result);

        ArgumentCaptor<Failure> failureArgumentCaptor = ArgumentCaptor.forClass(Failure.class);
        verify(runNotifier).fireTestAssumptionFailed(failureArgumentCaptor.capture());
        verify(runNotifier).fireTestFinished(description);

        Failure failure = failureArgumentCaptor.getValue();
        assertThat(failure.getDescription(), is(equalTo(description)));
        assertThat(failure.getException(), is(equalTo(exception)));
    }

    @Test
    public void test_step_finished_fires_assumption_failed_and_test_finished_for_pending_step_in_strict_mode() {
        createStrictReporter();
        createDefaultRunNotifier();
        Description description = mock(Description.class);
        setUpStepNotifierAndStepErrors(description);
        Throwable exception = new TestPendingException();
        Result result = mockResult(Status.PENDING, exception);

        jUnitReporter.handleStepResult(mock(PickleStepTestStep.class), result);

        ArgumentCaptor<Failure> failureArgumentCaptor = ArgumentCaptor.forClass(Failure.class);
        verify(runNotifier).fireTestFailure(failureArgumentCaptor.capture());
        verify(runNotifier).fireTestFinished(description);

        Failure failure = failureArgumentCaptor.getValue();
        assertThat(failure.getDescription(), is(equalTo(description)));
        assertThat(failure.getException(), is(equalTo(exception)));
    }

    @Test
    public void test_step_finished_adds_the_step_exeption_for_pending_steps() {
        createNonStrictReporter();
        createDefaultRunNotifier();
        setUpNoStepNotifierAndStepErrors();
        Throwable exception = new TestPendingException();
        Result result = mockResult(Status.PENDING, exception);

        jUnitReporter.handleStepResult(mock(PickleStepTestStep.class), result);

        assertThat(jUnitReporter.stepErrors, is(equalTo(asList(exception))));
    }

    @Test
    public void test_step_finished_fires_assumption_failed_and_test_finished_for_undefined_step_in_non_strict_mode() {
        createNonStrictReporter();
        createDefaultRunNotifier();
        Description description = mock(Description.class);
        setUpStepNotifierAndStepErrors(description);
        Result result = mockResult(Status.UNDEFINED);

        jUnitReporter.handleStepResult(mock(PickleStepTestStep.class), result);

        ArgumentCaptor<Failure> failureArgumentCaptor = ArgumentCaptor.forClass(Failure.class);
        verify(runNotifier).fireTestAssumptionFailed(failureArgumentCaptor.capture());
        verify(runNotifier).fireTestFinished(description);

        Failure failure = failureArgumentCaptor.getValue();
        assertThat(failure.getDescription(), is(equalTo(description)));
        assertThat(failure.getException(), instanceOf(UndefinedThrowable.class));
        assertThat(failure.getException().getMessage(), is(equalTo("This step is undefined")));
    }

    @Test
    public void test_step_finished_fires_failure_and_test_finished_for_undefined_step_in_strict_mode() {
        createStrictReporter();
        createDefaultRunNotifier();
        Description description = mock(Description.class);
        setUpStepNotifierAndStepErrors(description);
        Result result = mockResult(Status.UNDEFINED);

        jUnitReporter.handleStepResult(mock(PickleStepTestStep.class), result);

        ArgumentCaptor<Failure> failureArgumentCaptor = ArgumentCaptor.forClass(Failure.class);
        verify(runNotifier).fireTestFailure(failureArgumentCaptor.capture());
        verify(runNotifier).fireTestFinished(description);

        Failure failure = failureArgumentCaptor.getValue();
        assertThat(failure.getDescription(), is(equalTo(description)));
        assertThat(failure.getException(), instanceOf(UndefinedThrowable.class));
        assertThat(failure.getException().getMessage(), is(equalTo("This step is undefined")));
    }

    @Test
    public void test_step_finished_adds_a_step_exeption_for_undefined_steps() {
        createNonStrictReporter();
        createDefaultRunNotifier();
        setUpNoStepNotifierAndStepErrors();
        PickleStepTestStep testStep = mockTestStep("XX");
        Result result = mockResult(Status.UNDEFINED);

        jUnitReporter.handleStepResult(testStep, result);

        assertFalse(jUnitReporter.stepErrors.isEmpty());
        assertThat(jUnitReporter.stepErrors.get(0).getMessage(), is(equalTo("The step \"XX\" is undefined")));
    }

    @Test
    public void test_step_finished_fires_failure_and_test_finished_for_failed_step() {
        createNonStrictReporter();
        createDefaultRunNotifier();
        Description description = mock(Description.class);
        setUpStepNotifierAndStepErrors(description);
        Throwable exception = mock(Throwable.class);
        Result result = mockResult(Status.FAILED, exception);

        jUnitReporter.handleStepResult(mock(PickleStepTestStep.class), result);

        ArgumentCaptor<Failure> failureArgumentCaptor = ArgumentCaptor.forClass(Failure.class);
        verify(runNotifier).fireTestFailure(failureArgumentCaptor.capture());
        verify(runNotifier).fireTestFinished(description);

        Failure failure = failureArgumentCaptor.getValue();
        assertThat(failure.getDescription(), is(equalTo(description)));
        assertThat(failure.getException(), is(equalTo(exception)));
    }

    @Test
    public void test_step_finished_adds_the_step_exeption_for_failed_steps() {
        createNonStrictReporter();
        createDefaultRunNotifier();
        setUpNoStepNotifierAndStepErrors();
        Throwable exception = new TestPendingException();
        Result result = mockResult(Status.FAILED, exception);

        jUnitReporter.handleStepResult(mock(PickleStepTestStep.class), result);

        assertThat(jUnitReporter.stepErrors, is(equalTo(asList(exception))));
    }

    @Test
    public void test_case_finished_fires_only_test_finished_for_passed_step() {
        createNonStrictReporter();
        Description description = mock(Description.class);
        createRunNotifier(description);
        Result result = mockResult(Status.PASSED);

        jUnitReporter.handleTestCaseResult(new TestCaseFinished(Instant.now(), mock(TestCase.class), result));

        verify(runNotifier).fireTestFinished(description);
    }

    @Test
    public void test_case_finished_fires_assumption_failed_and_test_finished_for_skipped_step() {
        createNonStrictReporter();
        Description description = mock(Description.class);
        createRunNotifier(description);
        populateStepErrors(Collections.<Throwable>emptyList());
        Result result = mockResult(Status.SKIPPED);

        jUnitReporter.handleTestCaseResult(new TestCaseFinished(Instant.now(), mock(TestCase.class), result));

        ArgumentCaptor<Failure> failureArgumentCaptor = ArgumentCaptor.forClass(Failure.class);
        verify(runNotifier).fireTestAssumptionFailed(failureArgumentCaptor.capture());
        verify(runNotifier).fireTestFinished(description);

        Failure failure = failureArgumentCaptor.getValue();
        assertThat(failure.getDescription(), is(equalTo(description)));
        assertThat(failure.getException(), instanceOf(SkippedThrowable.class));
    }

    @Test
    public void test_case_finished_fires_assumption_failed_and_test_finished_for_skipped_step_with_assumption_violated() {
        createNonStrictReporter();
        Description description = mock(Description.class);
        createRunNotifier(description);
        Throwable exception1 = mock(AssumptionViolatedException.class);
        Throwable exception2 = mock(AssumptionViolatedException.class);
        populateStepErrors(asList(exception1, exception2));
        Result result = mockResult(Status.SKIPPED);

        jUnitReporter.handleTestCaseResult(new TestCaseFinished(Instant.now(), mock(TestCase.class), result));

        ArgumentCaptor<Failure> failureArgumentCaptor = ArgumentCaptor.forClass(Failure.class);
        verify(runNotifier, times(1)).fireTestAssumptionFailed(failureArgumentCaptor.capture());
        verify(runNotifier).fireTestFinished(description);

        List<Failure> failures = failureArgumentCaptor.getAllValues();
        assertThat(failures.get(0).getDescription(), is(equalTo(description)));
        assertThat(failures.get(0).getException(), is(equalTo(exception1)));
    }

    @Test
    public void test_case_finished_fires_assumption_failed_and_test_finished_for_pending_step_in_non_strict_mode() {
        createNonStrictReporter();
        Description description = mock(Description.class);
        createRunNotifier(description);
        Throwable exception1 = mock(Throwable.class);
        Throwable exception2 = mock(Throwable.class);
        populateStepErrors(asList(exception1, exception2));
        Result result = mockResult(Status.PENDING);

        jUnitReporter.handleTestCaseResult(new TestCaseFinished(Instant.now(), mock(TestCase.class), result));

        ArgumentCaptor<Failure> failureArgumentCaptor = ArgumentCaptor.forClass(Failure.class);
        verify(runNotifier, times(1)).fireTestAssumptionFailed(failureArgumentCaptor.capture());
        verify(runNotifier).fireTestFinished(description);

        List<Failure> failures = failureArgumentCaptor.getAllValues();
        assertThat(failures.get(0).getDescription(), is(equalTo(description)));
        assertThat(failures.get(0).getException(), is(equalTo(exception1)));
    }

    @Test
    public void test_case_finished_fires_failure_and_test_finished_for_pending_step_in_strict_mode() {
        createStrictReporter();
        Description description = mock(Description.class);
        createRunNotifier(description);
        Throwable exception1 = mock(Throwable.class);
        Throwable exception2 = mock(Throwable.class);
        populateStepErrors(asList(exception1, exception2));
        Result result = mockResult(Status.PENDING);

        jUnitReporter.handleTestCaseResult(new TestCaseFinished(Instant.now(), mock(TestCase.class), result));

        ArgumentCaptor<Failure> failureArgumentCaptor = ArgumentCaptor.forClass(Failure.class);
        verify(runNotifier, times(1)).fireTestFailure(failureArgumentCaptor.capture());
        verify(runNotifier).fireTestFinished(description);

        List<Failure> failures = failureArgumentCaptor.getAllValues();
        assertThat(failures.get(0).getDescription(), is(equalTo(description)));
        assertThat(failures.get(0).getException(), is(equalTo(exception1)));
    }

    @Test
    public void test_case_finished_fires_assumption_failed_and_test_finished_for_undefined_step_in_non_strict_mode() {
        createNonStrictReporter();
        Description description = mock(Description.class);
        createRunNotifier(description);
        Throwable exception1 = mock(Throwable.class);
        Throwable exception2 = mock(Throwable.class);
        populateStepErrors(asList(exception1, exception2));
        Result result = mockResult(Status.UNDEFINED);

        jUnitReporter.handleTestCaseResult(new TestCaseFinished(Instant.now(), mock(TestCase.class), result));

        ArgumentCaptor<Failure> failureArgumentCaptor = ArgumentCaptor.forClass(Failure.class);
        verify(runNotifier, times(1)).fireTestAssumptionFailed(failureArgumentCaptor.capture());
        verify(runNotifier).fireTestFinished(description);

        List<Failure> failures = failureArgumentCaptor.getAllValues();
        assertThat(failures.get(0).getDescription(), is(equalTo(description)));
        assertThat(failures.get(0).getException(), is(equalTo(exception1)));
    }

    @Test
    public void test_case_finished_fires_failure_and_test_finished_for_undefined_step_in_strict_mode() {
        createStrictReporter();
        Description description = mock(Description.class);
        createRunNotifier(description);
        Throwable exception1 = mock(Throwable.class);
        Throwable exception2 = mock(Throwable.class);
        populateStepErrors(asList(exception1, exception2));
        Result result = mockResult(Status.UNDEFINED);

        jUnitReporter.handleTestCaseResult(new TestCaseFinished(Instant.now(), mock(TestCase.class), result));

        ArgumentCaptor<Failure> failureArgumentCaptor = ArgumentCaptor.forClass(Failure.class);
        verify(runNotifier, times(1)).fireTestFailure(failureArgumentCaptor.capture());
        verify(runNotifier).fireTestFinished(description);

        List<Failure> failures = failureArgumentCaptor.getAllValues();
        assertThat(failures.get(0).getDescription(), is(equalTo(description)));
        assertThat(failures.get(0).getException(), is(equalTo(exception1)));
    }

    @Test
    public void test_case_finished_fires_failure_and_test_finished_for_failed_step() {
        createNonStrictReporter();
        Description description = mock(Description.class);
        createRunNotifier(description);
        Throwable exception1 = mock(Throwable.class);
        Throwable exception2 = mock(Throwable.class);
        populateStepErrors(asList(exception1, exception2));
        Result result = mockResult(Status.FAILED);

        jUnitReporter.handleTestCaseResult(new TestCaseFinished(Instant.now(), mock(TestCase.class), result));

        ArgumentCaptor<Failure> failureArgumentCaptor = ArgumentCaptor.forClass(Failure.class);
        verify(runNotifier, times(2)).fireTestFailure(failureArgumentCaptor.capture());
        verify(runNotifier).fireTestFinished(description);

        List<Failure> failures = failureArgumentCaptor.getAllValues();
        assertThat(failures.get(0).getDescription(), is(equalTo(description)));
        assertThat(failures.get(0).getException(), is(equalTo(exception1)));
        assertThat(failures.get(1).getDescription(), is(equalTo(description)));
        assertThat(failures.get(1).getException(), is(equalTo(exception2)));
    }

    private Result mockResult(Status status, Throwable exception) {
        return new Result(status, ZERO, exception);
    }

    private Result mockResult(Status status) {
        return new Result(status, ZERO, null);
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

    private void createStrictReporter() {
        createReporter(new JUnitOptionsBuilder().setStrict(true).build());
    }

    private void createNonStrictReporter() {
        createReporter(new JUnitOptions());
    }

    private void createReporter(JUnitOptions options) {
        jUnitReporter = new JUnitReporter(mock(EventBus.class), options);
    }

    private void setUpStepNotifierAndStepErrors(Description description) {
        jUnitReporter.stepNotifier = new EachTestNotifier(runNotifier, description);
        jUnitReporter.stepErrors = new ArrayList<>();
    }

    private void setUpNoStepNotifierAndStepErrors() {
        jUnitReporter.stepNotifier = new NoTestNotifier();
        jUnitReporter.stepErrors = new ArrayList<>();
    }

    private void populateStepErrors(List<Throwable> exceptions) {
        jUnitReporter.stepErrors = new ArrayList<>();
        jUnitReporter.stepErrors.addAll(exceptions);
    }

}
