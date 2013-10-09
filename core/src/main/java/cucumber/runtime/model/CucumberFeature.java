package cucumber.runtime.model;

import cucumber.runtime.FeatureBuilder;
import cucumber.runtime.Runtime;
import cucumber.runtime.io.Resource;
import cucumber.runtime.io.ResourceLoader;
import gherkin.I18n;
import gherkin.formatter.Formatter;
import gherkin.formatter.Reporter;
import gherkin.formatter.model.Background;
import gherkin.formatter.model.Examples;
import gherkin.formatter.model.Feature;
import gherkin.formatter.model.Scenario;
import gherkin.formatter.model.ScenarioOutline;
import gherkin.formatter.model.Step;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class CucumberFeature {
    private final String path;
    private final Feature feature;
    private CucumberBackground cucumberBackground;
    private StepContainer currentStepContainer;
    private final List<CucumberTagStatement> cucumberTagStatements = new ArrayList<CucumberTagStatement>();
    private I18n i18n;
    private CucumberScenarioOutline currentScenarioOutline;

    public static List<CucumberFeature> load(ResourceLoader resourceLoader, List<String> featurePaths, final List<Object> filters, PrintStream out) {
        final List<CucumberFeature> cucumberFeatures = load(resourceLoader, featurePaths, filters);
        if (cucumberFeatures.isEmpty()) {
            if (featurePaths.isEmpty()) {
                out.println(String.format("Got no path to feature directory or feature file"));
            } else if (filters.isEmpty()) {
                out.println(String.format("No features found at %s", featurePaths));
            } else {
                out.println(String.format("None of the features at %s matched the filters: %s", featurePaths, filters));
            }
        }
        return cucumberFeatures;
    }

    public static List<CucumberFeature> load(ResourceLoader resourceLoader, List<String> featurePaths, final List<Object> filters) {
        final List<CucumberFeature> cucumberFeatures = new ArrayList<CucumberFeature>();
        final FeatureBuilder builder = new FeatureBuilder(cucumberFeatures);
        for (String featurePath : featurePaths) {
            Iterable<Resource> resources = resourceLoader.resources(featurePath, ".feature");
            for (Resource resource : resources) {
                builder.parse(resource, filters);
            }
        }
        Collections.sort(cucumberFeatures, new CucumberFeatureUriComparator());
        return cucumberFeatures;
    }

    public CucumberFeature(Feature feature, String path) {
        this.feature = feature;
        this.path = path;
    }

    public void background(Background background) {
        cucumberBackground = new CucumberBackground(this, background);
        currentStepContainer = cucumberBackground;
    }

    public void scenario(Scenario scenario) {
        CucumberTagStatement cucumberTagStatement = new CucumberScenario(this, cucumberBackground, scenario);
        currentStepContainer = cucumberTagStatement;
        cucumberTagStatements.add(cucumberTagStatement);
    }

    public void scenarioOutline(ScenarioOutline scenarioOutline) {
        CucumberScenarioOutline cucumberScenarioOutline = new CucumberScenarioOutline(this, cucumberBackground, scenarioOutline);
        currentScenarioOutline = cucumberScenarioOutline;
        currentStepContainer = cucumberScenarioOutline;
        cucumberTagStatements.add(cucumberScenarioOutline);
    }

    public void examples(Examples examples) {
        currentScenarioOutline.examples(examples);
    }

    public void step(Step step) {
        currentStepContainer.step(step);
    }

    public Feature getGherkinFeature() {
        return feature;
    }

    public List<CucumberTagStatement> getFeatureElements() {
        return cucumberTagStatements;
    }

    public void setI18n(I18n i18n) {
        this.i18n = i18n;
    }

    public I18n getI18n() {
        return i18n;
    }

    public String getPath() {
        return path;
    }

    public void run(Formatter formatter, Reporter reporter, Runtime runtime) {
        formatter.uri(getPath());
        formatter.feature(getGherkinFeature());

        for (CucumberTagStatement cucumberTagStatement : getFeatureElements()) {
            //Run the scenario, it should handle before and after hooks
            cucumberTagStatement.run(formatter, reporter, runtime);
        }
        formatter.eof();

    }

    private static class CucumberFeatureUriComparator implements Comparator<CucumberFeature> {
        @Override
        public int compare(CucumberFeature a, CucumberFeature b) {
            return a.getPath().compareTo(b.getPath());
        }
    }
}
