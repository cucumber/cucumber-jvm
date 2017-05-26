package cucumber.runtime.junit;

import cucumber.api.PendingException;
import cucumber.api.Result;
import cucumber.api.event.EventHandler;
import cucumber.api.event.TestStepFinished;
import cucumber.api.event.TestStepStarted;
import cucumber.runner.EventBus;
import gherkin.pickles.PickleStep;
import org.junit.internal.runners.model.EachTestNotifier;
import org.junit.runner.Description;
import org.junit.runner.notification.RunNotifier;

import static cucumber.runtime.Runtime.isPending;

public class JUnitReporter {

    private final boolean strict;
    private final JUnitOptions junitOptions;

    EachTestNotifier stepNotifier; // package-private for testing
    private ExecutionUnitRunner executionUnitRunner;
    private RunNotifier runNotifier;
    EachTestNotifier executionUnitNotifier; // package-private for testing
    private boolean failedStep;
    private boolean ignoredStep;
    private final EventHandler<TestStepStarted> testStepStartedHandler = new EventHandler<TestStepStarted>() {

        @Override
        public void receive(TestStepStarted event) {
            if (!event.testStep.isHook()) {
                handleStepStarted(event.testStep.getPickleStep());
            }
        }

    };
    private EventHandler<TestStepFinished> testStepFinishedHandler = new EventHandler<TestStepFinished>() {

        @Override
        public void receive(TestStepFinished event) {
            if (event.testStep.isHook()) {
                handleHookResult(event.result);
            } else {
                handleStepResult(event.result);
            }
        }

    };

    public JUnitReporter(EventBus bus, boolean strict, JUnitOptions junitOption) {
        this.strict = strict;
        this.junitOptions = junitOption;
        bus.registerHandlerFor(TestStepStarted.class, testStepStartedHandler);
        bus.registerHandlerFor(TestStepFinished.class, testStepFinishedHandler);
    }

    void startExecutionUnit(ExecutionUnitRunner executionUnitRunner, RunNotifier runNotifier) {
        this.executionUnitRunner = executionUnitRunner;
        this.runNotifier = runNotifier;
        this.stepNotifier = null;
        this.failedStep = false;
        this.ignoredStep = false;

        executionUnitNotifier = new EachTestNotifier(runNotifier, executionUnitRunner.getDescription());
        executionUnitNotifier.fireTestStarted();
    }

    void finishExecutionUnit() {
        if (ignoredStep && !failedStep) {
            executionUnitNotifier.fireTestIgnored();
        }
        executionUnitNotifier.fireTestFinished();
    }

    void handleStepStarted(PickleStep step) {
        Description description = executionUnitRunner.describeChild(step);
        stepNotifier = new EachTestNotifier(runNotifier, description);
        if (junitOptions.allowStartedIgnored()) {
            stepNotifier.fireTestStarted();
        }
    }

    void handleStepResult(Result result) {
        Throwable error = result.getError();
        if (result.is(Result.Type.SKIPPED)) {
            stepNotifier.fireTestIgnored();
        } else if (isPendingOrUndefined(result)) {
            addFailureOrIgnoreStep(result);
        } else {
            if (stepNotifier != null) {
                //Should only fireTestStarted if not ignored
                if (!junitOptions.allowStartedIgnored()) {
                    stepNotifier.fireTestStarted();
                }
                if (error != null) {
                    stepNotifier.addFailure(error);
                }
                stepNotifier.fireTestFinished();
            }
            if (error != null) {
                failedStep = true;
                executionUnitNotifier.addFailure(error);
            }
        }
    }

    void handleHookResult(Result result) {
        if (result.is(Result.Type.FAILED) || (strict && isPending(result.getError()))) {
            executionUnitNotifier.addFailure(result.getError());
        } else if (isPending(result.getError())) {
            ignoredStep = true;
        }
    }

    boolean useFilenameCompatibleNames() {
        return junitOptions.filenameCompatibleNames();
    }

    private boolean isPendingOrUndefined(Result result) {
        Throwable error = result.getError();
        return result.is(Result.Type.UNDEFINED) || isPending(error);
    }

    private void addFailureOrIgnoreStep(Result result) {
        if (strict) {
            if (!junitOptions.allowStartedIgnored()) {
                stepNotifier.fireTestStarted();
            }
            addFailure(result);
            stepNotifier.fireTestFinished();
        } else {
            ignoredStep = true;
            stepNotifier.fireTestIgnored();
        }
    }

    private void addFailure(Result result) {
        Throwable error = result.getError();
        if (error == null) {
            error = new PendingException();
        }
        failedStep = true;
        stepNotifier.addFailure(error);
        executionUnitNotifier.addFailure(error);
    }
}
