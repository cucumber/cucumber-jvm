package cucumber.runtime.model;

import gherkin.formatter.Formatter;
import gherkin.formatter.model.Examples;
import gherkin.formatter.model.ExamplesTableRow;
import gherkin.formatter.model.Tag;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class CucumberExamples implements Serializable {
    private final CucumberScenarioOutline cucumberScenarioOutline;
    private final Examples examples;

    public CucumberExamples(CucumberScenarioOutline cucumberScenarioOutline, Examples examples) {
        this.cucumberScenarioOutline = cucumberScenarioOutline;
        this.examples = examples;
    }

    public List<CucumberScenario> createExampleScenarios() {
        List<CucumberScenario> exampleScenarios = new ArrayList<CucumberScenario>();

        List<ExamplesTableRow> rows = examples.getRows();
        List<Tag> tags = new ArrayList<Tag>(tagsAndInheritedTags());
        for (int i = 1; i < rows.size(); i++) {
            exampleScenarios.add(cucumberScenarioOutline.createExampleScenario(rows.get(0), rows.get(i), tags));
        }
        return exampleScenarios;
    }

    private Set<Tag> tagsAndInheritedTags() {
        Set<Tag> tags = new HashSet<Tag>();
        tags.addAll(cucumberScenarioOutline.tagsAndInheritedTags());
        tags.addAll(examples.getTags());
        return tags;
    }

    public Examples getExamples() {
        return examples;
    }

    public void format(Formatter formatter) {
        examples.replay(formatter);
    }
}
