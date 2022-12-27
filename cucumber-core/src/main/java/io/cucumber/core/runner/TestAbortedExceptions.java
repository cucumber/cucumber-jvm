package io.cucumber.core.runner;

import io.cucumber.core.logging.Logger;
import io.cucumber.core.logging.LoggerFactory;
import io.cucumber.core.resource.ClassLoaders;

import java.util.Arrays;
import java.util.function.Predicate;

import static io.cucumber.core.exception.UnrecoverableExceptions.rethrowIfUnrecoverable;

/**
 * Identifies which exceptions signal that a test has been aborted.
 */
final class TestAbortedExceptions {

    private static final Logger log = LoggerFactory.getLogger(TestAbortedExceptions.class);

    private static final String[] TEST_ABORTED_EXCEPTIONS = {
            "org.junit.AssumptionViolatedException",
            "org.junit.internal.AssumptionViolatedException",
            "org.opentest4j.TestAbortedException",
            "org.testng.SkipException",
    };

    static Predicate<Throwable> createIsTestAbortedExceptionPredicate() {
        ClassLoader defaultClassLoader = ClassLoaders.getDefaultClassLoader();
        return throwable -> Arrays.stream(TEST_ABORTED_EXCEPTIONS)
                .anyMatch(s -> {
                    try {
                        Class<?> aClass = defaultClassLoader.loadClass(s);
                        return aClass.isInstance(throwable);
                    } catch (Throwable t) {
                        rethrowIfUnrecoverable(t);
                        log.debug(t,
                            () -> String.format(
                                "Failed to load class %s: will not be supported for aborted executions.", s));
                    }
                    return false;
                });
    }

    private TestAbortedExceptions() {

    }

}
