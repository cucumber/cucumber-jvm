package io.cucumber.core.runtime;

import io.cucumber.core.backend.DefaultObjectFactory;
import io.cucumber.core.backend.ObjectFactory;
import io.cucumber.core.backend.Options;
import io.cucumber.core.eventbus.IncrementingUuidGenerator;
import io.cucumber.core.eventbus.RandomUuidGenerator;
import io.cucumber.core.eventbus.UuidGenerator;
import io.cucumber.core.exception.CucumberException;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.util.function.Supplier;

import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.IsInstanceOf.instanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Testcases for `ObjectFactoryServiceLoader`
 *
 * <!-- @formatter:off -->
 * | # | object-factory property | Available services                                    | Result                                                                           |
 * |---|-------------------------|-------------------------------------------------------|----------------------------------------------------------------------------------|
 * | 1 | undefined               | none                                                  | exception, no generators available                                               |
 * | 2 | undefined               | DefaultObjectFactory                                  | DefaultObjectFactory used                                                        |
 * | 3 | DefaultObjectFactory    | DefaultObjectFactory                                  | DefaultObjectFactory used                                                        |
 * | 4 | undefined               | DefaultObjectFactory, OtherFactory                    | OtherFactory used                                                                |
 * | 5 | DefaultObjectFactory    | DefaultObjectFactory, OtherFactory                    | DefaultObjectFactory used                                                        |
 * | 6 | undefined               | DefaultObjectFactory, OtherFactory, YetAnotherFactory | exception, cucumber couldn't  decide multiple (non default) generators available |
 * | 7 | OtherFactory            | DefaultObjectFactory, OtherFactory, YetAnotherFactory | OtherFactory used                                                                |
 * | 8 | OtherFactory            | DefaultObjectFactory                                  | exception, class not found through SPI                                           |
 * | 9 | undefined               | OtherFactory                                          | OtherFactory used                                                                |
 * <!-- @formatter:on -->
 *
 * Essentially this means that
 * * (2) Cucumber works by default
 * * (4) When adding a custom implementation to the class path it is used automatically
 * * When cucumber should not guess (5) or can not guess (7), the property is used to force a choice
 */
class ObjectFactoryServiceLoaderTest {

    /**
     * Test case #1
     */
    @Test
    void shouldThrowIfDefaultObjectFactoryServiceCouldNotBeLoaded() {
        Options options = () -> null;
        Supplier<ClassLoader> classLoader = () -> new ServiceLoaderTestClassLoader(ObjectFactory.class);
        ObjectFactoryServiceLoader loader = new ObjectFactoryServiceLoader(
            classLoader,
            options);

        CucumberException exception = assertThrows(CucumberException.class, loader::loadObjectFactory);
        assertThat(exception.getMessage(), is("" +
                "Could not find any object factory.\n" +
                "\n" +
                "Cucumber uses SPI to discover object factory implementations.\n" +
                "This typically happens when using shaded jars. Make sure\n" +
                "to merge all SPI definitions in META-INF/services correctly"));
    }

    /**
     * Test case #2
     */
    @Test
    void shouldLoadDefaultObjectFactoryService() {
        Options options = () -> null;
        ObjectFactoryServiceLoader loader = new ObjectFactoryServiceLoader(
            ObjectFactoryServiceLoaderTest.class::getClassLoader,
            options);
        assertThat(loader.loadObjectFactory(), instanceOf(DefaultObjectFactory.class));
    }

    /**
     * Test case #3
     */
    @Test
    void shouldLoadSelectedObjectFactoryService() {
        Options options = () -> DefaultObjectFactory.class;
        ObjectFactoryServiceLoader loader = new ObjectFactoryServiceLoader(
            ObjectFactoryServiceLoaderTest.class::getClassLoader,
            options);
        assertThat(loader.loadObjectFactory(), instanceOf(DefaultObjectFactory.class));
    }

    /**
     * Test-case #4
     */
    @Test
    void test_case_4() {
        io.cucumber.core.backend.Options options = () -> null;
        ObjectFactoryServiceLoader loader = new ObjectFactoryServiceLoader(
            () -> new ServiceLoaderTestClassLoader(ObjectFactory.class,
                DefaultObjectFactory.class,
                OtherFactory.class),
            options);
        assertThat(loader.loadObjectFactory(), instanceOf(OtherFactory.class));
    }

    /**
     * Test-case #4 bis (reverse order)
     */
    @Test
    void test_case_4_with_services_in_reverse_order() {
        io.cucumber.core.backend.Options options = () -> null;
        ObjectFactoryServiceLoader loader = new ObjectFactoryServiceLoader(
            () -> new ServiceLoaderTestClassLoader(ObjectFactory.class,
                OtherFactory.class,
                DefaultObjectFactory.class),
            options);
        assertThat(loader.loadObjectFactory(), instanceOf(OtherFactory.class));
    }

    /**
     * Test-case #5
     */
    @Test
    void test_case_5() {
        io.cucumber.core.backend.Options options = () -> DefaultObjectFactory.class;
        ObjectFactoryServiceLoader loader = new ObjectFactoryServiceLoader(
            () -> new ServiceLoaderTestClassLoader(ObjectFactory.class,
                DefaultObjectFactory.class,
                OtherFactory.class),
            options);
        assertThat(loader.loadObjectFactory(), instanceOf(DefaultObjectFactory.class));
    }

    /**
     * Test case #6
     */
    @Test
    void test_case_6() {
        // Given
        Options options = () -> null;
        ObjectFactoryServiceLoader loader = new ObjectFactoryServiceLoader(
            () -> new ServiceLoaderTestClassLoader(ObjectFactory.class,
                DefaultObjectFactory.class,
                OtherFactory.class,
                YetAnotherFactory.class),
            options);

        // When
        CucumberException exception = assertThrows(CucumberException.class, loader::loadObjectFactory);

        // Then
        assertThat(exception.getMessage(),
            containsString("More than one Cucumber ObjectFactory was found on the classpath"));
    }

    /**
     * Test-case #7
     */
    @Test
    void test_case_7() {
        io.cucumber.core.backend.Options options = () -> OtherFactory.class;
        ObjectFactoryServiceLoader loader = new ObjectFactoryServiceLoader(
            () -> new ServiceLoaderTestClassLoader(ObjectFactory.class,
                DefaultObjectFactory.class,
                OtherFactory.class,
                YetAnotherFactory.class),
            options);
        assertThat(loader.loadObjectFactory(), instanceOf(OtherFactory.class));
    }

    /**
     * Test case #8
     */
    @Test
    void shouldThrowIfSelectedObjectFactoryServiceCouldNotBeLoaded() {

        Options options = () -> OtherFactory.class;
        ObjectFactoryServiceLoader loader = new ObjectFactoryServiceLoader(
            () -> new ServiceLoaderTestClassLoader(ObjectFactory.class),
            options);

        CucumberException exception = assertThrows(CucumberException.class, loader::loadObjectFactory);
        assertThat(exception.getMessage(), is("" +
                "Could not find object factory io.cucumber.core.runtime.ObjectFactoryServiceLoaderTest$OtherFactory.\n"
                +
                "\n" +
                "Cucumber uses SPI to discover object factory implementations.\n" +
                "Has the class been registered with SPI and is it available on\n" +
                "the classpath?"));
    }

    /**
     * Test-case #9
     */
    @Test
    void test_case_9() {
        io.cucumber.core.backend.Options options = () -> null;
        ObjectFactoryServiceLoader loader = new ObjectFactoryServiceLoader(
            () -> new ServiceLoaderTestClassLoader(ObjectFactory.class,
                OtherFactory.class),
            options);
        assertThat(loader.loadObjectFactory(), instanceOf(OtherFactory.class));
    }

    public static class FakeObjectFactory implements ObjectFactory {

        @Override
        public boolean addClass(Class<?> glueClass) {
            return false;
        }

        @Override
        public <T> T getInstance(Class<T> glueClass) {
            return null;
        }

        @Override
        public void start() {

        }

        @Override
        public void stop() {

        }

    }

    public static class OtherFactory extends FakeObjectFactory {
    }

    public static class YetAnotherFactory extends FakeObjectFactory {
    }
}
