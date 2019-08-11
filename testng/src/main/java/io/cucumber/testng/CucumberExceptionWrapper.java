package io.cucumber.testng;

import gherkin.events.PickleEvent;
import io.cucumber.core.exception.CucumberException;

/**
 * The only purpose of this class is to move parse errors from the DataProvider
 * to the test execution of the TestNG tests.
 *
 * @see TestNGCucumberRunner#provideScenarios()
 */
final class CucumberExceptionWrapper implements PickleEventWrapper {
    private final CucumberException exception;

    CucumberExceptionWrapper(CucumberException e) {
        this.exception = e;
    }

    @Override
    public PickleEvent getPickleEvent() {
        throw this.exception;
    }
}
