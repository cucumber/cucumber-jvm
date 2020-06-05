package io.cucumber.junit;

import io.cucumber.core.eventbus.EventBus;
import io.cucumber.junit.PickleRunners.PickleRunner;
import io.cucumber.plugin.event.EventHandler;
import io.cucumber.plugin.event.PickleStepTestStep;
import io.cucumber.plugin.event.Result;
import io.cucumber.plugin.event.SnippetsSuggestedEvent;
import io.cucumber.plugin.event.TestCaseFinished;
import io.cucumber.plugin.event.TestCaseStarted;
import io.cucumber.plugin.event.TestStep;
import io.cucumber.plugin.event.TestStepFinished;
import io.cucumber.plugin.event.TestStepStarted;
import org.junit.runner.Description;
import org.junit.runner.notification.Failure;
import org.junit.runner.notification.RunNotifier;
import org.junit.runners.model.MultipleFailureException;

import java.net.URI;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import static io.cucumber.junit.SkippedThrowable.NotificationLevel.SCENARIO;
import static io.cucumber.junit.SkippedThrowable.NotificationLevel.STEP;

final class JUnitReporter {

    private final JUnitOptions junitOptions;
    private final EventBus bus;
    private final Map<StepLocation, Collection<String>> snippetsPerStep = new TreeMap<>();
    private final EventHandler<SnippetsSuggestedEvent> snippetsSuggestedEventEventHandler = this::handleSnippetSuggested;
    private List<Throwable> stepErrors;
    private final EventHandler<TestCaseStarted> testCaseStartedHandler = this::handleTestCaseStarted;
    private TestNotifier stepNotifier;
    private final EventHandler<TestStepFinished> testStepFinishedHandler = this::handleTestStepFinished;
    private PickleRunner pickleRunner;
    private RunNotifier runNotifier;
    private final EventHandler<TestStepStarted> testStepStartedHandler = this::handTestStepStarted;
    private TestNotifier pickleRunnerNotifier;
    private final EventHandler<TestCaseFinished> testCaseFinishedHandler = this::handleTestCaseResult;

    JUnitReporter(EventBus bus, JUnitOptions junitOption) {
        this.junitOptions = junitOption;
        this.bus = bus;
        bus.registerHandlerFor(TestCaseStarted.class, testCaseStartedHandler);
        bus.registerHandlerFor(TestStepStarted.class, testStepStartedHandler);
        bus.registerHandlerFor(TestStepFinished.class, testStepFinishedHandler);
        bus.registerHandlerFor(TestCaseFinished.class, testCaseFinishedHandler);
        bus.registerHandlerFor(SnippetsSuggestedEvent.class, snippetsSuggestedEventEventHandler);
    }

    private void handleSnippetSuggested(SnippetsSuggestedEvent snippetsSuggestedEvent) {
        snippetsPerStep.putIfAbsent(new StepLocation(
            snippetsSuggestedEvent.getUri(),
            snippetsSuggestedEvent.getStepLine()),
            snippetsSuggestedEvent.getSnippets());
    }

    void finishExecutionUnit() {
        bus.removeHandlerFor(TestCaseStarted.class, testCaseStartedHandler);
        bus.removeHandlerFor(TestStepStarted.class, testStepStartedHandler);
        bus.removeHandlerFor(TestStepFinished.class, testStepFinishedHandler);
        bus.removeHandlerFor(TestCaseFinished.class, testCaseFinishedHandler);
        bus.removeHandlerFor(SnippetsSuggestedEvent.class, snippetsSuggestedEventEventHandler);
    }

    void startExecutionUnit(PickleRunner pickleRunner, RunNotifier runNotifier) {
        this.pickleRunner = pickleRunner;
        this.runNotifier = runNotifier;
        this.stepNotifier = null;

        pickleRunnerNotifier = new EachTestNotifier(runNotifier, pickleRunner.getDescription());
    }

    private void handleTestCaseStarted(TestCaseStarted testCaseStarted) {
        stepErrors = new ArrayList<>();
    }

    private void handTestStepStarted(TestStepStarted event) {
        TestStep testStep = event.getTestStep();
        if (testStep instanceof PickleStepTestStep) {
            PickleStepTestStep pickleStep = (PickleStepTestStep) testStep;
            if (junitOptions.stepNotifications()) {
                Description description = pickleRunner.describeChild(pickleStep.getStep());
                stepNotifier = new EachTestNotifier(runNotifier, description);
            } else {
                stepNotifier = new NoTestNotifier();
            }
            stepNotifier.fireTestStarted();
        }
    }

    private void handleTestStepFinished(TestStepFinished event) {
        if (event.getTestStep() instanceof PickleStepTestStep) {
            PickleStepTestStep testStep = (PickleStepTestStep) event.getTestStep();
            handleStepResult(testStep, event.getResult());
        } else {
            handleHookResult(event.getResult());
        }
    }

    private void handleStepResult(PickleStepTestStep testStep, Result result) {
        Throwable error = result.getError();
        switch (result.getStatus()) {
            case PASSED:
                // do nothing
                break;
            case SKIPPED:
                if (error == null) {
                    error = new SkippedThrowable(STEP);
                } else {
                    stepErrors.add(error);
                }
                stepNotifier.addFailedAssumption(error);
                break;
            case PENDING:
            case AMBIGUOUS:
            case FAILED:
                stepErrors.add(error);
                stepNotifier.addFailure(error);
                break;
            case UNDEFINED:
                Collection<String> snippets = snippetsPerStep.remove(
                    new StepLocation(testStep.getUri(), testStep.getStepLine()));
                stepErrors.add(new UndefinedStepException(
                    testStep.getStepText(),
                    snippets,
                    snippetsPerStep.values()));
                stepNotifier.addFailure(error == null ? new UndefinedStepException(snippets) : error);
                break;
            default:
                throw new IllegalStateException("Unexpected result status: " + result.getStatus());
        }
        stepNotifier.fireTestFinished();
    }

    private void handleHookResult(Result result) {
        if (result.getError() != null) {
            stepErrors.add(result.getError());
        }
    }

    private void handleTestCaseResult(TestCaseFinished event) {
        Result result = event.getResult();
        switch (result.getStatus()) {
            case PASSED:
                // do nothing
                break;
            case SKIPPED:
                if (stepErrors.isEmpty()) {
                    stepErrors.add(new SkippedThrowable(SCENARIO));
                }
                stepErrors.stream()
                        .findFirst()
                        .ifPresent(pickleRunnerNotifier::addFailedAssumption);
                break;
            case PENDING:
            case UNDEFINED:
                stepErrors.stream()
                        .findFirst()
                        .ifPresent(pickleRunnerNotifier::addFailure);
                break;
            case AMBIGUOUS:
            case FAILED:
                stepErrors.forEach(pickleRunnerNotifier::addFailure);
                break;
        }
    }

    private interface TestNotifier {

        void fireTestStarted();

        void addFailure(Throwable error);

        void addFailedAssumption(Throwable error);

        void fireTestFinished();

    }

    private static final class StepLocation implements Comparable<StepLocation> {

        private final URI uri;
        private final int line;

        private StepLocation(URI uri, int line) {
            this.uri = uri;
            this.line = line;
        }

        @Override
        public int compareTo(StepLocation o) {
            int order = uri.compareTo(o.uri);
            return order != 0 ? order : Integer.compare(line, o.line);
        }

    }

    static final class NoTestNotifier implements TestNotifier {

        @Override
        public void fireTestStarted() {
            // Does nothing
        }

        @Override
        public void addFailure(Throwable error) {
            // Does nothing
        }

        @Override
        public void addFailedAssumption(Throwable error) {
            // Does nothing
        }

        @Override
        public void fireTestFinished() {
            // Does nothing
        }

    }

    static class EachTestNotifier implements TestNotifier {

        private final RunNotifier notifier;

        private final Description description;

        EachTestNotifier(RunNotifier notifier, Description description) {
            this.notifier = notifier;
            this.description = description;
        }

        private void addMultipleFailureException(MultipleFailureException mfe) {
            for (Throwable each : mfe.getFailures()) {
                addFailure(each);
            }
        }

        public void fireTestStarted() {
            notifier.fireTestStarted(description);
        }

        public void addFailure(Throwable targetException) {
            if (targetException instanceof MultipleFailureException) {
                addMultipleFailureException((MultipleFailureException) targetException);
            } else {
                notifier.fireTestFailure(new Failure(description, targetException));
            }
        }

        public void addFailedAssumption(Throwable e) {
            notifier.fireTestAssumptionFailed(new Failure(description, e));
        }

        public void fireTestFinished() {
            notifier.fireTestFinished(description);
        }

    }

}
