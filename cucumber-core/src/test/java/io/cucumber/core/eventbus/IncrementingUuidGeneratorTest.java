package io.cucumber.core.eventbus;

import io.cucumber.core.exception.CucumberException;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicLong;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.*;

class IncrementingUuidGeneratorTest {
    @Test
    void generates_different_non_null_uuids() {
        // Given
        UuidGenerator generator = new IncrementingUuidGenerator();
        UUID uuid1 = generator.get();

        // When
        UUID uuid2 = generator.get();

        // Then
        assertNotNull(uuid1);
        assertNotNull(uuid2);
        assertNotEquals(uuid1, uuid2);
    }

    @Test
    void raises_exception_when_out_of_range() throws NoSuchFieldException, IllegalAccessException {
        // Given
        UuidGenerator generator = new IncrementingUuidGenerator();
        Field counterField = IncrementingUuidGenerator.class.getDeclaredField("counter");
        counterField.setAccessible(true);
        AtomicLong counter = (AtomicLong) counterField.get(generator);
        counter.set(Long.MAX_VALUE - 1);

        // When
        CucumberException cucumberException = assertThrows(CucumberException.class, generator::get);

        // Then
        assertThat(cucumberException.getMessage(),
            Matchers.containsString("Out of IncrementingUuidGenerator capacity"));
    }

    @Test
    void same_thread_generates_different_UuidGenerators() {
        // Given
        UuidGenerator generator1 = new IncrementingUuidGenerator();
        UuidGenerator generator2 = new IncrementingUuidGenerator();

        // When
        UUID uuid1 = generator1.get();
        UUID uuid2 = generator2.get();

        // Then
        assertNotNull(uuid1);
        assertNotNull(uuid2);
        assertNotEquals(uuid1, uuid2);
    }
}
