package io.cucumber.core.exception;

import org.junit.jupiter.api.Test;

import static io.cucumber.core.exception.UnrecoverableExceptions.rethrowIfUnrecoverable;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

class UnrecoverableExceptionsTest {

    @Test
    void rethrowsOutOfMemoryError() {
        assertThrows(OutOfMemoryError.class, () -> rethrowIfUnrecoverable(new OutOfMemoryError()));
    }

    @Test
    void ignoresThrowable() {
        assertDoesNotThrow(() -> rethrowIfUnrecoverable(new Throwable()));
    }

}
