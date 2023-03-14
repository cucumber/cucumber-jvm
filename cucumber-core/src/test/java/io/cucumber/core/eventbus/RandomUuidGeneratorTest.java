package io.cucumber.core.eventbus;

import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class RandomUuidGeneratorTest {
    @Test
    void generates_different_non_null_uuids() {
        // Given
        UuidGenerator generator = new RandomUuidGenerator();
        UUID uuid1 = generator.get();

        // When
        UUID uuid2 = generator.get();

        // Then
        assertNotNull(uuid1);
        assertNotNull(uuid2);
        assertNotEquals(uuid1, uuid2);
    }
}
