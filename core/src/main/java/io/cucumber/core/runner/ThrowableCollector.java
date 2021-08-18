package io.cucumber.core.runner;

import java.util.function.Predicate;

import static io.cucumber.core.exception.UnrecoverableExceptions.rethrowIfUnrecoverable;
import static io.cucumber.core.runner.TestAbortedExceptions.createIsTestAbortedExceptionPredicate;

/**
 * Collects thrown exceptions.
 * <p>
 * When multiple exceptions are thrown, the worst exception is shown first.
 * Other exceptions are suppressed.
 */
final class ThrowableCollector {

    private Throwable throwable;
    private final Predicate<Throwable> isTestAbortedException = createIsTestAbortedExceptionPredicate();

    void execute(Runnable runnable) {
        try {
            runnable.run();
        } catch (Throwable t) {
            rethrowIfUnrecoverable(t);
            add(t);
        }
    }

    private void add(Throwable throwable) {
        if (this.throwable == null) {
            this.throwable = throwable;
        } else if (isTestAbortedException(this.throwable) && !isTestAbortedException(throwable)) {
            throwable.addSuppressed(this.throwable);
            this.throwable = throwable;
        } else if (this.throwable != throwable) {
            this.throwable.addSuppressed(throwable);
        }
    }

    private boolean isTestAbortedException(Throwable throwable) {
        return isTestAbortedException.test(throwable);
    }

    Throwable getThrowable() {
        return throwable;
    }

}
