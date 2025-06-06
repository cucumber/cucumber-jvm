package io.cucumber.core.eventbus;

import io.cucumber.core.exception.CucumberException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.ThrowingSupplier;

import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class IncrementingUuidGeneratorTest {

    public static final String CLASSLOADER_ID_FIELD_NAME = "classloaderId";

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
                .mapToObj(i -> getUuidGeneratorFromOtherClassloader(i).generateId())
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
            containsString("Out of IncrementingUuidGenerator capacity"));
    }

    @Test
    void version_overflow() {
        // Given
        IncrementingUuidGenerator generator = new IncrementingUuidGenerator();
        IncrementingUuidGenerator.sessionCounter.set(IncrementingUuidGenerator.MAX_SESSION_ID - 1);

        // When
        CucumberException cucumberException = assertThrows(CucumberException.class, generator::generateId);

        // Then
        assertThat(cucumberException.getMessage(),
            containsString("Out of IncrementingUuidGenerator capacity"));
    }

    @Test
    void lazy_init() {
        // Given
        IncrementingUuidGenerator.sessionCounter.set(IncrementingUuidGenerator.MAX_SESSION_ID - 1);

        // When
        ThrowingSupplier<IncrementingUuidGenerator> instantiateGenerator = IncrementingUuidGenerator::new;

        // Then
        assertDoesNotThrow(instantiateGenerator);
    }

    private static void checkUuidProperties(List<UUID> uuids) {
        // all UUIDs are non-null
        assertFalse(uuids.stream().anyMatch(Objects::isNull));

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

    /**
     * Create a copy of the UUID without the epoch-time part to allow
     * comparison.
     */
    private static UUID removeEpochTime(UUID uuid) {
        return new UUID(uuid.getMostSignificantBits() & 0x0ffffffL, uuid.getLeastSignificantBits());
    }

    /**
     * Check that classloaderId collision rate is lower than a given threshold
     * when using multiple classloaders. This should not be mistaken with the
     * UUID collision rate. Note: this test takes about 20 seconds.
     */
    @Test
    void classloaderid_collision_rate_lower_than_two_percents_with_ten_classloaders()
            throws NoSuchFieldException, IllegalAccessException {
        double collisionRateWhenUsingTenClassloaders;
        List<Double> collisionRatesWhenUsingTenClassloaders = new ArrayList<>();
        do {
            // When I compute the classloaderId collision rate with multiple
            // classloaders
            Set<Long> classloaderIds = new HashSet<>();
            List<Integer> stats = new ArrayList<>();
            while (stats.size() < 100) {
                if (!classloaderIds
                        .add(getStaticFieldValue(getUuidGeneratorFromOtherClassloader(null),
                            CLASSLOADER_ID_FIELD_NAME))) {
                    stats.add(classloaderIds.size() + 1);
                    classloaderIds.clear();
                }
            }

            // Then the classloaderId collision rate for 10 classloaders is less
            // than 2%
            collisionRateWhenUsingTenClassloaders = stats.stream()
                    .filter(x -> x < 10).count() * 100 / (double) stats.size();
            collisionRatesWhenUsingTenClassloaders.add(collisionRateWhenUsingTenClassloaders);
        } while (collisionRateWhenUsingTenClassloaders > 2 && collisionRatesWhenUsingTenClassloaders.size() < 10);
        assertTrue(collisionRateWhenUsingTenClassloaders <= 2,
            "all retries exceed the expected collision rate : " + collisionRatesWhenUsingTenClassloaders);
    }

    @Test
    void same_classloaderId_leads_to_same_uuid_when_ignoring_epoch_time() {
        // Given the two generator have the same classloaderId
        UuidGenerator generator1 = getUuidGeneratorFromOtherClassloader(255);
        UuidGenerator generator2 = getUuidGeneratorFromOtherClassloader(255);

        // When the UUID are generated
        UUID uuid1 = generator1.generateId();
        UUID uuid2 = generator2.generateId();

        // Then the UUID are the same
        assertEquals(removeEpochTime(uuid1), removeEpochTime(uuid2));
    }

    @Test
    void different_classloaderId_leads_to_different_uuid_when_ignoring_epoch_time() {
        // Given the two generator have the different classloaderId
        UuidGenerator generator1 = getUuidGeneratorFromOtherClassloader(1);
        UuidGenerator generator2 = getUuidGeneratorFromOtherClassloader(2);

        // When the UUID are generated
        UUID uuid1 = generator1.generateId();
        UUID uuid2 = generator2.generateId();

        // Then the UUID are the same
        assertNotEquals(removeEpochTime(uuid1), removeEpochTime(uuid2));
    }

    @Test
    void setClassloaderId_keeps_only_12_bits() throws NoSuchFieldException, IllegalAccessException {
        // When the classloaderId is defined with a value higher than 0xfff (12
        // bits)
        IncrementingUuidGenerator.setClassloaderId(0xfffffABC);

        // Then the classloaderId is truncated to 12 bits
        assertEquals(0x0ABC, getStaticFieldValue(new IncrementingUuidGenerator(), CLASSLOADER_ID_FIELD_NAME));
    }

    @Test
    void setClassloaderId_keeps_values_under_12_bits_unmodified() throws NoSuchFieldException, IllegalAccessException {
        // When the classloaderId is defined with a value lower than 0xfff (12
        // bits)
        IncrementingUuidGenerator.setClassloaderId(0x0123);

        // Then the classloaderId value is left unmodified
        assertEquals(0x0123, getStaticFieldValue(new IncrementingUuidGenerator(), CLASSLOADER_ID_FIELD_NAME));
    }

    private Long getStaticFieldValue(UuidGenerator generator, String fieldName)
            throws NoSuchFieldException, IllegalAccessException {
        // The Field cannot be cached because the IncrementingUuidGenerator
        // class is different at each call (because it was loaded by a
        // different classloader).
        Field declaredField = generator.getClass().getDeclaredField(fieldName);
        declaredField.setAccessible(true);
        return (Long) declaredField.get(null);
    }

    private static void setClassloaderId(Class<?> generatorClass, int value)
            throws IllegalAccessException, NoSuchMethodException, InvocationTargetException {
        // The Method cannot be cached because the IncrementingUuidGenerator
        // class is different at each call (because it was loaded by a
        // different classloader).
        Method method = generatorClass.getDeclaredMethod("setClassloaderId", int.class);
        method.setAccessible(true);
        method.invoke(null, value);
    }

    /**
     * Create a fresh new IncrementingUuidGenerator from a fresh new
     * classloader, and return a new instance.
     * 
     * @param  classloaderId the classloader unique identifier, or null if the
     *                       default classloader id generator must be used
     * @return               a new IncrementingUuidGenerator instance
     */
    private static UuidGenerator getUuidGeneratorFromOtherClassloader(Integer classloaderId) {
        try {
            Class<?> aClass = new NonCachingClassLoader().findClass(IncrementingUuidGenerator.class.getName());
            if (classloaderId != null) {
                setClassloaderId(aClass, classloaderId);
            }
            return (UuidGenerator) aClass.getConstructor().newInstance();
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
                return Files.readAllBytes(Paths.get(Objects.requireNonNull(NonCachingClassLoader.class
                        .getResource(className.replaceFirst(".+\\.", "") + ".class")).toURI()));
            } catch (IOException e) {
                throw new RuntimeException("Unable to read file from disk");
            } catch (URISyntaxException e) {
                throw new RuntimeException(e);
            }
        }
    }
}
