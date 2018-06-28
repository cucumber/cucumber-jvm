package cucumber.runtime;

import cucumber.runtime.model.CucumberFeature;

import java.util.List;

public interface FeatureSupplier {
    List<CucumberFeature> get();
}
