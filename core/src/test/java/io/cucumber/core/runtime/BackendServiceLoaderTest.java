package io.cucumber.core.runtime;

import io.cucumber.core.backend.ObjectFactoryServiceLoader;
import io.cucumber.core.exception.CucumberException;
import io.cucumber.core.io.MultiLoader;
import io.cucumber.core.io.ResourceLoader;
import io.cucumber.core.options.RuntimeOptions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;

import static java.util.Collections.emptyList;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class BackendServiceLoaderTest {

    @Test
    public void should_create_a_backend() {
        ClassLoader classLoader = getClass().getClassLoader();
        RuntimeOptions runtimeOptions = RuntimeOptions.defaultOptions();
        ResourceLoader resourceLoader = new MultiLoader(classLoader);
        ObjectFactoryServiceLoader objectFactoryServiceLoader = new ObjectFactoryServiceLoader(runtimeOptions);
        ObjectFactorySupplier objectFactory = new SingletonObjectFactorySupplier(objectFactoryServiceLoader);
        BackendSupplier backendSupplier = new BackendServiceLoader(resourceLoader, objectFactory);
        assertThat(backendSupplier.get().iterator().next(), is(notNullValue()));
    }

    @Test
    public void should_throw_an_exception_when_no_backend_could_be_found() {
        ClassLoader classLoader = getClass().getClassLoader();
        RuntimeOptions runtimeOptions = RuntimeOptions.defaultOptions();
        ResourceLoader resourceLoader = new MultiLoader(classLoader);
        ObjectFactoryServiceLoader objectFactoryServiceLoader = new ObjectFactoryServiceLoader(runtimeOptions);
        ObjectFactorySupplier objectFactory = new SingletonObjectFactorySupplier(objectFactoryServiceLoader);
        BackendServiceLoader backendSupplier = new BackendServiceLoader(resourceLoader, objectFactory);

        final Executable testMethod = () -> backendSupplier.get(emptyList()).iterator().next();
        final CucumberException actualThrown = assertThrows(CucumberException.class, testMethod);
        assertThat("Unexpected exception message", actualThrown.getMessage(), is(equalTo(
            "No backends were found. Please make sure you have a backend module on your CLASSPATH."
        )));
    }

}
