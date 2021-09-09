package io.cucumber.core.runner;

import io.cucumber.core.backend.CucumberInvocationTargetException;
import io.cucumber.core.backend.Located;

final class StackManipulation {

    private StackManipulation() {

    }

    static Throwable removeFrameworkFrames(CucumberInvocationTargetException invocationException) {
        Throwable error = invocationException.getInvocationTargetExceptionCause();
        StackTraceElement[] stackTraceElements = error.getStackTrace();
        Located located = invocationException.getLocated();

        int newStackTraceLength = findIndexOf(located, stackTraceElements);
        if (newStackTraceLength == -1) {
            return error;
        }

        StackTraceElement[] newStackTrace = new StackTraceElement[newStackTraceLength];
        System.arraycopy(stackTraceElements, 0, newStackTrace, 0, newStackTraceLength);
        error.setStackTrace(newStackTrace);
        return error;
    }

    private static int findIndexOf(Located located, StackTraceElement[] stackTraceElements) {
        if (stackTraceElements.length == 0) {
            return -1;
        }

        int newStackTraceLength;
        for (newStackTraceLength = 1; newStackTraceLength < stackTraceElements.length; ++newStackTraceLength) {
            if (located.isDefinedAt(stackTraceElements[newStackTraceLength - 1])) {
                break;
            }
        }
        return newStackTraceLength;
    }

    static Throwable removeFrameworkFramesAndAppendStepLocation(
            CucumberInvocationTargetException invocationException, StackTraceElement stepLocation
    ) {
        Located located = invocationException.getLocated();
        Throwable error = invocationException.getInvocationTargetExceptionCause();
        if (stepLocation == null) {
            return error;
        }
        StackTraceElement[] stackTraceElements = error.getStackTrace();
        int newStackTraceLength = findIndexOf(located, stackTraceElements);
        if (newStackTraceLength == -1) {
            return error;
        }
        StackTraceElement[] newStackTrace = new StackTraceElement[newStackTraceLength + 1];
        System.arraycopy(stackTraceElements, 0, newStackTrace, 0, newStackTraceLength);
        newStackTrace[newStackTraceLength] = stepLocation;
        error.setStackTrace(newStackTrace);
        return error;
    }

}
