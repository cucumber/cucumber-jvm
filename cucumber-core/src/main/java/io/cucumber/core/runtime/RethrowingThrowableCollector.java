package io.cucumber.core.runtime;

import io.cucumber.core.exception.CompositeCucumberException;
import io.cucumber.core.exception.UnrecoverableExceptions;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Supplier;

import static io.cucumber.core.exception.ExceptionUtils.throwAsUncheckedException;
import static io.cucumber.core.exception.UnrecoverableExceptions.rethrowIfUnrecoverable;

/**
 * Collects and rethrows thrown exceptions.
 */
final class RethrowingThrowableCollector {

    private final List<Throwable> thrown = Collections.synchronizedList(new ArrayList<>());

    void executeAndThrow(Runnable runnable) {
        try {
            runnable.run();
        } catch (TestCaseFailed e) {
            throwAsUncheckedException(e.getCause());
        } catch (Throwable t) {
            UnrecoverableExceptions.rethrowIfUnrecoverable(t);
            add(t);
            throwAsUncheckedException(t);
        }
    }

    <T> T executeAndThrow(Supplier<T> supplier) {
        try {
            return supplier.get();
        } catch (Throwable t) {
            rethrowIfUnrecoverable(t);
            thrown.add(t);
            throwAsUncheckedException(t);
            return null;
        }
    }

    void add(Throwable throwable) {
        thrown.add(throwable);
    }

    Throwable getThrowable() {
        // Don't try any tricks with `.addSuppressed`. Other frameworks are
        // already doing this.
        if (thrown.isEmpty()) {
            return null;
        }
        if (thrown.size() == 1) {
            return thrown.get(0);
        }
        return new CompositeCucumberException(thrown);
    }

}
