package io.cucumber.junit.platform.engine;

import io.cucumber.core.eventbus.EventBus;
import io.cucumber.plugin.event.EventPublisher;
import org.opentest4j.TestAbortedException;

import java.util.function.Function;

class TestCaseResultObserver implements AutoCloseable {

    private final io.cucumber.core.runtime.TestCaseResultObserver delegate;

    private TestCaseResultObserver(EventPublisher bus) {
        this.delegate = new io.cucumber.core.runtime.TestCaseResultObserver(bus);
    }

    static TestCaseResultObserver observe(EventBus bus) {
        return new TestCaseResultObserver(bus);
    }

    void assertTestCasePassed() {
        delegate.assertTestCasePassed(
            TestAbortedException::new,
            Function.identity(),
            UndefinedStepException::new,
            Function.identity());
    }

    @Override
    public void close() {
        delegate.close();
    }

}
