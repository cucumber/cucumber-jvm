package io.cucumber.core.runtime;

import io.cucumber.core.gherkin.Feature;

import java.util.Arrays;
import java.util.List;

public class StubFeatureSupplier implements FeatureSupplier {

    private final List<Feature> features;

    public StubFeatureSupplier(Feature... features) {
        this(Arrays.asList(features));
    }

    public StubFeatureSupplier(List<Feature> features) {
        this.features = features;
    }

    @Override
    public List<Feature> get() {
        return features;
    }

}
