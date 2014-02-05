package cucumber.api.testng;

import cucumber.runtime.model.CucumberFeature;

/**
 * The only purpose of this class is to provide custom {@linkplain #toString()},
 * making TestNG reports look more descriptive.
 *
 * @see AbstractTestNGCucumberTests#feature(cucumber.api.testng.CucumberFeatureWrapper)
 */
class CucumberFeatureWrapper {
    private final CucumberFeature cucumberFeature;

    public CucumberFeatureWrapper(CucumberFeature cucumberFeature) {
        this.cucumberFeature = cucumberFeature;
    }

    public CucumberFeature getCucumberFeature() {
        return cucumberFeature;
    }

    @Override
    public String toString() {
        return cucumberFeature.getGherkinFeature().getName();
    }
}
