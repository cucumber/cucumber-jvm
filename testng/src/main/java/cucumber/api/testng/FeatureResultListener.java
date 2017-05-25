package cucumber.api.testng;

import cucumber.runtime.CucumberException;
import cucumber.api.Result;
import cucumber.api.event.EventHandler;
import cucumber.api.event.EventPublisher;
import cucumber.api.event.TestStepFinished;
import cucumber.api.formatter.Formatter;

public class FeatureResultListener implements Formatter {
    static final String UNDEFINED_MESSAGE = "There are undefined steps";
    static final String PENDING_MESSAGE = "There are pending steps";
    private boolean strict;
    private Throwable error = null;
    private final EventHandler<TestStepFinished> testStepFinishedHandler = new EventHandler<TestStepFinished>() {
        @Override
        public void receive(TestStepFinished event) {
            collectError(event.result);
        }
    };

    public FeatureResultListener(boolean strict) {
        this.strict = strict;
    }

    @Override
    public void setEventPublisher(EventPublisher publisher) {
        publisher.registerHandlerFor(TestStepFinished.class, testStepFinishedHandler);
    }

    void collectError(Result result) {
        if (result.is(Result.Type.FAILED)) {
            if (error == null || isUndefinedError(error) || isPendingError(error)) {
                error = result.getError();
            }
        } else if (result.is(Result.Type.PENDING) && strict) {
            if (error == null || isUndefinedError(error)) {
                error = new CucumberException(PENDING_MESSAGE);
            }
        } else if (result.is(Result.Type.UNDEFINED) && strict) {
            if (error == null) {
                error = new CucumberException(UNDEFINED_MESSAGE);
            }
        }
    }

    private boolean isPendingError(Throwable error) {
        return (error instanceof CucumberException) && error.getMessage().equals(PENDING_MESSAGE);
    }

    private boolean isUndefinedError(Throwable error) {
        return (error instanceof CucumberException) && error.getMessage().equals(UNDEFINED_MESSAGE);
    }

    public boolean isPassed() {
        return error == null;
    }

    public Throwable getFirstError() {
        return error;
    }

    public void startFeature() {
        error = null;
    }
}
