package io.cucumber.testng;

import io.cucumber.core.feature.CucumberFeature;

final class FeatureWrapperImpl implements FeatureWrapper {
    private final CucumberFeature cucumberFeature;

    FeatureWrapperImpl(CucumberFeature cucumberFeature) {
        this.cucumberFeature = cucumberFeature;
    }

    @Override
    public String toString() {
        return "\"" + cucumberFeature.getName() + "\"";
    }
}
