package io.cucumber.core.runtime;

import io.cucumber.core.eventbus.EventBus;
import io.cucumber.core.options.RuntimeOptions;
import io.cucumber.core.runner.Runner;
import io.cucumber.plugin.event.TestCase;
import io.cucumber.plugin.event.TestCaseStarted;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.util.UUID;
import java.util.function.Supplier;

import static java.time.Instant.EPOCH;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.hamcrest.core.IsNot.not;
import static org.hamcrest.core.IsNull.notNullValue;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.fail;
import static org.mockito.Mockito.mock;

class ThreadLocalRunnerSupplierTest {

    private ThreadLocalRunnerSupplier runnerSupplier;
    private TimeServiceEventBus eventBus;

    @BeforeEach
    void before() {
        Supplier<ClassLoader> classLoader = ThreadLocalRunnerSupplierTest.class::getClassLoader;
        RuntimeOptions runtimeOptions = RuntimeOptions.defaultOptions();
        ObjectFactoryServiceLoader objectFactoryServiceLoader = new ObjectFactoryServiceLoader(runtimeOptions);
        ObjectFactorySupplier objectFactory = new SingletonObjectFactorySupplier(objectFactoryServiceLoader);
        BackendServiceLoader backendSupplier = new BackendServiceLoader(classLoader, objectFactory);
        eventBus = new TimeServiceEventBus(Clock.systemUTC(), UUID::randomUUID);
        TypeRegistryConfigurerSupplier typeRegistryConfigurerSupplier = new ScanningTypeRegistryConfigurerSupplier(
            classLoader, runtimeOptions);
        runnerSupplier = new ThreadLocalRunnerSupplier(runtimeOptions, eventBus, backendSupplier, objectFactory,
            typeRegistryConfigurerSupplier);
    }

    @Test
    void should_create_a_runner() {
        assertThat(runnerSupplier.get(), is(notNullValue()));
    }

    @Test
    void should_create_a_runner_per_thread() throws InterruptedException {
        final Runner[] runners = new Runner[2];
        Thread thread0 = new Thread(() -> runners[0] = runnerSupplier.get());

        Thread thread1 = new Thread(() -> runners[1] = runnerSupplier.get());

        thread0.start();
        thread1.start();

        thread0.join();
        thread1.join();

        assertAll(
            () -> assertThat(runners[0], is(not(equalTo(runners[1])))),
            () -> assertThat(runners[1], is(not(equalTo(runners[0])))));
    }

    @Test
    void should_return_the_same_runner_on_subsequent_calls() {
        assertThat(runnerSupplier.get(), is(equalTo(runnerSupplier.get())));
    }

    @Test
    void runner_should_wrap_event_bus_bus() {
        // This avoids problems with JUnit which listens to individual runners
        EventBus runnerBus = runnerSupplier.get().getBus();

        assertAll(
            () -> assertThat(eventBus, is(not(equalTo(runnerBus)))),
            () -> assertThat(runnerBus, is(not(equalTo(eventBus)))));
    }

    @Test
    void should_limit_runner_bus_scope_to_events_generated_by_runner() {
        // This avoids problems with JUnit which listens to individual runners
        runnerSupplier.get().getBus().registerHandlerFor(
            TestCaseStarted.class,
            event -> fail("Should not receive event"));
        eventBus.send(new TestCaseStarted(EPOCH, mock(TestCase.class)));
    }

}
