package io.cucumber.core.eventbus;

import io.cucumber.core.exception.CucumberException;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class IncrementingUuidGeneratorTest {

    /**
     * Example of generated values (same epochTime, same sessionId, same
     * classloaderId, different counter value):
     * "87273d64-5500-83e3-8000-000000000000"
     * "87273d64-5500-83e3-8000-000000000001"
     * "87273d64-5500-83e3-8000-000000000002"
     * "87273d64-5500-83e3-8000-000000000003"
     * "87273d64-5500-83e3-8000-000000000004"
     * "87273d64-5500-83e3-8000-000000000005"
     * "87273d64-5500-83e3-8000-000000000006"
     * "87273d64-5500-83e3-8000-000000000007"
     * "87273d64-5500-83e3-8000-000000000008"
     * "87273d64-5500-83e3-8000-000000000009"
     */
    @Test
    void generates_different_non_null_uuids() {
        // Given
        UuidGenerator generator = new IncrementingUuidGenerator();

        // When
        List<UUID> uuids = IntStream.rangeClosed(1, 10)
                .mapToObj(i -> generator.generateId())
                .collect(Collectors.toList());

        // Then
        checkUuidProperties(uuids);
    }

    /**
     * Example of generated values (same epochTime, different sessionId, * same
     * classloaderId, same counter value):
     * "87273c5d-8500-88b6-8000-000000000000"
     * "87273c5d-8501-88b6-8000-000000000000"
     * "87273c5d-8502-88b6-8000-000000000000"
     * "87273c5d-8503-88b6-8000-000000000000"
     * "87273c5d-8504-88b6-8000-000000000000"
     * "87273c5d-8505-88b6-8000-000000000000"
     * "87273c5d-8506-88b6-8000-000000000000"
     * "87273c5d-8507-88b6-8000-000000000000"
     * "87273c5d-8508-88b6-8000-000000000000"
     * "87273c5d-8509-88b6-8000-000000000000"
     */
    @Test
    void same_thread_generates_different_UuidGenerators() {
        // Given/When
        List<UUID> uuids = IntStream.rangeClosed(1, 10)
                .mapToObj(i -> new IncrementingUuidGenerator().generateId())
                .collect(Collectors.toList());

        // Then
        checkUuidProperties(uuids);
    }

    /**
     * Example of values generated using different classloaders (same epochTime,
     * same sessionId, different classloaderId, same counter value):
     * "87273a9d-9a00-8bf7-8000-000000000000"
     * "87273a9d-9c00-844e-8000-000000000000"
     * "87273a9d-9e00-89ad-8000-000000000000"
     * "87273a9d-a000-8fd9-8000-000000000000"
     * "87273a9d-a100-8a48-8000-000000000000"
     * "87273a9d-a400-8322-8000-000000000000"
     * "87273a9d-a600-872c-8000-000000000000"
     * "87273a9d-a700-88c9-8000-000000000000"
     * "87273a9d-a900-8eb4-8000-000000000000"
     * "87273a9d-ab00-898c-8000-000000000000"
     */
    @Test
    void different_classloaders_generators() {
        // Given/When
        List<UUID> uuids = IntStream.rangeClosed(1, 10)
                .mapToObj(i -> getUuidGeneratorFromOtherClassloader().generateId())
                .collect(Collectors.toList());

        // Then
        checkUuidProperties(uuids);
    }

    @Test
    void raises_exception_when_out_of_range() {
        // Given
        IncrementingUuidGenerator generator = new IncrementingUuidGenerator();
        generator.counter.set(IncrementingUuidGenerator.MAX_COUNTER_VALUE - 1);

        // When
        CucumberException cucumberException = assertThrows(CucumberException.class, generator::generateId);

        // Then
        assertThat(cucumberException.getMessage(),
            Matchers.containsString("Out of IncrementingUuidGenerator capacity"));
    }

    @Test
    void version_overflow() {
        // Given
        IncrementingUuidGenerator.sessionCounter.set(IncrementingUuidGenerator.MAX_SESSION_ID - 1);

        // When
        CucumberException cucumberException = assertThrows(CucumberException.class, IncrementingUuidGenerator::new);

        // Then
        assertThat(cucumberException.getMessage(),
            Matchers.containsString("Out of IncrementingUuidGenerator capacity"));
    }

    private static void checkUuidProperties(List<UUID> uuids) {
        // all UUIDs are non-null
        assertTrue(uuids.stream().filter(Objects::isNull).findFirst().isEmpty());

        // UUID version is always 8
        List<Integer> versions = uuids.stream().map(UUID::version).distinct().collect(Collectors.toList());
        assertEquals(1, versions.size());
        assertEquals(8, versions.get(0));

        // UUID variants is always 2
        List<Integer> variants = uuids.stream().map(UUID::variant).distinct().collect(Collectors.toList());
        assertEquals(1, variants.size());
        assertEquals(2, variants.get(0));

        // all UUIDs are distinct
        assertEquals(uuids.size(), uuids.stream().distinct().count());

        // all UUIDs are ordered
        assertEquals(uuids.stream()
                .map(IncrementingUuidGeneratorTest::removeClassloaderId)
                .collect(Collectors.toList()),
            uuids.stream()
                    .map(IncrementingUuidGeneratorTest::removeClassloaderId)
                    .sorted()
                    .collect(Collectors.toList()));
    }

    /**
     * Create a copy of the UUID without the random part to allow comparison.
     */
    private static UUID removeClassloaderId(UUID uuid) {
        return new UUID(uuid.getMostSignificantBits() & 0xfffffffffffff000L, uuid.getLeastSignificantBits());
    }

    private static UuidGenerator getUuidGeneratorFromOtherClassloader() {
        try {
            return (UuidGenerator) (new NonCachingClassLoader()
                    .findClass(IncrementingUuidGenerator.class.getName())
                    .getConstructor()
                    .newInstance());
        } catch (Exception e) {
            throw new RuntimeException("could not instantiate " + IncrementingUuidGenerator.class.getSimpleName(), e);
        }
    }

    /**
     * A classloader which does not cache the class definition. Thus, when the
     * Class loaded using #findClass will have different static fields.
     */
    private static class NonCachingClassLoader extends ClassLoader {

        public NonCachingClassLoader() {
        }

        @Override
        protected Class<?> findClass(String name) {
            byte[] classBytes = loadClassBytesFromDisk(name);
            return defineClass(name, classBytes, 0, classBytes.length);
        }

        private byte[] loadClassBytesFromDisk(String className) {
            try {
                return Files.readAllBytes(Path.of(Objects.requireNonNull(NonCachingClassLoader.class
                        .getResource(className.replaceFirst(".+\\.", "") + ".class")).toURI()));
            } catch (IOException e) {
                throw new RuntimeException("Unable to read file from disk");
            } catch (URISyntaxException e) {
                throw new RuntimeException(e);
            }
        }
    }
}
