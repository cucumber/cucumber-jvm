package cucumber.runtime;

import cucumber.io.Resource;
import cucumber.runtime.model.CucumberFeature;
import gherkin.I18n;
import gherkin.formatter.FilterFormatter;
import gherkin.formatter.Formatter;
import gherkin.formatter.model.Background;
import gherkin.formatter.model.Examples;
import gherkin.formatter.model.Feature;
import gherkin.formatter.model.Scenario;
import gherkin.formatter.model.ScenarioOutline;
import gherkin.formatter.model.Step;
import gherkin.parser.Parser;
import gherkin.util.FixJava;

import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;

public class FeatureBuilder implements Formatter {
    private final List<CucumberFeature> cucumberFeatures;
    private CucumberFeature currentCucumberFeature;
    private String uri;

    public FeatureBuilder(List<CucumberFeature> cucumberFeatures) {
        this.cucumberFeatures = cucumberFeatures;
    }

    @Override
    public void uri(String uri) {
        this.uri = uri;
    }

    @Override
    public void feature(Feature feature) {
        currentCucumberFeature = new CucumberFeature(feature, uri);
        cucumberFeatures.add(currentCucumberFeature);
    }

    @Override
    public void background(Background background) {
        currentCucumberFeature.background(background);
    }

    @Override
    public void scenario(Scenario scenario) {
        currentCucumberFeature.scenario(scenario);
    }

    @Override
    public void scenarioOutline(ScenarioOutline scenarioOutline) {
        currentCucumberFeature.scenarioOutline(scenarioOutline);
    }

    @Override
    public void examples(Examples examples) {
        currentCucumberFeature.examples(examples);
    }

    @Override
    public void step(Step step) {
        currentCucumberFeature.step(step);
    }

    @Override
    public void eof() {
    }

    @Override
    public void syntaxError(String state, String event, List<String> legalEvents, String uri, int line) {
    }

    @Override
    public void done() {
    }

    @Override
    public void close() {
    }

    public void parse(Resource resource, List<Object> filters) {
        Formatter formatter = this;
        if (!filters.isEmpty()) {
            formatter = new FilterFormatter(this, filters);
        }
        Parser parser = new Parser(formatter);
        String gherkin = read(resource);
        parser.parse(gherkin, resource.getPath(), 0);
        I18n i18n = parser.getI18nLanguage();
        if (currentCucumberFeature != null) {
            // The current feature may be null if we used a very restrictive filter, say a tag that isn't used.
            // Might also happen if the feature file itself is empty.
            currentCucumberFeature.setLocale(i18n.getLocale());
        }
    }

    private String read(Resource resource) {
        try {
            return FixJava.readReader(new InputStreamReader(resource.getInputStream(), "UTF-8"));
        } catch (IOException e) {
            throw new CucumberException("Failed to read resource:" + resource.getPath(), e);
        }
    }
}
