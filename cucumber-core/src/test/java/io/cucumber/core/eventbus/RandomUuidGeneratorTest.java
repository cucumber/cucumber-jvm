package io.cucumber.core.eventbus;

import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class RandomUuidGeneratorTest {
    @Test
    void generates_different_non_null_uuids() {
        // Given
        UuidGenerator generator = new RandomUuidGenerator();
        UUID uuid1 = generator.generateId();

        // When
        UUID uuid2 = generator.generateId();

        // Then
        assertNotNull(uuid1);
        assertNotNull(uuid2);
        assertNotEquals(uuid1, uuid2);
    }
}
