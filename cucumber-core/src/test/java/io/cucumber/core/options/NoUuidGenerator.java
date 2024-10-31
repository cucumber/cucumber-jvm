package io.cucumber.core.options;

import io.cucumber.core.eventbus.UuidGenerator;

import java.util.UUID;
import java.util.function.Supplier;

/**
 * This UUID generator does nothing. It is solely needed for marking purposes.
 */
final class NoUuidGenerator implements UuidGenerator {

    @Override
    public Supplier<UUID> supplier() {
        return () -> null;
    }
}
