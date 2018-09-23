package io.cucumber.core.runtime;

import io.cucumber.core.backend.BackendSupplier;
import io.cucumber.core.exception.CucumberException;
import io.cucumber.core.io.ClassFinder;
import io.cucumber.core.io.MultiLoader;
import io.cucumber.core.io.ResourceLoader;
import io.cucumber.core.io.ResourceLoaderClassFinder;
import io.cucumber.core.options.Env;
import io.cucumber.core.options.RuntimeOptions;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.util.Collections;

import static java.util.Collections.singletonList;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertThat;

public class BackendModuleBackendSupplierTest {


    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    @Test
    public void should_create_a_backend() {
        ClassLoader classLoader = getClass().getClassLoader();
        RuntimeOptions runtimeOptions = new RuntimeOptions(new MultiLoader(RuntimeOptions.class.getClassLoader()), Env.INSTANCE, Collections.<String>emptyList());
        ResourceLoader resourceLoader = new MultiLoader(classLoader);
        ClassFinder classFinder = new ResourceLoaderClassFinder(resourceLoader, classLoader);
        BackendSupplier backendSupplier = new BackendModuleBackendSupplier(resourceLoader, classFinder, runtimeOptions, singletonList("io.cucumber.core.backend"));
        assertThat(backendSupplier.get().iterator().next(), is(notNullValue()));
    }

    @Test
    public void should_throw_an_exception_when_no_backend_could_be_found() {
        ClassLoader classLoader = getClass().getClassLoader();
        RuntimeOptions runtimeOptions = new RuntimeOptions(new MultiLoader(RuntimeOptions.class.getClassLoader()), Env.INSTANCE, Collections.<String>emptyList());
        ResourceLoader resourceLoader = new MultiLoader(classLoader);
        ClassFinder classFinder = new ResourceLoaderClassFinder(resourceLoader, classLoader);
        BackendSupplier backendSupplier = new BackendModuleBackendSupplier(resourceLoader, classFinder, runtimeOptions, singletonList("no.backend.here"));

        expectedException.expect(CucumberException.class);
        assertThat(backendSupplier.get().iterator().next(), is(notNullValue()));
    }

}
