package io.cucumber.testng;

import io.cucumber.core.eventbus.UuidGenerator;

import java.util.UUID;

/**
 * This UUID generator does nothing. It is solely needed for marking purposes.
 */
final class NoUuidGenerator implements UuidGenerator {

    private NoUuidGenerator() {
        /* no-op */
    }

    @Override
    public UUID generateId() {
        throw new UnsupportedOperationException();
    }
}
