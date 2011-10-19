package cucumber.runtime.model;

import cucumber.runtime.Backend;
import cucumber.runtime.Runtime;
import gherkin.formatter.Formatter;
import gherkin.formatter.Reporter;
import gherkin.formatter.model.Row;
import gherkin.formatter.model.Tag;
import gherkin.formatter.model.TagStatement;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static gherkin.util.FixJava.join;

public abstract class CucumberTagStatement extends StepContainer {
    protected final TagStatement tagStatement;
    private final String visualName;

    public CucumberTagStatement(CucumberFeature cucumberFeature, TagStatement tagStatement) {
        super(cucumberFeature, tagStatement);
        this.tagStatement = tagStatement;
        this.visualName = tagStatement.getKeyword() + ": " + tagStatement.getName();
    }

    public CucumberTagStatement(CucumberFeature cucumberFeature, TagStatement tagStatement, Row example) {
        super(cucumberFeature, tagStatement);
        this.tagStatement = tagStatement;
        this.visualName = "| " + join(example.getCells(), " | ") + " |";
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
        return visualName;
    }

    public abstract void run(Formatter formatter, Reporter reporter, Runtime runtime, List<Backend> backends, List<String> codePaths);
}
