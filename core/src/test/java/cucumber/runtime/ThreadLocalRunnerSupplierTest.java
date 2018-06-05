package cucumber.runtime;


import cucumber.runner.EventBus;
import cucumber.runner.Runner;
import cucumber.runner.TimeService;
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


public class ThreadLocalRunnerSupplierTest {

    private ThreadLocalRunnerSupplier runnerSupplier;

    @Before
    public void before() {
        ClassLoader classLoader = getClass().getClassLoader();
        RuntimeOptions runtimeOptions = new RuntimeOptions(Collections.<String>emptyList());
        ResourceLoader resourceLoader = new MultiLoader(classLoader);
        ClassFinder classFinder = new ResourceLoaderClassFinder(resourceLoader, classLoader);
        BackendModuleBackendSupplier backendSupplier = new BackendModuleBackendSupplier(resourceLoader, classFinder, runtimeOptions);
        EventBus eventBus = new EventBus(TimeService.SYSTEM);
        RuntimeGlueSupplier glueSupplier = new RuntimeGlueSupplier();
        runnerSupplier = new ThreadLocalRunnerSupplier(runtimeOptions, eventBus, backendSupplier, glueSupplier);
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

}