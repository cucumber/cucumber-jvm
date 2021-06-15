package io.cucumber.core.runner;

import java.util.Arrays;

import static java.util.Objects.requireNonNull;

final class TestAbortedExceptions {
    private static final String[] TEST_ABORTED_EXCEPTIONS = {
            "org.junit.AssumptionViolatedException",
            "org.junit.internal.AssumptionViolatedException",
            "org.opentest4j.TestAbortedException",
            "org.testng.SkipException",
    };

    static {
        Arrays.sort(TestAbortedExceptions.TEST_ABORTED_EXCEPTIONS);
    }

    private TestAbortedExceptions(){

    }

    static boolean isTestAbortedException(Throwable throwable) {
        requireNonNull(throwable, "throwable may not be null");
        return Arrays.binarySearch(TEST_ABORTED_EXCEPTIONS, throwable.getClass().getName()) >= 0;
    }
}
