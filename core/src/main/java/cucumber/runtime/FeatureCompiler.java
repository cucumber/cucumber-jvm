package cucumber.runtime;

import cucumber.messages.Pickles.Pickle;
import cucumber.runtime.model.CucumberFeature;
import gherkin.pickles.PickleCompiler;

import java.util.List;

public final class FeatureCompiler {
    private final PickleCompiler compiler = new PickleCompiler();

    public List<Pickle> compileFeature(CucumberFeature feature) {
        return compiler.compile(feature.getGherkinFeature(), feature.getUri());
    }
}
