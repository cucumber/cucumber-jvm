package io.cucumber.core.runtime;

import io.cucumber.core.gherkin.Feature;

import java.util.List;

public interface FeatureSupplier {

    List<Feature> get();

}
