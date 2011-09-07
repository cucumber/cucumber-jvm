package cucumber.runtime;

import cucumber.resources.Resource;
import cucumber.runtime.model.CucumberFeature;
import gherkin.I18n;
import gherkin.formatter.FilterFormatter;
import gherkin.formatter.Formatter;
import gherkin.formatter.model.*;
import gherkin.parser.Parser;

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
    }

    @Override
    public void examples(Examples examples) {
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

    public void parse(Resource resource, List<Object> filters) {
        Formatter formatter = this;
        if (!filters.isEmpty()) {
            formatter = new FilterFormatter(this, filters);
        }
        Parser parser = new Parser(formatter);
        parser.parse(resource.getString(), resource.getPath(), 0);
        I18n i18n = parser.getI18nLanguage();
        if (currentCucumberFeature != null) {
            // The current feature may be null if we used a very restrictive filter, say a tag that isn't used.
            currentCucumberFeature.setLocale(i18n.getLocale());
        }
    }
}
