package io.cucumber.core.runtime;

import io.cucumber.core.exception.CucumberException;
import io.cucumber.core.options.RuntimeOptions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;

import java.util.function.Supplier;

import static java.util.Collections.emptyList;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.jupiter.api.Assertions.assertThrows;

class BackendServiceLoaderTest {

    final RuntimeOptions runtimeOptions = RuntimeOptions.defaultOptions();
    final Supplier<ClassLoader> classLoaderSupplier = this.getClass()::getClassLoader;
    final ObjectFactoryServiceLoader objectFactoryServiceLoader = new ObjectFactoryServiceLoader(classLoaderSupplier,
        runtimeOptions);
    final ObjectFactorySupplier objectFactory = new SingletonObjectFactorySupplier(objectFactoryServiceLoader);

    @Test
    void should_create_a_backend() {
        BackendSupplier backendSupplier = new BackendServiceLoader(classLoaderSupplier, objectFactory);
        assertThat(backendSupplier.get().iterator().next(), is(notNullValue()));
    }

    @Test
    void should_throw_an_exception_when_no_backend_could_be_found() {
        BackendServiceLoader backendSupplier = new BackendServiceLoader(classLoaderSupplier, objectFactory);

        Executable testMethod = () -> backendSupplier.get(emptyList()).iterator().next();
        CucumberException actualThrown = assertThrows(CucumberException.class, testMethod);
        assertThat("Unexpected exception message", actualThrown.getMessage(), is(equalTo(
            "No backends were found. Please make sure you have a backend module on your CLASSPATH.")));
    }

}
