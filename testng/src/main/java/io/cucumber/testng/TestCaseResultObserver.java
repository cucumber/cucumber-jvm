package io.cucumber.testng;

import io.cucumber.core.eventbus.EventBus;
import io.cucumber.plugin.event.EventPublisher;
import org.testng.SkipException;

class TestCaseResultObserver implements AutoCloseable {

    private static final String SKIP_MESSAGE = "This scenario is skipped";
    private final io.cucumber.core.runtime.TestCaseResultObserver delegate;

    private TestCaseResultObserver(EventPublisher bus, boolean strict) {
        this.delegate = new io.cucumber.core.runtime.TestCaseResultObserver(bus, strict);
    }

    static TestCaseResultObserver observe(EventBus bus, boolean strict) {
        return new TestCaseResultObserver(bus, strict);
    }

    void assertTestCasePassed() {
        delegate.assertTestCasePassed(
            () -> new SkipException(SKIP_MESSAGE),
            (exception) -> exception instanceof SkipException
                ? exception
                : new SkipException(exception.getMessage(), exception),
            (suggestions, strict) -> new UndefinedStepException(suggestions, strict),
            (exception) -> new SkipException(exception.getMessage(), exception)
        );
    }

    @Override
    public void close() {
        delegate.close();
    }
}

