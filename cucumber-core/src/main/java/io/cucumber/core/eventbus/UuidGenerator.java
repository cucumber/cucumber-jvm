package io.cucumber.core.eventbus;

import org.apiguardian.api.API;

import java.util.UUID;
import java.util.function.Supplier;

/**
 * SPI (Service Provider Interface) to generate UUIDs.
 */
@API(status = API.Status.STABLE)
public interface UuidGenerator extends Supplier<UUID> {
}
