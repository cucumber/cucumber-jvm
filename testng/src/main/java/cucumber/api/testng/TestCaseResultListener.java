package cucumber.api.testng;

import cucumber.api.Result;
import cucumber.api.event.EventHandler;
import cucumber.api.event.EventListener;
import cucumber.api.event.EventPublisher;
import cucumber.api.event.TestCaseFinished;
import cucumber.runtime.CucumberException;

class TestCaseResultListener implements EventListener {
    static final String UNDEFINED_MESSAGE = "There are undefined steps";
    private boolean strict;
    private Throwable error = null;
    private final EventHandler<TestCaseFinished> testCaseFinishedHandler = new EventHandler<TestCaseFinished>() {
        @Override
        public void receive(TestCaseFinished event) {
            collectError(event.result);
        }
    };

    TestCaseResultListener(boolean strict) {
        this.strict = strict;
    }

    @Override
    public void setEventPublisher(EventPublisher publisher) {
        publisher.registerHandlerFor(TestCaseFinished.class, testCaseFinishedHandler);
    }

    void collectError(Result result) {
        switch (result.getStatus()) {
        case FAILED:
        case AMBIGUOUS:
            error = result.getError();
            break;
        case PENDING:
            if (strict) {
                error = result.getError();
            }
            break;
        case UNDEFINED:
            if (strict) {
                error = new CucumberException(UNDEFINED_MESSAGE);
            }
            break;
        case PASSED:
        case SKIPPED:
            // do nothing
            break;
        default:
            throw new IllegalStateException("Unexpected result status: " + result.getStatus());
        }
    }

    boolean isPassed() {
        return error == null;
    }

    Throwable getError() {
        return error;
    }

    void startPickle() {
        error = null;
    }
}
