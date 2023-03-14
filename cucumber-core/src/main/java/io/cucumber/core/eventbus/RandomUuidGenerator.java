package io.cucumber.core.eventbus;

import java.util.UUID;

/**
 * UUID generator based on random numbers. The generator is thread-safe and
 * supports multi-jvm usage of Cucumber.
 */
public class RandomUuidGenerator implements UuidGenerator {
    @Override
    public UUID get() {
        return UUID.randomUUID();
    }
}
