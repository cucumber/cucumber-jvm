package io.cucumber.core.runtime;

import io.cucumber.core.feature.CucumberFeature;

import java.util.List;

public interface FeatureSupplier {
    List<CucumberFeature> get();
}
