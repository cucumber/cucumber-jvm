package cucumber.cli;

import cucumber.resources.Consumer;
import cucumber.resources.Resource;
import cucumber.resources.Resources;
import cucumber.runtime.FeatureBuilder;
import cucumber.runtime.Runtime;
import cucumber.runtime.model.CucumberFeature;
import cucumber.runtime.model.CucumberScenario;
import gherkin.formatter.Formatter;
import gherkin.formatter.Reporter;
import gherkin.formatter.model.Step;

import java.util.ArrayList;
import java.util.List;

public class Runner {
    private final Runtime runtime;

    public Runner(Runtime runtime) {
        this.runtime = runtime;
    }

    public void run(List<String> filesOrDirs, final List<Object> filters, Formatter formatter, Reporter reporter) {
        for (CucumberFeature cucumberFeature : load(filesOrDirs, filters)) {
            for (CucumberScenario cucumberScenario : cucumberFeature.getCucumberScenarios()) {
                // TODO: Maybe get extraPaths from scenario

                // TODO: split up prepareAndFormat o we can run Background in isolation.
                // Or maybe just try to make Background behave like a regular Scenario?? Printing wise at least.

                runtime.createWorld(new ArrayList<String>(), cucumberScenario.tags());

                cucumberScenario.format(formatter);
                for (Step step : cucumberScenario.getSteps()) {
                    runtime.runStep(cucumberScenario.getUri(), step, reporter, cucumberScenario.getLocale());
                }
                runtime.disposeWorld();
            }
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
