package io.cucumber.testng;

import io.cucumber.core.event.EventHandler;
import io.cucumber.core.event.Result;
import io.cucumber.core.event.Status;
import io.cucumber.core.event.TestCaseFinished;
import io.cucumber.core.eventbus.EventBus;
import io.cucumber.core.exception.CucumberException;
import org.testng.SkipException;

class TestCaseResultListener {
    private static final String UNDEFINED_MESSAGE = "There are undefined steps";
    private static final String SKIPPED_MESSAGE = "This scenario is skipped";
    private final EventBus bus;
    private boolean strict;
    private Result result;
    private final EventHandler<TestCaseFinished> testCaseFinishedHandler = event -> receiveResult(event.getResult());

    TestCaseResultListener(EventBus bus, boolean strict) {
        this.strict = strict;
        this.bus = bus;
        bus.registerHandlerFor(TestCaseFinished.class, testCaseFinishedHandler);
    }

    void finishExecutionUnit() {
        bus.removeHandlerFor(TestCaseFinished.class, testCaseFinishedHandler);
    }


    void receiveResult(Result result) {
        this.result = result;
    }

    boolean isPassed() {
        return result == null || result.getStatus().is(Status.PASSED);
    }

    Throwable getError() {
        if (result == null) {
            return null;
        }
        switch (result.getStatus()) {
        case FAILED:
        case AMBIGUOUS:
            return result.getError();
        case PENDING:
            if (strict) {
                return result.getError();
            } else {
                return new SkipException(result.getError().getMessage(), result.getError());
            }
        case UNDEFINED:
            if (strict) {
                return new CucumberException(UNDEFINED_MESSAGE);
            } else {
                return new SkipException(UNDEFINED_MESSAGE);
            }
        case SKIPPED:
            Throwable error = result.getError();
            if (error != null) {
                if (error instanceof SkipException) {
                    return error;
                } else {
                    return new SkipException(result.getError().getMessage(), error);
                }
            } else {
                return new SkipException(SKIPPED_MESSAGE);
            }
        case PASSED:
            return null;
        default:
            throw new IllegalStateException("Unexpected result status: " + result.getStatus());
        }
    }

}
