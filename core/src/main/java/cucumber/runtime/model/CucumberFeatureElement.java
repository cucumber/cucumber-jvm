package cucumber.runtime.model;

import cucumber.runtime.Backend;
import cucumber.runtime.Runtime;
import gherkin.formatter.Formatter;
import gherkin.formatter.Reporter;
import gherkin.formatter.model.Tag;
import gherkin.formatter.model.TagStatement;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public abstract class CucumberFeatureElement extends StepContainer {
    protected final TagStatement featureElement;

    public CucumberFeatureElement(CucumberFeature cucumberFeature, TagStatement featureElement) {
        super(cucumberFeature, featureElement);
        this.featureElement = featureElement;
    }

    protected Set<String> tags() {
        Set<String> tags = new HashSet<String>();
        for (Tag tag : cucumberFeature.getFeature().getTags()) {
            tags.add(tag.getName());
        }
        for (Tag tag : featureElement.getTags()) {
            tags.add(tag.getName());
        }
        return tags;
    }

    public String getKeywordAndName() {
        return featureElement.getKeyword() + ": " + featureElement.getName();
    }

    public abstract void run(Formatter formatter, Reporter reporter, Runtime runtime, List<Backend> backends);
}
