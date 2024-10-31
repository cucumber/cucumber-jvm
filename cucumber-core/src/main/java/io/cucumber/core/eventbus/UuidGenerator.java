package io.cucumber.core.eventbus;

import org.apiguardian.api.API;

import java.util.UUID;
import java.util.function.Supplier;

/**
 * SPI (Service Provider Interface) to provide a supplier that generates UUIDs.
 */
@API(status = API.Status.EXPERIMENTAL)
public interface UuidGenerator {

    Supplier<UUID> supplier();
}
