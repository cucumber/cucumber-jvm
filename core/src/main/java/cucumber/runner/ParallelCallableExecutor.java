package cucumber.runner;

import cucumber.runtime.CucumberException;

import java.util.Collection;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ParallelCallableExecutor<T> {
    
    public void run(final Collection<Callable<T>> tasks) {
        if (!tasks.isEmpty()) {
            final ExecutorService executor = Executors.newFixedThreadPool(tasks.size());
            try {
                executor.invokeAll(tasks);
            }
            catch (final InterruptedException e) {
                throw new CucumberException(e);
            }
            finally {
                executor.shutdown();
            }
        }
    }
}
