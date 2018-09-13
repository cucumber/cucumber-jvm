package io.cucumber.core.runtime;

import io.cucumber.core.api.event.TestSourceRead;
import io.cucumber.core.model.CucumberFeature;
import io.cucumber.core.model.FeatureLoader;
import io.cucumber.core.options.RuntimeOptions;
import io.cucumber.core.event.EventBus;

import java.util.List;

/**
 * Supplies a list of features found on the the feature path provided to RuntimeOptions.
 */
public final class FeaturePathFeatureSupplier implements FeatureSupplier {
    private final FeatureLoader featureLoader;
    private final RuntimeOptions runtimeOptions;
    private final EventBus bus;

    public FeaturePathFeatureSupplier(FeatureLoader featureLoader, RuntimeOptions runtimeOptions, EventBus bus) {
        this.featureLoader = featureLoader;
        this.runtimeOptions = runtimeOptions;
        this.bus = bus;
    }

    @Override
    public List<CucumberFeature> get() {
        List<CucumberFeature> features = featureLoader.load(runtimeOptions.getFeaturePaths(), System.out);
        for (CucumberFeature feature : features) {
            bus.send(new TestSourceRead(bus.getTime(), feature.getUri(), feature.getGherkinSource()));
        }
        return features;
    }
}
