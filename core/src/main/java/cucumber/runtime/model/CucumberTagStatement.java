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
    private final CucumberBackground cucumberBackground;
    private final TagStatement gherkinModel;
    private final String visualName;

    CucumberTagStatement(CucumberBackground cucumberBackground, CucumberFeature cucumberFeature, TagStatement gherkinModel) {
        super(cucumberFeature, gherkinModel);
        this.cucumberBackground = cucumberBackground;
        this.gherkinModel = gherkinModel;
        this.visualName = gherkinModel.getKeyword() + ": " + gherkinModel.getName();
    }

    CucumberTagStatement(CucumberBackground cucumberBackground, CucumberFeature cucumberFeature, TagStatement gherkinModel, Row example) {
        super(cucumberFeature, gherkinModel);
        this.cucumberBackground = cucumberBackground;
        this.gherkinModel = gherkinModel;
        this.visualName = "| " + join(example.getCells(), " | ") + " |";
    }

    protected Set<Tag> tagsAndInheritedTags() {
        Set<Tag> tags = new HashSet<Tag>();
        tags.addAll(getCucumberFeature().getGherkinFeature().getTags());
        tags.addAll(gherkinModel.getTags());
        return tags;
    }

    public CucumberBackground getCucumberBackground() {
        return cucumberBackground;
    }

    public String getVisualName() {
        return visualName;
    }

    public TagStatement getGherkinModel() {
        return gherkinModel;
    }

    public abstract void run(Formatter formatter, Reporter reporter, Runtime runtime);
}
