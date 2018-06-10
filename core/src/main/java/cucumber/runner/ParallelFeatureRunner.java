package cucumber.runner;

import cucumber.runtime.CucumberException;
import cucumber.runtime.FeatureCompiler;
import cucumber.runtime.RunnerSupplier;
import cucumber.runtime.filter.Filters;
import cucumber.runtime.model.CucumberFeature;

import gherkin.events.PickleEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ParallelFeatureRunner {

    private ParallelFeatureRunner() {
        // Hide constructor
    }

    public static void run(final Filters filters,
                           final RunnerSupplier runnerSupplier,
                           final List<CucumberFeature> features,
                           final int requestedThreads) {

        final FeatureCompiler compiler = new FeatureCompiler();
        final ExecutorService executor = Executors.newFixedThreadPool(requestedThreads);
        try {
            for (final CucumberFeature feature : features) {
                final List<PickleEvent> pickleEvents = compiler.compileFeature(feature);
                final List<ParallelPickleRunner> tasks = new ArrayList<ParallelPickleRunner>(pickleEvents.size());
                for (final PickleEvent pickleEvent : pickleEvents) {
                    if (filters.matchesFilters(pickleEvent)) {
                        tasks.add(new ParallelPickleRunner(runnerSupplier, pickleEvent));
                    }
                }
                if (!tasks.isEmpty()) {
                    executor.invokeAll(tasks);
                }
            }
        }
        catch (final InterruptedException e) {
            throw new CucumberException(e);
        }
        finally {
            executor.shutdown();
        }
    }

    private static class ParallelPickleRunner implements Callable<Void> {

        private final RunnerSupplier runnerSupplier;
        private final PickleEvent pickleEvent;

        ParallelPickleRunner(final RunnerSupplier runnerSupplier, final PickleEvent pickleEvent) {
            this.runnerSupplier = runnerSupplier;
            this.pickleEvent = pickleEvent;
        }

        @Override
        public Void call() {
            runnerSupplier.get().runPickle(pickleEvent);
            return null;
        }
    }
}
