package cucumber.runtime;

import cucumber.runtime.io.MultiLoader;
import cucumber.runtime.io.ResourceLoader;
import cucumber.runtime.io.ResourceLoaderClassFinder;
import io.cucumber.core.options.RuntimeOptions;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.net.URI;

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
        RuntimeOptions runtimeOptions = RuntimeOptions.defaultOptions();
        ResourceLoader resourceLoader = new MultiLoader(classLoader);
        ClassFinder classFinder = new ResourceLoaderClassFinder(resourceLoader, classLoader);
        BackendSupplier backendSupplier = new BackendModuleBackendSupplier(resourceLoader, classFinder, runtimeOptions);
        assertThat(backendSupplier.get().iterator().next(), is(notNullValue()));
    }

    @Test
    public void should_throw_an_exception_when_no_backend_could_be_found() {
        ClassLoader classLoader = getClass().getClassLoader();
        RuntimeOptions runtimeOptions = RuntimeOptions.defaultOptions();
        ResourceLoader resourceLoader = new MultiLoader(classLoader);
        ClassFinder classFinder = new ResourceLoaderClassFinder(resourceLoader, classLoader);
        BackendSupplier backendSupplier = new BackendModuleBackendSupplier(resourceLoader, classFinder, runtimeOptions, singletonList(URI.create("classpath:no/backend/here")));

        expectedException.expect(CucumberException.class);
        assertThat(backendSupplier.get().iterator().next(), is(notNullValue()));
    }

}
