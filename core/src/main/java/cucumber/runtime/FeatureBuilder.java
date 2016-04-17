package cucumber.runtime;

import cucumber.runtime.model.CucumberFeature;
import gherkin.formatter.model.Feature;

import java.io.File;
import java.util.List;

public class FeatureBuilder extends SingleFeatureBuilder {
    private final List<CucumberFeature> cucumberFeatures;

    public FeatureBuilder(List<CucumberFeature> cucumberFeatures) {
        this(cucumberFeatures, File.separatorChar);
    }

    FeatureBuilder(List<CucumberFeature> cucumberFeatures, char fileSeparatorChar) {
        super(fileSeparatorChar);
        this.cucumberFeatures = cucumberFeatures;
    }

    @Override
    public void feature(Feature feature) {
        super.feature(feature);
        cucumberFeatures.add(super.currentCucumberFeature);
    }
}
