package io.cucumber.testng;

import cucumber.runtime.CucumberException;
import gherkin.events.PickleEvent;

/**
 * The only purpose of this class is to move parse errors from the DataProvider
 * to the test execution of the TestNG tests.
 *
 * @see TestNGCucumberRunner#provideScenarios()
 */
class CucumberExceptionWrapper implements PickleEventWrapper {
    private CucumberException exception;

    CucumberExceptionWrapper(CucumberException e) {
        this.exception = e;
    }

    @Override
    public PickleEvent getPickleEvent() {
        throw this.exception;
    }

}
