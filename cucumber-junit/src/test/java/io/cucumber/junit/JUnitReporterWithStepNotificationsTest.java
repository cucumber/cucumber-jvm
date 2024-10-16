package io.cucumber.junit;

import io.cucumber.core.eventbus.EventBus;
import io.cucumber.core.exception.CucumberException;
import io.cucumber.core.gherkin.Feature;
import io.cucumber.core.gherkin.Step;
import io.cucumber.core.runtime.TimeServiceEventBus;
import io.cucumber.junit.PickleRunners.PickleRunner;
import io.cucumber.plugin.event.Argument;
import io.cucumber.plugin.event.Location;
import io.cucumber.plugin.event.PickleStepTestStep;
import io.cucumber.plugin.event.Result;
import io.cucumber.plugin.event.SnippetsSuggestedEvent;
import io.cucumber.plugin.event.SnippetsSuggestedEvent.Suggestion;
import io.cucumber.plugin.event.Status;
import io.cucumber.plugin.event.StepArgument;
import io.cucumber.plugin.event.TestCase;
import io.cucumber.plugin.event.TestCaseFinished;
import io.cucumber.plugin.event.TestCaseStarted;
import io.cucumber.plugin.event.TestStep;
import io.cucumber.plugin.event.TestStepFinished;
import io.cucumber.plugin.event.TestStepStarted;
import org.junit.AssumptionViolatedException;
import org.junit.jupiter.api.Test;
import org.junit.runner.Description;
import org.junit.runner.notification.Failure;
import org.junit.runner.notification.RunNotifier;
import org.junit.runner.notification.StoppedByUserException;
import org.junit.runners.model.MultipleFailureException;

import java.net.URI;
import java.time.Clock;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static java.time.Duration.ZERO;
import static java.time.Instant.now;
import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class JUnitReporterWithStepNotificationsTest {

    private static final Location scenarioLine = new Location(0, 0);
    private static final URI featureUri = URI.create("file:example.feature");
    private final EventBus bus = new TimeServiceEventBus(Clock.systemUTC(), UUID::randomUUID);
    private final JUnitReporter jUnitReporter = new JUnitReporter(bus,
        new JUnitOptionsBuilder().setStepNotifications(true).build());
    private final Feature feature = TestFeatureParser.parse("" +
            "Feature: Test feature\n" +
            "  Scenario: Test scenario\n" +
            "     Given step name\n");
    private final Step step = feature.getPickles().get(0).getSteps().get(0);
    private final PickleRunner pickleRunner = new MockPickleRunner(step);
    private final PickleStepTestStep testStep = new StubPickleStepTestStep(featureUri, step);
    private final TestCase testCase = new StubTestCase();

    @Test
    void test_step_started_fires_test_started_for_step() {
        MockRunNotifier runNotifier = new MockRunNotifier();
        jUnitReporter.startExecutionUnit(pickleRunner, runNotifier);
        bus.send(new TestCaseStarted(now(), testCase));
        bus.send(new TestStepStarted(now(), testCase, testStep));
        jUnitReporter.finishExecutionUnit();
    }

    @Test
    void disconnects_from_bus_once_execution_unit_finished() {
        MockRunNotifier runNotifier = new MockRunNotifier();
        jUnitReporter.startExecutionUnit(pickleRunner, runNotifier);
        jUnitReporter.finishExecutionUnit();
        bus.send(new TestCaseStarted(now(), testCase));
        assertNull(runNotifier.testStartedDescription);
    }

    @Test
    void ignores_steps_when_step_notification_are_disabled() {
        EventBus bus = new TimeServiceEventBus(Clock.systemUTC(), UUID::randomUUID);
        JUnitReporter jUnitReporter = new JUnitReporter(bus, new JUnitOptionsBuilder()
                .setStepNotifications(false)
                .build());

        MockRunNotifier runNotifier = new MockRunNotifier();
        jUnitReporter.startExecutionUnit(pickleRunner, runNotifier);

        bus.send(new TestCaseStarted(now(), testCase));
        bus.send(new TestStepStarted(now(), testCase, testStep));

        Result result = new Result(Status.PASSED, ZERO, null);
        bus.send(new TestStepFinished(now(), testCase, testStep, result));
        bus.send(new TestCaseFinished(now(), testCase, result));

        assertNull(runNotifier.testStartedDescription);
        assertNull(runNotifier.testFinishedDescription);
    }

    @Test
    void test_case_finished_fires_test_finished_for_pickle() {
        MockRunNotifier runNotifier = new MockRunNotifier();
        jUnitReporter.startExecutionUnit(pickleRunner, runNotifier);

        bus.send(new TestCaseStarted(now(), testCase));
        bus.send(new TestStepStarted(now(), testCase, testStep));

        Result result = new Result(Status.PASSED, ZERO, null);
        bus.send(new TestStepFinished(now(), testCase, testStep, result));
        bus.send(new TestCaseFinished(now(), testCase, result));

        assertEquals(pickleRunner.describeChild(step), runNotifier.testStartedDescription);
        assertEquals(pickleRunner.describeChild(step), runNotifier.testFinishedDescription);
    }

    @Test
    void test_step_finished_fires_assumption_failed_and_test_finished_for_skipped_step() {
        MockRunNotifier runNotifier = new MockRunNotifier();
        jUnitReporter.startExecutionUnit(pickleRunner, runNotifier);

        bus.send(new TestCaseStarted(now(), testCase));
        bus.send(new TestStepStarted(now(), testCase, testStep));
        Result result = new Result(Status.SKIPPED, ZERO, null);
        bus.send(new TestStepFinished(now(), testCase, testStep, result));

        assertEquals(1, runNotifier.testAssumptionFailedFailures.size());
        assertEquals(pickleRunner.describeChild(step), runNotifier.testFinishedDescription);
        Failure stepFailure = runNotifier.testAssumptionFailedFailures.get(0);
        assertThat(stepFailure.getDescription(), is(equalTo(pickleRunner.describeChild(step))));
        assertThat(stepFailure.getException(), instanceOf(SkippedThrowable.class));
        assertThat(stepFailure.getException().getMessage(), is(equalTo("This step is skipped")));

        bus.send(new TestCaseFinished(now(), testCase, result));

        assertEquals(2, runNotifier.testAssumptionFailedFailures.size());
        Failure pickleFailure = runNotifier.testAssumptionFailedFailures.get(1);
        assertThat(pickleFailure.getDescription(), is(equalTo(pickleRunner.getDescription())));
        assertThat(pickleFailure.getException(), instanceOf(SkippedThrowable.class));
        assertThat(pickleFailure.getException().getMessage(), is(equalTo("This scenario is skipped")));
    }

    @Test
    void test_step_finished_fires_assumption_failed_and_test_finished_for_skipped_step_with_assumption_violated() {
        MockRunNotifier runNotifier = new MockRunNotifier();
        jUnitReporter.startExecutionUnit(pickleRunner, runNotifier);

        bus.send(new TestCaseStarted(now(), testCase));
        bus.send(new TestStepStarted(now(), testCase, testStep));
        Throwable exception = new AssumptionViolatedException("Oops");
        Result result = new Result(Status.SKIPPED, ZERO, exception);
        bus.send(new TestStepFinished(now(), testCase, testStep, result));

        assertEquals(1, runNotifier.testAssumptionFailedFailures.size());
        assertEquals(pickleRunner.describeChild(step), runNotifier.testFinishedDescription);

        Failure stepFailure = runNotifier.testAssumptionFailedFailures.get(0);
        assertThat(stepFailure.getDescription(), is(equalTo(pickleRunner.describeChild(step))));
        assertThat(stepFailure.getException(), is(equalTo(exception)));

        bus.send(new TestCaseFinished(now(), testCase, result));

        assertEquals(2, runNotifier.testAssumptionFailedFailures.size());
        assertEquals(pickleRunner.describeChild(step), runNotifier.testFinishedDescription);

        Failure pickleFailure = runNotifier.testAssumptionFailedFailures.get(1);
        assertThat(pickleFailure.getDescription(), is(equalTo(pickleRunner.getDescription())));
        assertThat(pickleFailure.getException(), is(equalTo(exception)));
    }

    @Test
    void test_step_finished_fires_test_failure_and_test_finished_for_skipped_step_with_pending_exception() {
        MockRunNotifier runNotifier = new MockRunNotifier();
        jUnitReporter.startExecutionUnit(pickleRunner, runNotifier);

        bus.send(new TestCaseStarted(now(), testCase));
        bus.send(new TestStepStarted(now(), testCase, testStep));
        Throwable exception = new TestPendingException("Oops");
        Result result = new Result(Status.PENDING, ZERO, exception);
        bus.send(new TestStepFinished(now(), testCase, testStep, result));

        assertEquals(1, runNotifier.testFailures.size());
        assertEquals(pickleRunner.describeChild(step), runNotifier.testFinishedDescription);

        Failure stepFailure = runNotifier.testFailures.get(0);
        assertThat(stepFailure.getDescription(), is(equalTo(pickleRunner.describeChild(step))));
        assertThat(stepFailure.getException(), is(equalTo(exception)));

        bus.send(new TestCaseFinished(now(), testCase, result));

        assertEquals(2, runNotifier.testFailures.size());
        assertEquals(pickleRunner.describeChild(step), runNotifier.testFinishedDescription);

        Failure pickleFailure = runNotifier.testFailures.get(1);
        assertThat(pickleFailure.getDescription(), is(equalTo(pickleRunner.getDescription())));
        assertThat(pickleFailure.getException(), is(equalTo(exception)));

    }

    @Test
    void test_step_undefined_fires_test_failure_and_test_finished_for_undefined_step() {
        MockRunNotifier runNotifier = new MockRunNotifier();
        jUnitReporter.startExecutionUnit(pickleRunner, runNotifier);

        Suggestion suggestion = new Suggestion("step name", singletonList("some snippet"));
        bus.send(new SnippetsSuggestedEvent(now(), featureUri, scenarioLine, scenarioLine, suggestion));
        bus.send(new TestCaseStarted(now(), testCase));
        bus.send(new TestStepStarted(now(), testCase, testStep));
        Throwable exception = new CucumberException("No step definitions found");
        Result result = new Result(Status.UNDEFINED, ZERO, exception);
        bus.send(new TestStepFinished(now(), testCase, testStep, result));

        assertEquals(1, runNotifier.testFailures.size());
        assertEquals(pickleRunner.describeChild(step), runNotifier.testFinishedDescription);

        Failure stepFailure = runNotifier.testFailures.get(0);
        assertThat(stepFailure.getDescription(), is(equalTo(pickleRunner.describeChild(step))));
        assertThat(stepFailure.getException(), is(equalTo(exception)));

        bus.send(new TestCaseFinished(now(), testCase, result));

        assertEquals(2, runNotifier.testFailures.size());
        assertEquals(pickleRunner.describeChild(step), runNotifier.testFinishedDescription);

        Failure pickleFailure = runNotifier.testFailures.get(1);
        assertThat(pickleFailure.getDescription(), is(equalTo(pickleRunner.getDescription())));
        assertThat(pickleFailure.getException().getMessage(), is("" +
                "The step 'step name' is undefined.\n" +
                "You can implement this step using the snippet(s) below:\n" +
                "\n" +
                "some snippet\n"));
    }

    @Test
    void test_step_finished_fires_test_failure_and_test_finished_for_failed_step() {
        MockRunNotifier runNotifier = new MockRunNotifier();
        jUnitReporter.startExecutionUnit(pickleRunner, runNotifier);

        bus.send(new TestCaseStarted(now(), testCase));
        bus.send(new TestStepStarted(now(), testCase, testStep));
        Throwable exception = new Exception("Oops");
        Result result = new Result(Status.FAILED, ZERO, exception);
        bus.send(new TestStepFinished(now(), testCase, testStep, result));

        assertEquals(1, runNotifier.testFailures.size());
        assertEquals(pickleRunner.describeChild(step), runNotifier.testFinishedDescription);

        Failure stepFailure = runNotifier.testFailures.get(0);
        assertThat(stepFailure.getDescription(), is(equalTo(pickleRunner.describeChild(step))));
        assertThat(stepFailure.getException(), is(equalTo(exception)));

        bus.send(new TestCaseFinished(now(), testCase, result));

        assertEquals(2, runNotifier.testFailures.size());
        assertEquals(pickleRunner.describeChild(step), runNotifier.testFinishedDescription);

        Failure pickleFailure = runNotifier.testFailures.get(1);
        assertThat(pickleFailure.getDescription(), is(equalTo(pickleRunner.getDescription())));
        assertThat(pickleFailure.getException(), is(equalTo(exception)));
    }

    @Test
    void test_step_finished_fires_test_failure_and_test_finished_for_failed_hook() {
        MockRunNotifier runNotifier = new MockRunNotifier();
        jUnitReporter.startExecutionUnit(pickleRunner, runNotifier);

        bus.send(new TestCaseStarted(now(), testCase));
        bus.send(new TestStepStarted(now(), testCase, testStep));
        Result stepResult = new Result(Status.PASSED, ZERO, null);
        bus.send(new TestStepFinished(now(), testCase, testStep, stepResult));

        bus.send(new TestStepStarted(now(), testCase, new StubHookTestStep()));
        Throwable exception = new Exception("Oops");
        Result result = new Result(Status.FAILED, ZERO, exception);
        bus.send(new TestStepFinished(now(), testCase, new StubHookTestStep(), result));

        // Hooks are not included in step failure
        assertEquals(0, runNotifier.testFailures.size());
        assertEquals(pickleRunner.describeChild(step), runNotifier.testFinishedDescription);

        bus.send(new TestCaseFinished(now(), testCase, result));

        assertEquals(1, runNotifier.testFailures.size());
        assertEquals(pickleRunner.describeChild(step), runNotifier.testFinishedDescription);

        Failure pickleFailure = runNotifier.testFailures.get(0);
        assertThat(pickleFailure.getDescription(), is(equalTo(pickleRunner.getDescription())));
        assertThat(pickleFailure.getException(), is(equalTo(exception)));
    }

    @Test
    void test_step_finished_fires_test_failure_and_test_finished_for_failed_step_with_multiple_failure_exception() {
        MockRunNotifier runNotifier = new MockRunNotifier();
        jUnitReporter.startExecutionUnit(pickleRunner, runNotifier);

        bus.send(new TestCaseStarted(now(), testCase));
        bus.send(new TestStepStarted(now(), testCase, testStep));
        List<Throwable> failures = asList(
            new Exception("Oops"),
            new Exception("I did it again"));
        Throwable exception = new MultipleFailureException(failures);
        Result result = new Result(Status.FAILED, ZERO, exception);
        bus.send(new TestStepFinished(now(), testCase, testStep, result));

        assertEquals(2, runNotifier.testFailures.size());
        assertEquals(pickleRunner.describeChild(step), runNotifier.testFinishedDescription);

        List<Failure> stepFailure = runNotifier.testFailures;

        assertThat(stepFailure.get(0).getDescription(), is(equalTo(pickleRunner.describeChild(step))));
        assertThat(stepFailure.get(0).getException(), is(equalTo(failures.get(0))));

        assertThat(stepFailure.get(1).getDescription(), is(equalTo(pickleRunner.describeChild(step))));
        assertThat(stepFailure.get(1).getException(), is(equalTo(failures.get(1))));

        bus.send(new TestCaseFinished(now(), testCase, result));

        assertEquals(4, runNotifier.testFailures.size());
        assertEquals(pickleRunner.describeChild(step), runNotifier.testFinishedDescription);

        List<Failure> pickleFailure = runNotifier.testFailures;

        assertThat(pickleFailure.get(2).getDescription(), is(equalTo(pickleRunner.getDescription())));
        assertThat(pickleFailure.get(2).getException(), is(equalTo(failures.get(0))));

        assertThat(pickleFailure.get(3).getDescription(), is(equalTo(pickleRunner.getDescription())));
        assertThat(pickleFailure.get(3).getException(), is(equalTo(failures.get(1))));
    }

    private static class MockPickleRunner implements PickleRunner {
        private final Map<io.cucumber.plugin.event.Step, Description> childDescriptions = new HashMap<>();
        private final Description description;

        public MockPickleRunner(io.cucumber.plugin.event.Step step) {
            childDescriptions.put(step, Description.createTestDescription("", step.getText()));
            description = Description.createTestDescription("", "pickle name");
        }

        @Override
        public void run(RunNotifier notifier) {
        }

        @Override
        public Description getDescription() {
            return description;
        }

        @Override
        public Description describeChild(io.cucumber.plugin.event.Step step) {
            return childDescriptions.get(step);
        }
    }

    private static class StubPickleStepTestStep implements PickleStepTestStep {
        private final URI uri;
        private final Step step;

        public StubPickleStepTestStep(URI featureUri, Step step) {
            uri = featureUri;
            this.step = step;
        }

        @Override
        public String getPattern() {
            return null;
        }

        @Override
        public io.cucumber.plugin.event.Step getStep() {
            return step;
        }

        @Override
        public List<Argument> getDefinitionArgument() {
            return null;
        }

        @Override
        public StepArgument getStepArgument() {
            return null;
        }

        @Override
        public int getStepLine() {
            return 0;
        }

        @Override
        public URI getUri() {
            return uri;
        }

        @Override
        public String getStepText() {
            return null;
        }

        @Override
        public String getCodeLocation() {
            return null;
        }

        @Override
        public UUID getId() {
            return null;
        }
    }

    private static class StubTestCase implements TestCase {
        @Override
        public Integer getLine() {
            return null;
        }

        @Override
        public Location getLocation() {
            return null;
        }

        @Override
        public String getKeyword() {
            return null;
        }

        @Override
        public String getName() {
            return null;
        }

        @Override
        public String getScenarioDesignation() {
            return null;
        }

        @Override
        public List<String> getTags() {
            return null;
        }

        @Override
        public List<TestStep> getTestSteps() {
            return null;
        }

        @Override
        public URI getUri() {
            return null;
        }

        @Override
        public UUID getId() {
            return null;
        }
    }

    private static class MockRunNotifier extends RunNotifier {
        List<Failure> testAssumptionFailedFailures = new ArrayList<>();
        List<Failure> testFailures = new ArrayList<>();

        Description testStartedDescription;
        Description testFinishedDescription;

        @Override
        public void fireTestAssumptionFailed(Failure failure) {
            this.testAssumptionFailedFailures.add(failure);
        }

        @Override
        public void fireTestFailure(Failure failure) {
            this.testFailures.add(failure);
        }

        @Override
        public void fireTestStarted(Description description) throws StoppedByUserException {
            this.testStartedDescription = description;
        }

        @Override
        public void fireTestFinished(Description description) {
            this.testFinishedDescription = description;
        }
    }

    private static class StubHookTestStep implements TestStep {
        @Override
        public String getCodeLocation() {
            return null;
        }

        @Override
        public UUID getId() {
            return null;
        }
    }
}
