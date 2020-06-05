package io.cucumber.core.runtime;

import io.cucumber.core.eventbus.EventBus;
import io.cucumber.core.options.RuntimeOptions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.util.UUID;
import java.util.function.Supplier;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.hamcrest.core.IsNull.notNullValue;

class SingletonRunnerSupplierTest {

    private SingletonRunnerSupplier runnerSupplier;

    @BeforeEach
    void before() {
        Supplier<ClassLoader> classLoader = SingletonRunnerSupplier.class::getClassLoader;
        RuntimeOptions runtimeOptions = RuntimeOptions.defaultOptions();
        ObjectFactoryServiceLoader objectFactoryServiceLoader = new ObjectFactoryServiceLoader(runtimeOptions);
        ObjectFactorySupplier objectFactory = new SingletonObjectFactorySupplier(objectFactoryServiceLoader);
        BackendServiceLoader backendSupplier = new BackendServiceLoader(getClass()::getClassLoader, objectFactory);
        EventBus eventBus = new TimeServiceEventBus(Clock.systemUTC(), UUID::randomUUID);
        TypeRegistryConfigurerSupplier typeRegistryConfigurerSupplier = new ScanningTypeRegistryConfigurerSupplier(
            classLoader, runtimeOptions);
        runnerSupplier = new SingletonRunnerSupplier(runtimeOptions, eventBus, backendSupplier, objectFactory,
            typeRegistryConfigurerSupplier);
    }

    @Test
    void should_create_a_runner() {
        assertThat(runnerSupplier.get(), is(notNullValue()));
    }

    @Test
    void should_return_the_same_runner_on_subsequent_calls() {
        assertThat(runnerSupplier.get(), is(equalTo(runnerSupplier.get())));
    }

}
