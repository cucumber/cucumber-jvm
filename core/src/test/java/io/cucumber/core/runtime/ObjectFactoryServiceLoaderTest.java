package io.cucumber.core.runtime;

import io.cucumber.core.backend.DefaultObjectFactory;
import io.cucumber.core.backend.ObjectFactory;
import io.cucumber.core.backend.Options;
import io.cucumber.core.exception.CucumberException;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Enumeration;
import java.util.function.Supplier;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.IsInstanceOf.instanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ObjectFactoryServiceLoaderTest {

    @Test
    void shouldLoadDefaultObjectFactoryService() {
        Options options = () -> null;
        ObjectFactoryServiceLoader loader = new ObjectFactoryServiceLoader(
            ObjectFactoryServiceLoaderTest.class::getClassLoader,
            options);
        assertThat(loader.loadObjectFactory(), instanceOf(DefaultObjectFactory.class));
    }

    @Test
    void shouldLoadSelectedObjectFactoryService() {
        Options options = () -> DefaultObjectFactory.class;
        ObjectFactoryServiceLoader loader = new ObjectFactoryServiceLoader(
            ObjectFactoryServiceLoaderTest.class::getClassLoader,
            options);
        assertThat(loader.loadObjectFactory(), instanceOf(DefaultObjectFactory.class));
    }

    @Test
    void shouldThrowIfDefaultObjectFactoryServiceCouldNotBeLoaded() {
        Options options = () -> null;
        Supplier<ClassLoader> classLoader = () -> new FilteredClassLoader(
            "META-INF/services/io.cucumber.core.backend.ObjectFactory");
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

    @Test
    void shouldThrowIfSelectedObjectFactoryServiceCouldNotBeLoaded() {

        Options options = () -> NoSuchObjectFactory.class;
        Supplier<ClassLoader> classLoader = () -> new FilteredClassLoader(
            "META-INF/services/io.cucumber.core.backend.ObjectFactory");
        ObjectFactoryServiceLoader loader = new ObjectFactoryServiceLoader(
            classLoader,
            options);

        CucumberException exception = assertThrows(CucumberException.class, loader::loadObjectFactory);
        assertThat(exception.getMessage(), is("" +
                "Could not find object factory io.cucumber.core.runtime.ObjectFactoryServiceLoaderTest$NoSuchObjectFactory.\n"
                +
                "\n" +
                "Cucumber uses SPI to discover object factory implementations.\n" +
                "Has the class been registered with SPI and is it available on\n" +
                "the classpath?"));
    }

    static class NoSuchObjectFactory implements ObjectFactory {

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

    private static class FilteredClassLoader extends URLClassLoader {

        private final Collection<String> filteredResources;

        public FilteredClassLoader(String... filteredResources) {
            super(new URL[0], FilteredClassLoader.class.getClassLoader());
            this.filteredResources = Arrays.asList(filteredResources);
        }

        @Override
        public Enumeration<URL> getResources(String name) throws IOException {
            for (String filteredResource : filteredResources) {
                if (name.equals(filteredResource)) {
                    return Collections.emptyEnumeration();
                }
            }
            return super.getResources(name);
        }

    }

}
