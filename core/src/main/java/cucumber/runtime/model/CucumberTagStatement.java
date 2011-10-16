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

public abstract class CucumberTagStatement extends StepContainer {
    protected final TagStatement tagStatement;

    public CucumberTagStatement(CucumberFeature cucumberFeature, TagStatement tagStatement) {
        super(cucumberFeature, tagStatement);
        this.tagStatement = tagStatement;
    }

    protected Set<String> tags() {
        Set<String> tags = new HashSet<String>();
        for (Tag tag : cucumberFeature.getFeature().getTags()) {
            tags.add(tag.getName());
        }
        for (Tag tag : tagStatement.getTags()) {
            tags.add(tag.getName());
        }
        return tags;
    }

    public String getVisualName() {
        if (tagStatement.getName() != null) {
            return tagStatement.getKeyword() + ": " + tagStatement.getName();
        } else {
            // Example rows get "compiled" into a Scenario, using the row as the keyword and a null name
            return tagStatement.getKeyword();
        }
    }

    public abstract void run(Formatter formatter, Reporter reporter, Runtime runtime, List<Backend> backends);
}
