package io.cucumber.core.runner;

import org.junit.jupiter.api.Test;
import org.opentest4j.TestAbortedException;

import static org.junit.jupiter.api.Assertions.*;

class ThrowableCollectorTest {

    final ThrowableCollector collector = new ThrowableCollector();

    @Test
    void collects_nothing() {
        collector.execute(() -> {

        });
        assertNull(collector.getThrowable());
    }

    @Test
    void collects_single_exception() {
        RuntimeException exception = new RuntimeException();
        collector.execute(() -> {
            throw exception;
        });
        assertEquals(exception, collector.getThrowable());
    }

    @Test
    void second_exception_is_suppressed() {
        RuntimeException firstException = new RuntimeException();
        collector.execute(() -> {
            throw firstException;
        });
        RuntimeException secondException = new RuntimeException();
        collector.execute(() -> {
            throw secondException;
        });
        assertEquals(firstException, collector.getThrowable());
        assertEquals(secondException, collector.getThrowable().getSuppressed()[0]);
    }

    @Test
    void first_aborted_exception_is_suppressed() {
        RuntimeException firstException = new TestAbortedException();
        collector.execute(() -> {
            throw firstException;
        });
        RuntimeException secondException = new RuntimeException();
        collector.execute(() -> {
            throw secondException;
        });
        assertEquals(secondException, collector.getThrowable());
        assertEquals(firstException, collector.getThrowable().getSuppressed()[0]);
    }

    @Test
    void second_aborted_exception_is_suppressed() {
        RuntimeException firstException = new RuntimeException();
        collector.execute(() -> {
            throw firstException;
        });
        RuntimeException secondException = new TestAbortedException();
        collector.execute(() -> {
            throw secondException;
        });
        assertEquals(firstException, collector.getThrowable());
        assertEquals(secondException, collector.getThrowable().getSuppressed()[0]);
    }

    @Test
    void rethrows_unrecoverable() {
        assertThrows(OutOfMemoryError.class, () -> collector.execute(() -> {
            throw new OutOfMemoryError();
        }));
    }

}
