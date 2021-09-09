package io.cucumber.testng;

import io.cucumber.core.exception.CucumberException;

/**
 * The only purpose of this class is to move parse errors from the DataProvider
 * to the test execution of the TestNG tests.
 *
 * @see TestNGCucumberRunner#provideScenarios()
 */
final class CucumberExceptionWrapper implements PickleWrapper {

    private final CucumberException exception;

    CucumberExceptionWrapper(CucumberException e) {
        this.exception = e;
    }

    @Override
    public Pickle getPickle() {
        throw this.exception;
    }

}
