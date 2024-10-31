package io.cucumber.core.eventbus;

import org.junit.jupiter.api.Test;

import java.util.UUID;
import java.util.function.Supplier;

import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class RandomUuidGeneratorTest {
    @Test
    void generates_different_non_null_uuids() {
        // Given
        Supplier<UUID> generator = new RandomUuidGenerator().supplier();
        UUID uuid1 = generator.get();

        // When
        UUID uuid2 = generator.get();

        // Then
        assertNotNull(uuid1);
        assertNotNull(uuid2);
        assertNotEquals(uuid1, uuid2);
    }
}
