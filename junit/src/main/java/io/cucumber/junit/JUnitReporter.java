package io.cucumber.junit;

import io.cucumber.core.event.PickleStepTestStep;
import io.cucumber.core.event.EventHandler;
import io.cucumber.core.event.Result;
import io.cucumber.core.event.TestCaseFinished;
import io.cucumber.core.event.TestCaseStarted;
import io.cucumber.core.event.TestStepFinished;
import io.cucumber.core.event.TestStepStarted;
import io.cucumber.junit.PickleRunners.PickleRunner;
import io.cucumber.core.eventbus.EventBus;
import org.junit.runner.Description;
import org.junit.runner.notification.Failure;
import org.junit.runner.notification.RunNotifier;
import org.junit.runners.model.MultipleFailureException;

import java.util.ArrayList;

import static io.cucumber.junit.SkippedThrowable.NotificationLevel.SCENARIO;
import static io.cucumber.junit.SkippedThrowable.NotificationLevel.STEP;

final class JUnitReporter {

    private final JUnitOptions junitOptions;
    private final EventBus bus;

    TestNotifier stepNotifier; // package-private for testing
    private PickleRunner pickleRunner;
    private RunNotifier runNotifier;
    private TestNotifier pickleRunnerNotifier;
    ArrayList<Throwable> stepErrors; // package-private for testing
    private final EventHandler<TestCaseStarted> testCaseStartedHandler = this::handleTestCaseStarted;
    private final EventHandler<TestStepStarted> testStepStartedHandler = this::handTestStepStarted;
    private final EventHandler<TestStepFinished> testStepFinishedHandler = this::handleTestStepFinished;
    private final EventHandler<TestCaseFinished> testCaseFinishedHandler = this::handleTestCaseResult;


    JUnitReporter(EventBus bus, JUnitOptions junitOption) {
        this.junitOptions = junitOption;
        this.bus = bus;
        bus.registerHandlerFor(TestCaseStarted.class, testCaseStartedHandler);
        bus.registerHandlerFor(TestStepStarted.class, testStepStartedHandler);
        bus.registerHandlerFor(TestStepFinished.class, testStepFinishedHandler);
        bus.registerHandlerFor(TestCaseFinished.class, testCaseFinishedHandler);
    }

    void finishExecutionUnit() {
        bus.removeHandlerFor(TestCaseStarted.class, testCaseStartedHandler);
        bus.removeHandlerFor(TestStepStarted.class, testStepStartedHandler);
        bus.removeHandlerFor(TestStepFinished.class, testStepFinishedHandler);
        bus.removeHandlerFor(TestCaseFinished.class, testCaseFinishedHandler);
    }

    void startExecutionUnit(PickleRunner pickleRunner, RunNotifier runNotifier) {
        this.pickleRunner = pickleRunner;
        this.runNotifier = runNotifier;
        this.stepNotifier = null;

        pickleRunnerNotifier = new EachTestNotifier(runNotifier, pickleRunner.getDescription());
    }

    void handleTestCaseStarted(TestCaseStarted testCaseStarted) {
        pickleRunnerNotifier.fireTestStarted();
        stepErrors = new ArrayList<Throwable>();
    }


    private void handTestStepStarted(TestStepStarted event) {
        if (event.getTestStep() instanceof PickleStepTestStep) {
            PickleStepTestStep testStep = (PickleStepTestStep) event.getTestStep();
            handleStepStarted(testStep.getPickleStep());
        }
    }

    void handleStepStarted(gherkin.pickles.PickleStep step) {
        if (junitOptions.stepNotifications()) {
            Description description = pickleRunner.describeChild(step);
            stepNotifier = new EachTestNotifier(runNotifier, description);
        } else {
            stepNotifier = new NoTestNotifier();
        }
        stepNotifier.fireTestStarted();
    }

    private void handleTestStepFinished(TestStepFinished event) {
        if (event.getTestStep() instanceof PickleStepTestStep) {
            PickleStepTestStep testStep = (PickleStepTestStep) event.getTestStep();
            handleStepResult(testStep, event.getResult());
        } else {
            handleHookResult(event.getResult());
        }
    }

    void handleStepResult(PickleStepTestStep testStep, Result result) {
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
                stepErrors.add(error);
                addFailureOrFailedAssumptionDependingOnStrictMode(stepNotifier, error);
                break;
            case UNDEFINED:
                if (error == null) {
                    error = new UndefinedThrowable();
                }
                stepErrors.add(new UndefinedThrowable(testStep.getStepText()));
                addFailureOrFailedAssumptionDependingOnStrictMode(stepNotifier, error);
                break;
            case AMBIGUOUS:
            case FAILED:
                stepErrors.add(error);
                stepNotifier.addFailure(error);
                break;
            default:
                throw new IllegalStateException("Unexpected result status: " + result.getStatus());
        }
        stepNotifier.fireTestFinished();
    }

    void handleHookResult(Result result) {
        if (result.getError() != null) {
            stepErrors.add(result.getError());
        }
    }

    void handleTestCaseResult(TestCaseFinished event) {
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
                    .ifPresent(error -> pickleRunnerNotifier.addFailedAssumption(error));
                break;
            case PENDING:
            case UNDEFINED:
                stepErrors.stream()
                    .findFirst()
                    .ifPresent(error -> addFailureOrFailedAssumptionDependingOnStrictMode(pickleRunnerNotifier, error));
                break;
            case AMBIGUOUS:
            case FAILED:
                for (Throwable error : stepErrors) {
                    pickleRunnerNotifier.addFailure(error);
                }
                break;
        }
        pickleRunnerNotifier.fireTestFinished();
    }

    private void addFailureOrFailedAssumptionDependingOnStrictMode(TestNotifier notifier, Throwable error) {
        if (junitOptions.isStrict()) {
            notifier.addFailure(error);
        } else {
            notifier.addFailedAssumption(error);
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

        public void addFailure(Throwable targetException) {
            if (targetException instanceof MultipleFailureException) {
                addMultipleFailureException((MultipleFailureException) targetException);
            } else {
                notifier.fireTestFailure(new Failure(description, targetException));
            }
        }

        private void addMultipleFailureException(MultipleFailureException mfe) {
            for (Throwable each : mfe.getFailures()) {
                addFailure(each);
            }
        }

        public void addFailedAssumption(Throwable e) {
            notifier.fireTestAssumptionFailed(new Failure(description, e));
        }

        public void fireTestFinished() {
            notifier.fireTestFinished(description);
        }

        public void fireTestStarted() {
            notifier.fireTestStarted(description);
        }
    }
}
