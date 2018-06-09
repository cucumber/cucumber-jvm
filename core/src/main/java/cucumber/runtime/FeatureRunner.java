package cucumber.runtime;

import cucumber.runtime.filter.Filters;
import cucumber.runtime.model.CucumberFeature;
import gherkin.events.PickleEvent;

public class FeatureRunner {

    private final FeatureCompiler compiler = new FeatureCompiler();
    private final Filters filters;
    private final RunnerSupplier runnerSupplier;

    public FeatureRunner(final Filters filters, final RunnerSupplier runnerSupplier) {
        this.filters = filters;
        this.runnerSupplier = runnerSupplier;
    }

    public void runFeature(CucumberFeature feature) {
        for (PickleEvent pickleEvent : compiler.compileFeature(feature)) {
            if (filters.matchesFilters(pickleEvent)) {
                runnerSupplier.get().runPickle(pickleEvent);
            }
        }
    }
}
