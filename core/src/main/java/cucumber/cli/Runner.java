package cucumber.cli;

import cucumber.resources.Resource;
import cucumber.resources.Resources;
import cucumber.resources.Consumer;
import cucumber.runtime.FeatureBuilder;
import cucumber.runtime.Runtime;
import cucumber.runtime.model.CucumberFeature;
import gherkin.formatter.Formatter;
import gherkin.formatter.Reporter;

import java.util.ArrayList;
import java.util.List;

public class Runner {
    private final Runtime runtime;

    public Runner(Runtime runtime) {
        this.runtime = runtime;
    }

    public void run(List<String> filesOrDirs, final List<Object> filters, Formatter formatter, Reporter reporter) {
        for (CucumberFeature cucumberFeature : load(filesOrDirs, filters)) {
            cucumberFeature.run(runtime, formatter, reporter);
        }
    }

    private List<CucumberFeature> load(List<String> filesOrDirs, final List<Object> filters) {
        final List<CucumberFeature> cucumberFeatures = new ArrayList<CucumberFeature>();
        final FeatureBuilder builder = new FeatureBuilder(cucumberFeatures);
        for (String fileOrDir : filesOrDirs) {
            Resources.scan(fileOrDir, ".feature", new Consumer() {
                @Override
                public void consume(Resource resource) {
                    builder.parse(resource, filters);
                }
            });
        }
        return cucumberFeatures;
    }
}
