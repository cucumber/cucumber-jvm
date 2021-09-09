package io.cucumber.testng;

import io.cucumber.core.gherkin.Feature;

final class FeatureWrapperImpl implements FeatureWrapper {

    private final Feature feature;

    FeatureWrapperImpl(Feature feature) {
        this.feature = feature;
    }

    @Override
    public String toString() {
        return "\"" + feature.getName() + "\"";
    }

}
