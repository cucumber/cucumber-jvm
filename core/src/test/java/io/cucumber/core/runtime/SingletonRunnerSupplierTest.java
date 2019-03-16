package io.cucumber.core.runtime;


import io.cucumber.core.backend.ObjectFactorySupplier;
import io.cucumber.core.backend.SingletonObjectFactorySupplier;
import io.cucumber.core.event.EventBus;
import io.cucumber.core.io.ClassFinder;
import io.cucumber.core.io.MultiLoader;
import io.cucumber.core.io.ResourceLoader;
import io.cucumber.core.io.ResourceLoaderClassFinder;
import io.cucumber.core.options.RuntimeOptions;
import io.cucumber.core.runner.TimeService;
import io.cucumber.core.runner.TimeServiceEventBus;
import org.junit.Before;
import org.junit.Test;

import static io.cucumber.core.options.Env.INSTANCE;
import static java.util.Collections.emptyList;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertThat;


public class SingletonRunnerSupplierTest {

    private SingletonRunnerSupplier runnerSupplier;

    @Before
    public void before() {
        ClassLoader classLoader = getClass().getClassLoader();
        ResourceLoader resourceLoader = new MultiLoader(classLoader);
        RuntimeOptions runtimeOptions = new RuntimeOptions(resourceLoader, INSTANCE, emptyList());
        ClassFinder classFinder = new ResourceLoaderClassFinder(resourceLoader, classLoader);
        ObjectFactorySupplier objectFactory = new SingletonObjectFactorySupplier();
        BackendServiceLoader backendSupplier = new BackendServiceLoader(resourceLoader, classFinder, runtimeOptions, objectFactory);
        EventBus eventBus = new TimeServiceEventBus(TimeService.SYSTEM);
        runnerSupplier = new SingletonRunnerSupplier(runtimeOptions, eventBus, backendSupplier, objectFactory);
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