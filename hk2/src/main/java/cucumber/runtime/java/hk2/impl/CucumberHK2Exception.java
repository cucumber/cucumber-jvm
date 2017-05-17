package cucumber.runtime.java.hk2.impl;

import cucumber.runtime.CucumberException;

/**
 * Exception class for cucumber-hk2 exceptions
 */
class CucumberHK2Exception extends CucumberException {

    CucumberHK2Exception(String message) {
        super(message);
    }

    CucumberHK2Exception(String message, Throwable e) {
        super(message, e);
    }
}
