package cucumber.runtime;


import cucumber.api.event.EventHandler;
import cucumber.api.event.TestCaseStarted;
import cucumber.runner.EventBus;
import cucumber.runner.Runner;
import cucumber.runner.ThreadLocalRunnerSupplier;
import cucumber.runner.TimeService;
import cucumber.runner.TimeServiceEventBus;
import cucumber.runtime.io.MultiLoader;
import cucumber.runtime.io.ResourceLoader;
import cucumber.runtime.io.ResourceLoaderClassFinder;
import io.cucumber.core.options.RuntimeOptions;
import org.junit.Before;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;


public class ThreadLocalRunnerSupplierTest {

    private ThreadLocalRunnerSupplier runnerSupplier;
    private TimeServiceEventBus eventBus;

    @Before
    public void before() {
        ClassLoader classLoader = getClass().getClassLoader();
        RuntimeOptions runtimeOptions = RuntimeOptions.defaultOptions();
        ResourceLoader resourceLoader = new MultiLoader(classLoader);
        ClassFinder classFinder = new ResourceLoaderClassFinder(resourceLoader, classLoader);
        BackendModuleBackendSupplier backendSupplier = new BackendModuleBackendSupplier(resourceLoader, classFinder, runtimeOptions);
        eventBus = new TimeServiceEventBus(TimeService.SYSTEM);
        runnerSupplier = new ThreadLocalRunnerSupplier(runtimeOptions, eventBus, backendSupplier);
    }


    @Test
    public void should_create_a_runner() {
        assertThat(runnerSupplier.get(), is(notNullValue()));
    }

    @Test
    public void should_create_a_runner_per_thread() throws InterruptedException {
        final Runner[] runners = new Runner[2];
        Thread thread0 = new Thread(new Runnable() {
            @Override
            public void run() {
                runners[0] = runnerSupplier.get();
            }
        });

        Thread thread1 = new Thread(new Runnable() {
            @Override
            public void run() {
                runners[1] = runnerSupplier.get();
            }
        });

        thread0.start();
        thread1.start();

        thread0.join();
        thread1.join();

        assertNotSame(runners[0], runners[1]);
    }

    @Test
    public void should_return_the_same_runner_on_subsequent_calls() {
        assertSame(runnerSupplier.get(), runnerSupplier.get());
    }

    @Test
    public void runner_should_wrap_event_bus_bus() {
        //This avoids problems with JUnit which listens to individual runners
        EventBus runnerBus = runnerSupplier.get().getBus();
        assertNotSame(eventBus, runnerBus);
    }

    @Test
    public void should_limit_runner_bus_scope_to_events_generated_by_runner() {
        //This avoids problems with JUnit which listens to individual runners
        runnerSupplier.get().getBus().registerHandlerFor(TestCaseStarted.class, new EventHandler<TestCaseStarted>() {
            @Override
            public void receive(TestCaseStarted event) {
                fail();
            }
        });
        eventBus.send(new TestCaseStarted(0L, 0L, null));
    }
}