package cucumber.runtime.junit.jupiter;

import cucumber.api.Result;
import cucumber.api.event.TestCaseFinished;
import cucumber.runner.EventBus;

import static java.util.Objects.requireNonNull;

public class JunitJupiterReporter {

    private Result result;

    public JunitJupiterReporter(EventBus eventBus) {
        eventBus.registerHandlerFor(TestCaseFinished.class, event -> JunitJupiterReporter.this.result = event.result);
    }

    public boolean isOk(boolean strict) {
        requireNonNull(result);
        return result.isOk(strict);
    }

    public Throwable getError() {
        requireNonNull(result);
        return result.getError();
    }
}
