package io.cucumber.core.runner;

import org.junit.jupiter.api.Test;
import org.opentest4j.TestAbortedException;

import java.util.function.Predicate;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class TestAbortedExceptionsTest {
    static class TestAbortedExceptionSubClass extends TestAbortedException {
    }

    @Test
    void testPredicate() {
        Predicate<Throwable> isTestAbortedExceptionPredicate = TestAbortedExceptions
                .createIsTestAbortedExceptionPredicate();
        assertFalse(isTestAbortedExceptionPredicate.test(new RuntimeException()));
        assertTrue(isTestAbortedExceptionPredicate.test(new TestAbortedException()));
        assertTrue(isTestAbortedExceptionPredicate.test(new TestAbortedExceptionSubClass()));
    }

}
