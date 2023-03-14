package io.cucumber.core.options;

import io.cucumber.core.eventbus.UuidGenerator;

import java.util.UUID;

/**
 * This UUID generator does nothing. It is solely needed for marking purposes.
 */
final class NoUuidGenerator implements UuidGenerator {

    private NoUuidGenerator() {
        // No need for instantiation
    }

    @Override
    public UUID get() {
        return null;
    }
}
