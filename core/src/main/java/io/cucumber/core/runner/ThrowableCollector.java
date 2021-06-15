package io.cucumber.core.runner;

import static io.cucumber.core.exception.UnrecoverableExceptions.rethrowIfUnrecoverable;
import static io.cucumber.core.runner.TestAbortedExceptions.isTestAbortedException;

/**
 * Collects thrown exceptions.
 *
 * When multiple exceptions are thrown, the worst exception is shown first.
 * Other exceptions are suppressed.
 */
final class ThrowableCollector {

    private Throwable throwable;

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

    Throwable getThrowable() {
        return throwable;
    }

}
