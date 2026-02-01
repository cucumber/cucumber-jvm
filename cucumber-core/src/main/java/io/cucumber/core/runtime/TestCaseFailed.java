package io.cucumber.core.runtime;

import java.util.function.Function;
import java.util.function.Supplier;

import static java.util.Objects.requireNonNull;

/**
 * Failures as asserted by
 * {@link TestCaseResultObserver#assertTestCasePassed(Supplier, Function, Function, Function)}
 * should not be collected by the rethrowing
 * {@link RethrowingThrowableCollector}.
 * <p>
 * This wrapper facilitates cooperation between the two. Any exceptions caught
 * this way should be unpacked and rethrown.
 */
class TestCaseFailed extends RuntimeException {

    TestCaseFailed(Throwable cause) {
        super(requireNonNull(cause));
    }

    @Override
    public synchronized Throwable getCause() {
        return requireNonNull(super.getCause());
    }
}
