package cucumber.runtime.junit;

import cucumber.api.Result;
import cucumber.api.TestStep;
import cucumber.api.event.EventHandler;
import cucumber.api.event.TestCaseFinished;
import cucumber.api.event.TestCaseStarted;
import cucumber.api.event.TestStepFinished;
import cucumber.api.event.TestStepStarted;
import cucumber.runner.EventBus;
import cucumber.runtime.junit.PickleRunners.PickleRunner;
import gherkin.pickles.PickleStep;
import org.junit.runner.Description;
import org.junit.runner.notification.Failure;
import org.junit.runner.notification.RunNotifier;
import org.junit.runners.model.MultipleFailureException;

import java.util.ArrayList;

public class JUnitReporter {

    private final boolean strict;
    private final JUnitOptions junitOptions;

    TestNotifier stepNotifier; // package-private for testing
    private PickleRunner pickleRunner;
    private RunNotifier runNotifier;
    TestNotifier pickleRunnerNotifier; // package-private for testing
    ArrayList<Throwable> stepErrors; // package-private for testing
    private final EventHandler<TestCaseStarted> testCaseStartedHandler = new EventHandler<TestCaseStarted>() {

        @Override
        public void receive(TestCaseStarted event) {
            handleTestCaseStarted();
        }

    };
    private final EventHandler<TestStepStarted> testStepStartedHandler = new EventHandler<TestStepStarted>() {

        @Override
        public void receive(TestStepStarted event) {
            if (!event.testStep.isHook()) {
                handleStepStarted(event.testStep.getPickleStep());
            }
        }

    };
    private final EventHandler<TestStepFinished> testStepFinishedHandler = new EventHandler<TestStepFinished>() {

        @Override
        public void receive(TestStepFinished event) {
            if (!event.testStep.isHook()) {
                handleStepResult(event.testStep, event.result);
            } else {
                handleHookResult(event.result);
            }
        }

    };
    private final EventHandler<TestCaseFinished> testCaseFinishedHandler = new EventHandler<TestCaseFinished>() {

        @Override
        public void receive(TestCaseFinished event) {
            handleTestCaseResult(event.result);
        }

    };

    public JUnitReporter(EventBus bus, boolean strict, JUnitOptions junitOption) {
        this.strict = strict;
        this.junitOptions = junitOption;
        bus.registerHandlerFor(TestCaseStarted.class, testCaseStartedHandler);
        bus.registerHandlerFor(TestStepStarted.class, testStepStartedHandler);
        bus.registerHandlerFor(TestStepFinished.class, testStepFinishedHandler);
        bus.registerHandlerFor(TestCaseFinished.class, testCaseFinishedHandler);
    }

    void startExecutionUnit(PickleRunner pickleRunner, RunNotifier runNotifier) {
        this.pickleRunner = pickleRunner;
        this.runNotifier = runNotifier;
        this.stepNotifier = null;

        pickleRunnerNotifier = new EachTestNotifier(runNotifier, pickleRunner.getDescription());
    }

    void handleTestCaseStarted() {
        pickleRunnerNotifier.fireTestStarted();
        stepErrors = new ArrayList<Throwable>();
    }

    void handleStepStarted(PickleStep step) {
        if (stepNotifications()) {
            Description description = pickleRunner.describeChild(step);
            stepNotifier = new EachTestNotifier(runNotifier, description);
        } else {
            stepNotifier = new NoTestNotifier();
        }
        stepNotifier.fireTestStarted();
    }

    boolean stepNotifications() {
        return junitOptions.stepNotifications();
    }

    void handleStepResult(TestStep testStep, Result result) {
        Throwable error = result.getError();
        switch (result.getStatus()) {
        case PASSED:
            // do nothing
            break;
        case SKIPPED:
            if (error == null) {
                error = new SkippedThrowable(NotificationLevel.STEP);
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
        }
        stepNotifier.fireTestFinished();
    }

    void handleHookResult(Result result) {
        if (result.getError() != null) {
            stepErrors.add(result.getError());
        }
    }

    void handleTestCaseResult(Result result) {
        switch (result.getStatus()) {
        case PASSED:
            // do nothing
            break;
        case SKIPPED:
            if (stepErrors.isEmpty()) {
                stepErrors.add(new SkippedThrowable(NotificationLevel.SCENARIO));
            }
            for (Throwable error : stepErrors) {
                pickleRunnerNotifier.addFailedAssumption(error);
            }
            break;
        case PENDING:
        case UNDEFINED:
            for (Throwable error : stepErrors) {
                addFailureOrFailedAssumptionDependingOnStrictMode(pickleRunnerNotifier, error);
            }
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

    boolean useFilenameCompatibleNames() {
        return junitOptions.filenameCompatibleNames();
    }

    private void addFailureOrFailedAssumptionDependingOnStrictMode(TestNotifier notifier, Throwable error) {
        if (strict) {
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
