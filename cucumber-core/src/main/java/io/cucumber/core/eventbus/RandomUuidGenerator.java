package io.cucumber.core.eventbus;

import java.util.UUID;
import java.util.function.Supplier;

/**
 * UUID generator based on random numbers. The generator is thread-safe and
 * supports multi-jvm usage of Cucumber.
 */
public class RandomUuidGenerator implements UuidGenerator {

    @Override
    public Supplier<UUID> supplier() {
        return UUID::randomUUID;
    }
}
