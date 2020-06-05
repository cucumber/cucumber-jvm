package io.cucumber.core.runtime;

import io.cucumber.core.gherkin.Feature;

import java.util.Arrays;
import java.util.List;

public class TestFeatureSupplier implements FeatureSupplier {

    private final List<Feature> features;

    public TestFeatureSupplier(Feature... features) {
        this(Arrays.asList(features));
    }

    public TestFeatureSupplier(List<Feature> features) {
        this.features = features;
    }

    @Override
    public List<Feature> get() {
        return features;
    }

}
