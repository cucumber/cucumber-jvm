package io.cucumber.core.runner;

import io.cucumber.core.backend.CucumberInvocationTargetException;
import io.cucumber.core.backend.Located;
import org.jspecify.annotations.Nullable;

import java.util.function.Consumer;

final class StackManipulation {

    private StackManipulation() {
        /* no-op */
    }

    static Throwable removeFrameworkFramesAndAppendStepLocation(
            CucumberInvocationTargetException invocationException, StackTraceElement stepLocation
    ) {
        Throwable error = invocationException.getCause();
        walkException(error, appendStepLocation(invocationException.getLocated(), stepLocation));
        return error;
    }

    static Throwable removeFrameworkFrames(CucumberInvocationTargetException invocationException) {
        Throwable error = invocationException.getCause();
        walkException(invocationException, removeFramesAfter(invocationException.getLocated()));
        return error;
    }

    private static void walkException(@Nullable Throwable cause, Consumer<Throwable> action) {
        while (cause != null) {
            action.accept(cause);
            cause = cause.getCause();
        }
    }

    static Consumer<Throwable> removeFramesAfter(Located located) {
        return throwable -> {
            StackTraceElement[] stackTrace = throwable.getStackTrace();
            int lastFrame = findIndexOf(located, stackTrace);
            if (lastFrame == -1) {
                return;
            }
            StackTraceElement[] newStackTrace = new StackTraceElement[lastFrame + 1];
            System.arraycopy(stackTrace, 0, newStackTrace, 0, lastFrame + 1);
            throwable.setStackTrace(newStackTrace);
        };
    }

    private static Consumer<Throwable> appendStepLocation(Located located, StackTraceElement stepLocation) {
        return throwable -> {
            if (located == null) {
                return;
            }
            StackTraceElement[] stackTrace = throwable.getStackTrace();
            int lastFrame = findIndexOf(located, stackTrace);
            if (lastFrame == -1) {
                return;
            }
            // One extra for the step location
            StackTraceElement[] newStackTrace = new StackTraceElement[lastFrame + 1 + 1];
            System.arraycopy(stackTrace, 0, newStackTrace, 0, lastFrame + 1);
            newStackTrace[lastFrame + 1] = stepLocation;
            throwable.setStackTrace(newStackTrace);
        };
    }

    private static int findIndexOf(Located located, StackTraceElement[] stackTraceElements) {
        for (int index = 0; index < stackTraceElements.length; index++) {
            if (located.isDefinedAt(stackTraceElements[index])) {
                return index;
            }
        }
        return -1;
    }
}
