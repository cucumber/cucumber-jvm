package io.cucumber.junit;

import io.cucumber.core.eventbus.EventBus;
import io.cucumber.core.exception.CucumberException;
import io.cucumber.core.gherkin.Feature;
import io.cucumber.core.gherkin.Step;
import io.cucumber.core.runtime.TimeServiceEventBus;
import io.cucumber.junit.PickleRunners.PickleRunner;
import io.cucumber.plugin.event.HookTestStep;
import io.cucumber.plugin.event.Location;
import io.cucumber.plugin.event.PickleStepTestStep;
import io.cucumber.plugin.event.Result;
import io.cucumber.plugin.event.SnippetsSuggestedEvent;
import io.cucumber.plugin.event.SnippetsSuggestedEvent.Suggestion;
import io.cucumber.plugin.event.Status;
import io.cucumber.plugin.event.TestCase;
import io.cucumber.plugin.event.TestCaseFinished;
import io.cucumber.plugin.event.TestCaseStarted;
import io.cucumber.plugin.event.TestStepFinished;
import io.cucumber.plugin.event.TestStepStarted;
import org.junit.AssumptionViolatedException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.runner.Description;
import org.junit.runner.notification.Failure;
import org.junit.runner.notification.RunNotifier;
import org.junit.runners.model.MultipleFailureException;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoSettings;

import java.net.URI;
import java.time.Clock;
import java.util.List;
import java.util.UUID;

import static java.time.Duration.ZERO;
import static java.time.Instant.now;
import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.runner.Description.createTestDescription;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@MockitoSettings
final class JUnitReporterWithStepNotificationsTest {

    private static final Location scenarioLine = new Location(0, 0);
    private static final URI featureUri = URI.create("file:example.feature");
    private final EventBus bus = new TimeServiceEventBus(Clock.systemUTC(), UUID::randomUUID);
    private final JUnitReporter jUnitReporter = new JUnitReporter(bus,
        new JUnitOptionsBuilder().setStepNotifications(true).build());
    private final Feature feature = TestFeatureParser.parse("""
            Feature: Test feature
              Scenario: Test scenario
                 Given step name
            """);
    private final Step step = feature.getPickles().get(0).getSteps().get(0);

    private final Description runnerStepDescription = createTestDescription("", step.getText());
    @Mock
    private TestCase testCase;
    @Mock
    private Description pickleRunnerDescription;

    @Mock
    private PickleRunner pickleRunner;
    @Mock
    private RunNotifier runNotifier;

    @Captor
    private ArgumentCaptor<Failure> failureArgumentCaptor;

    @BeforeEach
    void mockPickleRunner() {
        when(pickleRunner.getDescription()).thenReturn(pickleRunnerDescription);
        lenient().when(pickleRunner.describeChild(step)).thenReturn(runnerStepDescription);
    }

    @Test
    void test_step_started_fires_test_started_for_step() {
        jUnitReporter.startExecutionUnit(pickleRunner, runNotifier);
        bus.send(new TestCaseStarted(now(), testCase));
        bus.send(new TestStepStarted(now(), testCase, mockTestStep(step)));
        jUnitReporter.finishExecutionUnit();
    }

    private static PickleStepTestStep mockTestStep(Step step) {
        PickleStepTestStep testStep = mock(PickleStepTestStep.class);
        lenient().when(testStep.getUri()).thenReturn(featureUri);
        lenient().when(testStep.getStep()).thenReturn(step);
        return testStep;
    }

    @Test
    void disconnects_from_bus_once_execution_unit_finished() {
        jUnitReporter.startExecutionUnit(pickleRunner, runNotifier);
        jUnitReporter.finishExecutionUnit();
        bus.send(new TestCaseStarted(now(), testCase));
        verify(runNotifier, never()).fireTestStarted(runnerStepDescription);
    }

    @Test
    void ignores_steps_when_step_notification_are_disabled() {
        EventBus bus = new TimeServiceEventBus(Clock.systemUTC(), UUID::randomUUID);
        JUnitReporter jUnitReporter = new JUnitReporter(bus, new JUnitOptionsBuilder()
                .setStepNotifications(false)
                .build());

        jUnitReporter.startExecutionUnit(pickleRunner, runNotifier);

        bus.send(new TestCaseStarted(now(), testCase));
        bus.send(new TestStepStarted(now(), testCase, mockTestStep(step)));

        Result result = new Result(Status.PASSED, ZERO, null);
        bus.send(new TestStepFinished(now(), testCase, mockTestStep(step), result));
        bus.send(new TestCaseFinished(now(), testCase, result));

        verify(runNotifier, never()).fireTestStarted(runnerStepDescription);
        verify(runNotifier, never()).fireTestFinished(runnerStepDescription);
    }

    @Test
    void test_case_finished_fires_test_finished_for_pickle() {
        jUnitReporter.startExecutionUnit(pickleRunner, runNotifier);

        bus.send(new TestCaseStarted(now(), testCase));
        bus.send(new TestStepStarted(now(), testCase, mockTestStep(step)));

        Result result = new Result(Status.PASSED, ZERO, null);
        bus.send(new TestStepFinished(now(), testCase, mockTestStep(step), result));
        bus.send(new TestCaseFinished(now(), testCase, result));

        verify(runNotifier).fireTestStarted(runnerStepDescription);
        verify(runNotifier).fireTestFinished(runnerStepDescription);
    }

    @Test
    void test_step_finished_fires_assumption_failed_and_test_finished_for_skipped_step() {
        jUnitReporter.startExecutionUnit(pickleRunner, runNotifier);

        bus.send(new TestCaseStarted(now(), testCase));
        bus.send(new TestStepStarted(now(), testCase, mockTestStep(step)));
        Result result = new Result(Status.SKIPPED, ZERO, null);
        bus.send(new TestStepFinished(now(), testCase, mockTestStep(step), result));

        verify(runNotifier).fireTestAssumptionFailed(failureArgumentCaptor.capture());
        verify(runNotifier).fireTestFinished(runnerStepDescription);
        Failure stepFailure = failureArgumentCaptor.getValue();
        assertThat(stepFailure.getDescription(), is(equalTo(runnerStepDescription)));
        assertThat(stepFailure.getException(), instanceOf(SkippedThrowable.class));
        assertThat(stepFailure.getException().getMessage(), is(equalTo("This step is skipped")));

        bus.send(new TestCaseFinished(now(), testCase, result));

        verify(runNotifier, times(2)).fireTestAssumptionFailed(failureArgumentCaptor.capture());
        Failure pickleFailure = failureArgumentCaptor.getValue();
        assertThat(pickleFailure.getDescription(), is(equalTo(pickleRunnerDescription)));
        assertThat(pickleFailure.getException(), instanceOf(SkippedThrowable.class));
        assertThat(pickleFailure.getException().getMessage(), is(equalTo("This scenario is skipped")));

    }

    @Test
    void test_step_finished_fires_assumption_failed_and_test_finished_for_skipped_step_with_assumption_violated() {
        jUnitReporter.startExecutionUnit(pickleRunner, runNotifier);

        bus.send(new TestCaseStarted(now(), testCase));
        bus.send(new TestStepStarted(now(), testCase, mockTestStep(step)));
        Throwable exception = new AssumptionViolatedException("Oops");
        Result result = new Result(Status.SKIPPED, ZERO, exception);
        bus.send(new TestStepFinished(now(), testCase, mockTestStep(step), result));

        verify(runNotifier).fireTestAssumptionFailed(failureArgumentCaptor.capture());
        verify(runNotifier).fireTestFinished(runnerStepDescription);

        Failure stepFailure = failureArgumentCaptor.getValue();
        assertThat(stepFailure.getDescription(), is(equalTo(runnerStepDescription)));
        assertThat(stepFailure.getException(), is(equalTo(exception)));

        bus.send(new TestCaseFinished(now(), testCase, result));

        verify(runNotifier, times(2)).fireTestAssumptionFailed(failureArgumentCaptor.capture());
        verify(runNotifier).fireTestFinished(runnerStepDescription);

        Failure pickleFailure = failureArgumentCaptor.getValue();
        assertThat(pickleFailure.getDescription(), is(equalTo(pickleRunnerDescription)));
        assertThat(pickleFailure.getException(), is(equalTo(exception)));
    }

    @Test
    void test_step_finished_fires_test_failure_and_test_finished_for_skipped_step_with_pending_exception() {
        jUnitReporter.startExecutionUnit(pickleRunner, runNotifier);

        bus.send(new TestCaseStarted(now(), testCase));
        bus.send(new TestStepStarted(now(), testCase, mockTestStep(step)));
        Throwable exception = new TestPendingException("Oops");
        Result result = new Result(Status.PENDING, ZERO, exception);
        bus.send(new TestStepFinished(now(), testCase, mockTestStep(step), result));

        verify(runNotifier).fireTestFailure(failureArgumentCaptor.capture());
        verify(runNotifier).fireTestFinished(runnerStepDescription);

        Failure stepFailure = failureArgumentCaptor.getValue();
        assertThat(stepFailure.getDescription(), is(equalTo(runnerStepDescription)));
        assertThat(stepFailure.getException(), is(equalTo(exception)));

        bus.send(new TestCaseFinished(now(), testCase, result));

        verify(runNotifier, times(2)).fireTestFailure(failureArgumentCaptor.capture());
        verify(runNotifier).fireTestFinished(runnerStepDescription);

        Failure pickleFailure = failureArgumentCaptor.getValue();
        assertThat(pickleFailure.getDescription(), is(equalTo(pickleRunnerDescription)));
        assertThat(pickleFailure.getException(), is(equalTo(exception)));

    }

    @Test
    void test_step_undefined_fires_test_failure_and_test_finished_for_undefined_step() {
        jUnitReporter.startExecutionUnit(pickleRunner, runNotifier);

        Suggestion suggestion = new Suggestion("step name", singletonList("some snippet"));
        bus.send(new SnippetsSuggestedEvent(now(), featureUri, scenarioLine, scenarioLine, suggestion));
        bus.send(new TestCaseStarted(now(), testCase));
        bus.send(new TestStepStarted(now(), testCase, mockTestStep(step)));
        Throwable exception = new CucumberException("No step definitions found");
        Result result = new Result(Status.UNDEFINED, ZERO, exception);
        bus.send(new TestStepFinished(now(), testCase, mockTestStep(step), result));

        verify(runNotifier).fireTestFailure(failureArgumentCaptor.capture());
        verify(runNotifier).fireTestFinished(runnerStepDescription);

        Failure stepFailure = failureArgumentCaptor.getValue();
        assertThat(stepFailure.getDescription(), is(equalTo(runnerStepDescription)));
        assertThat(stepFailure.getException(), is(equalTo(exception)));

        bus.send(new TestCaseFinished(now(), testCase, result));

        verify(runNotifier, times(2)).fireTestFailure(failureArgumentCaptor.capture());
        verify(runNotifier).fireTestFinished(runnerStepDescription);

        Failure pickleFailure = failureArgumentCaptor.getValue();
        assertThat(pickleFailure.getDescription(), is(equalTo(pickleRunnerDescription)));
        assertThat(pickleFailure.getException().getMessage(), is("""
                The step 'step name' is undefined.
                You can implement this step using the snippet(s) below:

                some snippet
                """));
    }

    @Test
    void test_step_finished_fires_test_failure_and_test_finished_for_failed_step() {
        jUnitReporter.startExecutionUnit(pickleRunner, runNotifier);

        bus.send(new TestCaseStarted(now(), testCase));
        bus.send(new TestStepStarted(now(), testCase, mockTestStep(step)));
        Throwable exception = new Exception("Oops");
        Result result = new Result(Status.FAILED, ZERO, exception);
        bus.send(new TestStepFinished(now(), testCase, mockTestStep(step), result));

        verify(runNotifier).fireTestFailure(failureArgumentCaptor.capture());
        verify(runNotifier).fireTestFinished(runnerStepDescription);

        Failure stepFailure = failureArgumentCaptor.getValue();
        assertThat(stepFailure.getDescription(), is(equalTo(runnerStepDescription)));
        assertThat(stepFailure.getException(), is(equalTo(exception)));

        bus.send(new TestCaseFinished(now(), testCase, result));

        verify(runNotifier, times(2)).fireTestFailure(failureArgumentCaptor.capture());
        verify(runNotifier).fireTestFinished(runnerStepDescription);

        Failure pickleFailure = failureArgumentCaptor.getValue();
        assertThat(pickleFailure.getDescription(), is(equalTo(pickleRunnerDescription)));
        assertThat(pickleFailure.getException(), is(equalTo(exception)));
    }

    @Test
    void test_step_finished_fires_test_failure_and_test_finished_for_failed_hook() {

        jUnitReporter.startExecutionUnit(pickleRunner, runNotifier);

        bus.send(new TestCaseStarted(now(), testCase));
        bus.send(new TestStepStarted(now(), testCase, mockTestStep(step)));
        Result stepResult = new Result(Status.PASSED, ZERO, null);
        bus.send(new TestStepFinished(now(), testCase, mockTestStep(step), stepResult));

        bus.send(new TestStepStarted(now(), testCase, mock(HookTestStep.class)));
        Throwable exception = new Exception("Oops");
        Result result = new Result(Status.FAILED, ZERO, exception);
        bus.send(new TestStepFinished(now(), testCase, mock(HookTestStep.class), result));

        // Hooks are not included in step failure
        verify(runNotifier, never()).fireTestFailure(failureArgumentCaptor.capture());
        verify(runNotifier).fireTestFinished(runnerStepDescription);

        bus.send(new TestCaseFinished(now(), testCase, result));

        verify(runNotifier).fireTestFailure(failureArgumentCaptor.capture());
        verify(runNotifier).fireTestFinished(runnerStepDescription);

        Failure pickleFailure = failureArgumentCaptor.getValue();
        assertThat(pickleFailure.getDescription(), is(equalTo(pickleRunnerDescription)));
        assertThat(pickleFailure.getException(), is(equalTo(exception)));
    }

    @Test
    void test_step_finished_fires_test_failure_and_test_finished_for_failed_step_with_multiple_failure_exception() {
        jUnitReporter.startExecutionUnit(pickleRunner, runNotifier);

        bus.send(new TestCaseStarted(now(), testCase));
        bus.send(new TestStepStarted(now(), testCase, mockTestStep(step)));
        List<Throwable> failures = asList(
            new Exception("Oops"),
            new Exception("I did it again"));
        Throwable exception = new MultipleFailureException(failures);
        Result result = new Result(Status.FAILED, ZERO, exception);
        bus.send(new TestStepFinished(now(), testCase, mockTestStep(step), result));

        verify(runNotifier, times(2)).fireTestFailure(failureArgumentCaptor.capture());
        verify(runNotifier).fireTestFinished(runnerStepDescription);

        List<Failure> stepFailure = failureArgumentCaptor.getAllValues();

        assertThat(stepFailure.get(0).getDescription(), is(equalTo(runnerStepDescription)));
        assertThat(stepFailure.get(0).getException(), is(equalTo(failures.get(0))));

        assertThat(stepFailure.get(1).getDescription(), is(equalTo(runnerStepDescription)));
        assertThat(stepFailure.get(1).getException(), is(equalTo(failures.get(1))));

        bus.send(new TestCaseFinished(now(), testCase, result));

        verify(runNotifier, times(4)).fireTestFailure(failureArgumentCaptor.capture());
        verify(runNotifier).fireTestFinished(runnerStepDescription);

        List<Failure> pickleFailure = failureArgumentCaptor.getAllValues();

        // Mockito recapture all arguments on .capture() so we end up with the
        // original 2, those 2 repeated and the finally the 2 we expect.
        assertThat(pickleFailure.get(4).getDescription(), is(equalTo(pickleRunnerDescription)));
        assertThat(pickleFailure.get(4).getException(), is(equalTo(failures.get(0))));

        assertThat(pickleFailure.get(5).getDescription(), is(equalTo(pickleRunnerDescription)));
        assertThat(pickleFailure.get(5).getException(), is(equalTo(failures.get(1))));
    }

}
