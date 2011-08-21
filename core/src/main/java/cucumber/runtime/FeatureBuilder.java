package cucumber.runtime;

import cucumber.io.Resource;
import cucumber.runtime.model.CucumberFeature;
import gherkin.I18n;
import gherkin.formatter.Formatter;
import gherkin.formatter.model.*;
import gherkin.parser.Parser;

import java.util.List;

public class FeatureBuilder implements Formatter {
    private final Parser parser = new Parser(this);
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

    public void parse(Resource resource) {
        parser.parse(resource.getString(), resource.getPath(), 0);
        I18n i18n = parser.getI18nLanguage();
        currentCucumberFeature.setLocale(i18n.getLocale());
    }
}
