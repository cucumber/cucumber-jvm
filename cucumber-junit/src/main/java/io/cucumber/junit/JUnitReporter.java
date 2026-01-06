package io.cucumber.junit;

import io.cucumber.core.eventbus.EventBus;
import io.cucumber.junit.PickleRunners.PickleRunner;
import io.cucumber.plugin.event.EventHandler;
import io.cucumber.plugin.event.PickleStepTestStep;
import io.cucumber.plugin.event.Result;
import io.cucumber.plugin.event.SnippetsSuggestedEvent;
import io.cucumber.plugin.event.SnippetsSuggestedEvent.Suggestion;
import io.cucumber.plugin.event.TestCaseFinished;
import io.cucumber.plugin.event.TestCaseStarted;
import io.cucumber.plugin.event.TestStep;
import io.cucumber.plugin.event.TestStepFinished;
import io.cucumber.plugin.event.TestStepStarted;
import org.jspecify.annotations.Nullable;
import org.junit.runner.Description;
import org.junit.runner.notification.Failure;
import org.junit.runner.notification.RunNotifier;
import org.junit.runners.model.MultipleFailureException;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import static io.cucumber.junit.SkippedThrowable.NotificationLevel.SCENARIO;
import static io.cucumber.junit.SkippedThrowable.NotificationLevel.STEP;
import static java.util.Objects.requireNonNull;

final class JUnitReporter {

    private final JUnitOptions junitOptions;
    private final EventBus bus;
    private final Collection<Suggestion> suggestions = new ArrayList<>();
    private final EventHandler<SnippetsSuggestedEvent> snippetsSuggestedEventEventHandler = this::handleSnippetSuggested;
    private @Nullable List<Throwable> stepErrors;
    private final EventHandler<TestCaseStarted> testCaseStartedHandler = this::handleTestCaseStarted;
    private @Nullable TestNotifier stepNotifier;
    private final EventHandler<TestStepFinished> testStepFinishedHandler = this::handleTestStepFinished;
    private @Nullable PickleRunner pickleRunner;
    private @Nullable RunNotifier runNotifier;
    private final EventHandler<TestStepStarted> testStepStartedHandler = this::handTestStepStarted;
    private @Nullable TestNotifier pickleRunnerNotifier;
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
        suggestions.add(snippetsSuggestedEvent.getSuggestion());
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
        if (testStep instanceof PickleStepTestStep pickleStep) {
            if (junitOptions.stepNotifications()) {
                requireNonNull(pickleRunner);
                requireNonNull(runNotifier);
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
            handleStepResult(event.getResult());
        } else {
            handleHookResult(event.getResult());
        }
    }

    private void handleStepResult(Result result) {
        requireNonNull(stepErrors);
        requireNonNull(stepNotifier);

        Throwable error = result.getError();
        switch (result.getStatus()) {
            case PASSED -> {
                // do nothing
            }
            case SKIPPED -> {
                if (error == null) {
                    error = new SkippedThrowable(STEP);
                } else {
                    stepErrors.add(error);
                }
                stepNotifier.addFailedAssumption(error);
            }
            case PENDING, AMBIGUOUS, FAILED -> {
                requireNonNull(error);
                stepErrors.add(error);
                stepNotifier.addFailure(error);
            }
            case UNDEFINED -> {
                stepErrors.add(new UndefinedStepException(suggestions));
                stepNotifier.addFailure(error == null ? new UndefinedStepException(suggestions) : error);
            }
            default -> throw new IllegalStateException("Unexpected result status: " + result.getStatus());
        }
        stepNotifier.fireTestFinished();
    }

    private void handleHookResult(Result result) {
        requireNonNull(stepErrors);
        Throwable error = result.getError();
        if (error != null) {
            stepErrors.add(error);
        }
    }

    private void handleTestCaseResult(TestCaseFinished event) {
        requireNonNull(stepErrors);
        requireNonNull(pickleRunnerNotifier);

        Result result = event.getResult();
        switch (result.getStatus()) {
            case PASSED -> {
                // do nothing
            }
            case SKIPPED -> {
                if (stepErrors.isEmpty()) {
                    stepErrors.add(new SkippedThrowable(SCENARIO));
                }
                stepErrors.stream()
                        .findFirst()
                        .ifPresent(pickleRunnerNotifier::addFailedAssumption);
            }
            case PENDING, UNDEFINED -> stepErrors.stream()
                    .findFirst()
                    .ifPresent(pickleRunnerNotifier::addFailure);
            case AMBIGUOUS, FAILED -> stepErrors.forEach(pickleRunnerNotifier::addFailure);
            default -> throw new IllegalStateException("Unexpected value: " + result.getStatus());
        }
    }

    private interface TestNotifier {

        void fireTestStarted();

        void addFailure(Throwable error);

        void addFailedAssumption(Throwable error);

        void fireTestFinished();

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

        @Override
        public void fireTestStarted() {
            notifier.fireTestStarted(description);
        }

        @Override
        public void addFailure(Throwable targetException) {
            if (targetException instanceof MultipleFailureException multipleFailureException) {
                addMultipleFailureException(multipleFailureException);
            } else {
                notifier.fireTestFailure(new Failure(description, targetException));
            }
        }

        @Override
        public void addFailedAssumption(Throwable e) {
            notifier.fireTestAssumptionFailed(new Failure(description, e));
        }

        @Override
        public void fireTestFinished() {
            notifier.fireTestFinished(description);
        }

    }

}
