package cucumber.runner;


import cucumber.runtime.BackendModuleBackendSupplier;
import cucumber.runtime.ClassFinder;
import io.cucumber.core.options.RuntimeOptions;
import cucumber.runtime.io.MultiLoader;
import cucumber.runtime.io.ResourceLoader;
import cucumber.runtime.io.ResourceLoaderClassFinder;
import org.junit.Before;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertThat;


public class SingletonRunnerSupplierTest {

    private SingletonRunnerSupplier runnerSupplier;

    @Before
    public void before() {
        ClassLoader classLoader = getClass().getClassLoader();
        RuntimeOptions runtimeOptions = RuntimeOptions.defaultOptions();
        ResourceLoader resourceLoader = new MultiLoader(classLoader);
        ClassFinder classFinder = new ResourceLoaderClassFinder(resourceLoader, classLoader);
        BackendModuleBackendSupplier backendSupplier = new BackendModuleBackendSupplier(resourceLoader, classFinder, runtimeOptions);
        EventBus eventBus = new TimeServiceEventBus(TimeService.SYSTEM);
        runnerSupplier = new SingletonRunnerSupplier(runtimeOptions, eventBus, backendSupplier);
    }

    @Test
    public void should_create_a_runner() {
        assertThat(runnerSupplier.get(), is(notNullValue()));
    }

    @Test
    public void should_return_the_same_runner_on_subsequent_calls() {
        assertSame(runnerSupplier.get(), runnerSupplier.get());
    }

}