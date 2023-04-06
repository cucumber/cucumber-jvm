package io.cucumber.core.options;

import io.cucumber.core.eventbus.IncrementingUuidGenerator;
import io.cucumber.core.eventbus.RandomUuidGenerator;
import io.cucumber.core.eventbus.UuidGenerator;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.*;

class UuidGeneratorParserTest {

    @Test
    void parseUuidGenerator_IncrementingUuidGenerator() {
        // When
        Class<? extends UuidGenerator> uuidGeneratorClass = UuidGeneratorParser
                .parseUuidGenerator(IncrementingUuidGenerator.class.getName());

        // Then
        assertEquals(IncrementingUuidGenerator.class, uuidGeneratorClass);
    }

    @Test
    void parseUuidGenerator_RandomUuidGenerator() {
        // When
        Class<? extends UuidGenerator> uuidGeneratorClass = UuidGeneratorParser
                .parseUuidGenerator(RandomUuidGenerator.class.getName());

        // Then
        assertEquals(RandomUuidGenerator.class, uuidGeneratorClass);
    }

    @Test
    void parseUuidGenerator_not_a_generator() {
        // When
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
            () -> UuidGeneratorParser.parseUuidGenerator(String.class.getName()));

        // Then
        assertThat(exception.getMessage(), Matchers.containsString("not a subclass"));
    }

    @Test
    void parseUuidGenerator_not_a_class() {
        // When
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
            () -> UuidGeneratorParser.parseUuidGenerator("java.lang.NonExistingClassName"));

        // Then
        assertThat(exception.getMessage(), Matchers.containsString("Could not load UUID generator class"));
    }
}
