package io.cucumber.core.runtime;

import java.util.function.Function;
import java.util.function.Supplier;

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

    public TestCaseFailed(Throwable throwable) {
        super(throwable);
    }

}
