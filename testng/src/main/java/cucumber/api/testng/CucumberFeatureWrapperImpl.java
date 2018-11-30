package cucumber.api.testng;

import cucumber.runtime.model.CucumberFeature;

class CucumberFeatureWrapperImpl implements CucumberFeatureWrapper {
    private final CucumberFeature cucumberFeature;

    CucumberFeatureWrapperImpl(CucumberFeature cucumberFeature) {
        this.cucumberFeature = cucumberFeature;
    }

    @Override
    public String toString() {
        return "\"" + cucumberFeature.getGherkinFeature().getFeature().getName() + "\"";
    }
}
