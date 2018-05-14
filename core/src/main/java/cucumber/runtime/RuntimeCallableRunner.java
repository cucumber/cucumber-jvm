package cucumber.runtime;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class RuntimeCallableRunner {
    
    private RuntimeCallableRunner() {}
    
    public static void run(final List<RuntimeCallable> tasks) {
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
