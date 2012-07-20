package cucumber.runtime.model;

import cucumber.runtime.Runtime;
import gherkin.formatter.Formatter;
import gherkin.formatter.Reporter;
import gherkin.formatter.model.Row;
import gherkin.formatter.model.Tag;
import gherkin.formatter.model.TagStatement;

import java.util.HashSet;
import java.util.Set;

import static gherkin.util.FixJava.join;

public abstract class CucumberTagStatement extends StepContainer {
    final TagStatement tagStatement;
    private final String visualName;

    CucumberTagStatement(CucumberFeature cucumberFeature, TagStatement tagStatement) {
        super(cucumberFeature, tagStatement);
        this.tagStatement = tagStatement;
        this.visualName = tagStatement.getKeyword() + ": " + tagStatement.getName();
    }

    CucumberTagStatement(CucumberFeature cucumberFeature, TagStatement tagStatement, Row example) {
        super(cucumberFeature, tagStatement);
        this.tagStatement = tagStatement;
        this.visualName = "| " + join(example.getCells(), " | ") + " |";
    }

    protected Set<Tag> tagsAndInheritedTags() {
        Set<Tag> tags = new HashSet<Tag>();
        tags.addAll(cucumberFeature.getFeature().getTags());
        tags.addAll(tagStatement.getTags());
        return tags;
    }

    public String getVisualName() {
        return visualName;
    }


    public abstract void run(Formatter formatter, Reporter reporter, Runtime runtime);
}
