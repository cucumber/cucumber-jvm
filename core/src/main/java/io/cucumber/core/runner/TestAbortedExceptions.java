package io.cucumber.core.runner;

import io.cucumber.core.logging.Logger;
import io.cucumber.core.logging.LoggerFactory;
import io.cucumber.core.resource.ClassLoaders;

import java.util.Arrays;
import java.util.function.Predicate;
import java.util.stream.Stream;

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
        return Arrays.stream(TEST_ABORTED_EXCEPTIONS)
                .flatMap(s -> {
                    try {
                        Class<?> aClass = defaultClassLoader.loadClass(s);
                        return Stream.of(aClass);
                    } catch (Throwable t) {
                        rethrowIfUnrecoverable(t);
                        log.debug(t,
                            () -> "Failed to load class" + s + ": will not be supported for aborted executions.");
                    }
                    return Stream.empty();
                })
                .map(throwable -> (Predicate<Throwable>) throwable::isInstance)
                .reduce(__ -> false, Predicate::or);
    }

    private TestAbortedExceptions() {

    }

}
