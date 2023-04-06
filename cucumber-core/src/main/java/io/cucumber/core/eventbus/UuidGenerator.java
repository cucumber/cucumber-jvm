package io.cucumber.core.eventbus;

import org.apiguardian.api.API;

import java.util.UUID;
import java.util.function.Supplier;

/**
 * SPI (Service Provider Interface) to generate UUIDs.
 */
@API(status = API.Status.EXPERIMENTAL)
public interface UuidGenerator extends Supplier<UUID> {
    UUID generateId();

    default UUID get() {
        return generateId();
    }
}
