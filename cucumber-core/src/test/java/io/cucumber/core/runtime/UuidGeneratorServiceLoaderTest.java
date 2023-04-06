package io.cucumber.core.runtime;

import io.cucumber.core.eventbus.IncrementingUuidGenerator;
import io.cucumber.core.eventbus.Options;
import io.cucumber.core.eventbus.RandomUuidGenerator;
import io.cucumber.core.eventbus.UuidGenerator;
import io.cucumber.core.exception.CucumberException;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;

import java.util.*;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.IsInstanceOf.instanceOf;
import static org.junit.jupiter.api.Assertions.*;

/**
 * # Testcases for `UuidGeneratorServiceLoader`
 *
 * <!-- @formatter:off -->
 * | #   | uuid-generator property   | Available services                                                                  | Result                                                                           |
 * |-----|---------------------------|-------------------------------------------------------------------------------------|----------------------------------------------------------------------------------|
 * | 1   | undefined                 | none                                                                                | exception, no generators available                                               |
 * | 2   | undefined                 | RandomUuidGenerator, IncrementingUuidGenerator                                      | RandomUuidGenerator used                                                         |
 * | 3   | RandomUuidGenerator       | RandomUuidGenerator, IncrementingUuidGenerator                                      | RandomUuidGenerator used                                                         |
 * | 4   | undefined                 | RandomUuidGenerator, IncrementingUuidGenerator, OtherGenerator                      | OtherGenerator used                                                              |
 * | 5   | RandomUuidGenerator       | RandomUuidGenerator, IncrementingUuidGenerator, OtherGenerator                      | RandomUuidGenerator used                                                         |
 * | 6   | undefined                 | RandomUuidGenerator, IncrementingUuidGenerator, OtherGenerator, YetAnotherGenerator | exception, cucumber couldn't  decide multiple (non default) generators available |
 * | 7   | OtherGenerator            | RandomUuidGenerator, IncrementingUuidGenerator, OtherGenerator, YetAnotherGenerator | OtherGenerator used                                                              |
 * | 8   | IncrementingUuidGenerator | RandomUuidGenerator, IncrementingUuidGenerator, OtherGenerator, YetAnotherGenerator | IncrementingUuidGenerator used                                                   |
 * | 9   | IncrementingUuidGenerator | RandomUuidGenerator, IncrementingUuidGenerator                                      | IncrementingUuidGenerator used                                                   |
 * | 10  | OtherGenerator            | none                                                                                | exception, generator OtherGenerator not available                                |
 * | 11  | undefined                 | OtherGenerator                                                                      | OtherGenerator used                                                              |
 * | 12  | undefined                 | IncrementingUuidGenerator, OtherGenerator                                           | OtherGenerator used                                                              |
 * | 13  | undefined                 | IncrementingUuidGenerator                                                           | IncrementingUuidGenerator used                                                   |
 * <!-- @formatter:on -->
 */
class UuidGeneratorServiceLoaderTest {

    /**
     * | 1 | undefined | none | exception, no generators available |
     */
    @Test
    void test_case_1() {
        Options options = () -> null;
        UuidGeneratorServiceLoader loader = new UuidGeneratorServiceLoader(
            () -> new ServiceLoaderTestClassLoader(UuidGenerator.class),
            options);

        CucumberException exception = assertThrows(CucumberException.class, loader::loadUuidGenerator);
        assertThat(exception.getMessage(), is("" +
                "Could not find any UUID generator.\n" +
                "\n" +
                "Cucumber uses SPI to discover UUID generator implementations.\n" +
                "This typically happens when using shaded jars. Make sure\n" +
                "to merge all SPI definitions in META-INF/services correctly"));
    }

    /**
     * | 2 | undefined | RandomUuidGenerator, IncrementingUuidGenerator |
     * RandomUuidGenerator used |
     */
    @Test
    void test_case_2() {
        Options options = () -> null;
        UuidGeneratorServiceLoader loader = new UuidGeneratorServiceLoader(
            UuidGeneratorServiceLoaderTest.class::getClassLoader,
            options);
        assertThat(loader.loadUuidGenerator(), instanceOf(RandomUuidGenerator.class));
    }

    /**
     * | 3 | RandomUuidGenerator | RandomUuidGenerator,
     * IncrementingUuidGenerator | RandomUuidGenerator used |
     */
    @Test
    void test_case_3() {
        Options options = () -> RandomUuidGenerator.class;
        UuidGeneratorServiceLoader loader = new UuidGeneratorServiceLoader(
            UuidGeneratorServiceLoaderTest.class::getClassLoader,
            options);
        assertThat(loader.loadUuidGenerator(), instanceOf(RandomUuidGenerator.class));
    }

    /**
     * | 4 | undefined | RandomUuidGenerator, IncrementingUuidGenerator,
     * OtherGenerator | OtherGenerator used |
     */
    @Test
    void test_case_4() {
        Options options = () -> null;
        UuidGeneratorServiceLoader loader = new UuidGeneratorServiceLoader(
            () -> new ServiceLoaderTestClassLoader(UuidGenerator.class,
                RandomUuidGenerator.class,
                IncrementingUuidGenerator.class,
                OtherGenerator.class),
            options);
        assertThat(loader.loadUuidGenerator(), instanceOf(OtherGenerator.class));
    }

    /**
     * | 4bis | undefined | OtherGenerator, RandomUuidGenerator,
     * IncrementingUuidGenerator | OtherGenerator used |
     */
    @Test
    void test_case_4_bis() {
        Options options = () -> null;
        UuidGeneratorServiceLoader loader = new UuidGeneratorServiceLoader(
            () -> new ServiceLoaderTestClassLoader(UuidGenerator.class,
                OtherGenerator.class,
                RandomUuidGenerator.class,
                IncrementingUuidGenerator.class),
            options);
        assertThat(loader.loadUuidGenerator(), instanceOf(OtherGenerator.class));
    }

    /**
     * | 5 | RandomUuidGenerator | RandomUuidGenerator,
     * IncrementingUuidGenerator, OtherGenerator | RandomUuidGenerator used |
     */
    @Test
    void test_case_5() {
        Options options = () -> RandomUuidGenerator.class;
        UuidGeneratorServiceLoader loader = new UuidGeneratorServiceLoader(
            () -> new ServiceLoaderTestClassLoader(UuidGenerator.class,
                RandomUuidGenerator.class,
                IncrementingUuidGenerator.class,
                OtherGenerator.class),
            options);
        assertThat(loader.loadUuidGenerator(), instanceOf(RandomUuidGenerator.class));
    }

    /**
     * | 6 | undefined | RandomUuidGenerator, IncrementingUuidGenerator,
     * OtherGenerator, YetAnotherGenerator | exception, cucumber couldn't decide
     * multiple (non default) generators available |
     */
    @Test
    void test_case_6() {
        // Given
        Options options = () -> null;
        UuidGeneratorServiceLoader loader = new UuidGeneratorServiceLoader(
            () -> new ServiceLoaderTestClassLoader(UuidGenerator.class,
                RandomUuidGenerator.class,
                IncrementingUuidGenerator.class,
                OtherGenerator.class,
                YetAnotherGenerator.class),
            options);

        // When
        CucumberException cucumberException = assertThrows(CucumberException.class, loader::loadUuidGenerator);

        // Then
        assertThat(cucumberException.getMessage(),
            Matchers.containsString("More than one Cucumber UuidGenerator was found on the classpath"));
    }

    /**
     * | 7 | OtherGenerator | RandomUuidGenerator, IncrementingUuidGenerator,
     * OtherGenerator, YetAnotherGenerator | OtherGenerator used |
     */
    @Test
    void test_case_7() {
        Options options = () -> OtherGenerator.class;
        UuidGeneratorServiceLoader loader = new UuidGeneratorServiceLoader(
            () -> new ServiceLoaderTestClassLoader(UuidGenerator.class,
                RandomUuidGenerator.class,
                IncrementingUuidGenerator.class,
                OtherGenerator.class,
                YetAnotherGenerator.class),
            options);
        assertThat(loader.loadUuidGenerator(), instanceOf(OtherGenerator.class));
    }

    /**
     * | 8 | IncrementingUuidGenerator | RandomUuidGenerator,
     * IncrementingUuidGenerator, OtherGenerator, YetAnotherGenerator |
     * IncrementingUuidGenerator used |
     */
    @Test
    void test_case_8() {
        Options options = () -> IncrementingUuidGenerator.class;
        UuidGeneratorServiceLoader loader = new UuidGeneratorServiceLoader(
            () -> new ServiceLoaderTestClassLoader(UuidGenerator.class,
                RandomUuidGenerator.class,
                IncrementingUuidGenerator.class,
                OtherGenerator.class,
                YetAnotherGenerator.class),
            options);
        assertThat(loader.loadUuidGenerator(), instanceOf(IncrementingUuidGenerator.class));
    }

    /**
     * | 9 | IncrementingUuidGenerator | RandomUuidGenerator,
     * IncrementingUuidGenerator | IncrementingUuidGenerator used |
     */
    @Test
    void test_case_9() {
        Options options = () -> IncrementingUuidGenerator.class;
        UuidGeneratorServiceLoader loader = new UuidGeneratorServiceLoader(
            UuidGeneratorServiceLoaderTest.class::getClassLoader,
            options);
        assertThat(loader.loadUuidGenerator(), instanceOf(IncrementingUuidGenerator.class));
    }

    /**
     * | 10 | OtherGenerator | none | exception, generator OtherGenerator not
     * available |
     */
    @Test
    void test_case_10() {

        Options options = () -> OtherGenerator.class;
        UuidGeneratorServiceLoader loader = new UuidGeneratorServiceLoader(
            () -> new ServiceLoaderTestClassLoader(UuidGenerator.class),
            options);

        CucumberException exception = assertThrows(CucumberException.class, loader::loadUuidGenerator);
        assertThat(exception.getMessage(), is("" +
                "Could not find UUID generator io.cucumber.core.runtime.UuidGeneratorServiceLoaderTest$OtherGenerator.\n"
                +
                "\n" +
                "Cucumber uses SPI to discover UUID generator implementations.\n" +
                "Has the class been registered with SPI and is it available on\n" +
                "the classpath?"));
    }

    /**
     * | 11 | undefined | OtherGenerator | OtherGenerator used |
     */
    @Test
    void test_case_11() {
        Options options = () -> null;
        UuidGeneratorServiceLoader loader = new UuidGeneratorServiceLoader(
            () -> new ServiceLoaderTestClassLoader(UuidGenerator.class,
                OtherGenerator.class),
            options);
        assertThat(loader.loadUuidGenerator(), instanceOf(OtherGenerator.class));
    }

    /**
     * | 12 | undefined | IncrementingUuidGenerator, OtherGenerator |
     * OtherGenerator used |
     */
    @Test
    void test_case_12() {
        Options options = () -> null;
        UuidGeneratorServiceLoader loader = new UuidGeneratorServiceLoader(
            () -> new ServiceLoaderTestClassLoader(UuidGenerator.class,
                IncrementingUuidGenerator.class,
                OtherGenerator.class),
            options);

        assertThat(loader.loadUuidGenerator(), instanceOf(OtherGenerator.class));
    }

    /**
     * | 13 | undefined | IncrementingUuidGenerator | IncrementingUuidGenerator
     * used |
     */
    @Test
    void test_case_13() {
        Options options = () -> null;
        UuidGeneratorServiceLoader loader = new UuidGeneratorServiceLoader(
            () -> new ServiceLoaderTestClassLoader(UuidGenerator.class,
                IncrementingUuidGenerator.class),
            options);
        assertThat(loader.loadUuidGenerator(), instanceOf(IncrementingUuidGenerator.class));
    }

    public static class OtherGenerator implements UuidGenerator {
        @Override
        public UUID generateId() {
            return null;
        }
    }

    public static class YetAnotherGenerator implements UuidGenerator {
        @Override
        public UUID generateId() {
            return null;
        }
    }

}
