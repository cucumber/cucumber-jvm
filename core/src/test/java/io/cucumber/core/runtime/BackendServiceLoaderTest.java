package io.cucumber.core.runtime;

import io.cucumber.core.exception.CucumberException;
import io.cucumber.core.io.ClassFinder;
import io.cucumber.core.io.MultiLoader;
import io.cucumber.core.io.ResourceLoader;
import io.cucumber.core.io.ResourceLoaderClassFinder;
import io.cucumber.core.options.RuntimeOptions;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import static java.util.Collections.emptyList;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertThat;

public class BackendServiceLoaderTest {

    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    @Test
    public void should_create_a_backend() {
        ClassLoader classLoader = getClass().getClassLoader();
        RuntimeOptions runtimeOptions = RuntimeOptions.defaultOptions();
        ResourceLoader resourceLoader = new MultiLoader(classLoader);
        ObjectFactorySupplier objectFactory = new SingletonObjectFactorySupplier(runtimeOptions);
        BackendSupplier backendSupplier = new BackendServiceLoader(resourceLoader, objectFactory);
        assertThat(backendSupplier.get().iterator().next(), is(notNullValue()));
    }

    @Test
    public void should_throw_an_exception_when_no_backend_could_be_found() {
        ClassLoader classLoader = getClass().getClassLoader();
        RuntimeOptions runtimeOptions = RuntimeOptions.defaultOptions();
        ResourceLoader resourceLoader = new MultiLoader(classLoader);
        ObjectFactorySupplier objectFactory = new SingletonObjectFactorySupplier(runtimeOptions);
        BackendServiceLoader backendSupplier = new BackendServiceLoader(resourceLoader, objectFactory);

        expectedException.expect(CucumberException.class);
        backendSupplier.get(emptyList()).iterator().next();
    }

}
