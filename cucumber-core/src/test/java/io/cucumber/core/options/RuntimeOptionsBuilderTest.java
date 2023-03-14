package io.cucumber.core.options;

import io.cucumber.core.eventbus.IncrementingUuidGenerator;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class RuntimeOptionsBuilderTest {

    @Test
    void build() {
        // Given
        RuntimeOptionsBuilder builder = new RuntimeOptionsBuilder()
                .setUuidGeneratorClass(IncrementingUuidGenerator.class);

        // When
        RuntimeOptions runtimeOptions = builder.build();

        // Then
        assertEquals(IncrementingUuidGenerator.class, runtimeOptions.getUuidGeneratorClass());
    }
}
