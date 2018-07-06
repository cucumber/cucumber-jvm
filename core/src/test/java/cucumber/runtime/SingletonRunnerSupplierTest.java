package cucumber.runtime;


import cucumber.runner.EventBus;
import cucumber.runner.Runner;
import cucumber.runner.TimeService;
import cucumber.runner.TimeServiceEventBus;
import cucumber.runtime.io.MultiLoader;
import cucumber.runtime.io.ResourceLoader;
import cucumber.runtime.io.ResourceLoaderClassFinder;
import org.junit.Before;
import org.junit.Test;

import java.util.Collections;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertThat;


public class SingletonRunnerSupplierTest {

    private SingletonRunnerSupplier runnerSupplier;

    @Before
    public void before() {
        ClassLoader classLoader = getClass().getClassLoader();
        RuntimeOptions runtimeOptions = new RuntimeOptions(Collections.<String>emptyList());
        ResourceLoader resourceLoader = new MultiLoader(classLoader);
        ClassFinder classFinder = new ResourceLoaderClassFinder(resourceLoader, classLoader);
        BackendModuleBackendSupplier backendSupplier = new BackendModuleBackendSupplier(resourceLoader, classFinder, runtimeOptions);
        EventBus eventBus = new TimeServiceEventBus(TimeService.SYSTEM);
        RuntimeGlueSupplier glueSupplier = new RuntimeGlueSupplier();
        runnerSupplier = new SingletonRunnerSupplier(runtimeOptions, eventBus, backendSupplier, glueSupplier);
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