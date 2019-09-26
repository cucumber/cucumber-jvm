package io.cucumber.core.backend;

import java.lang.reflect.InvocationTargetException;

public class CucumberInvocationTargetException extends RuntimeException   {

    private final Located located;
    private final InvocationTargetException invocationTargetException;

    public CucumberInvocationTargetException(Located located, InvocationTargetException invocationTargetException) {
        this.located = located;
        this.invocationTargetException = invocationTargetException;
    }

    public Throwable getInvocationTargetExceptionCause() {
        return invocationTargetException.getCause();
    }

    public Located getLocated() {
        return located;
    }
}
