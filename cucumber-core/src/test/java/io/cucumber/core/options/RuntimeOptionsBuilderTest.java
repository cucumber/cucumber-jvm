package io.cucumber.core.options;

import io.cucumber.core.eventbus.IncrementingUuidGenerator;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RuntimeOptionsBuilderTest {

    @Test
    void merges_fail_if_no_tests() {
        RuntimeOptions options = new RuntimeOptionsBuilder().setFailIfNoTests(true).build();
        assertTrue(options.isFailIfNoTests());

        options = new RuntimeOptionsBuilder().build(options);
        assertTrue(options.isFailIfNoTests());

        options = new RuntimeOptionsBuilder().setFailIfNoTests(false).build(options);
        assertFalse(options.isFailIfNoTests());
    }

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
