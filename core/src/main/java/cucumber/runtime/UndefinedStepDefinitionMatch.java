package cucumber.runtime;

import cucumber.api.Scenario;
import gherkin.pickles.PickleStep;

import java.util.Collections;
import java.util.List;

public class UndefinedStepDefinitionMatch extends StepDefinitionMatch {
    private final List<String> snippets;

    public UndefinedStepDefinitionMatch(PickleStep step, List<String> snippets) {
        super(Collections.<Argument>emptyList(), new NoStepDefinition(), null, step, null);
        this.snippets = snippets;
    }

    @Override
    public void runStep(String language, Scenario scenario) throws Throwable {
        throw new UndefinedStepDefinitionException();
    }

    @Override
    public void dryRunStep(String language, Scenario scenario) throws Throwable {
        runStep(language, scenario);
    }

    @Override
    public Match getMatch() {
        return Match.UNDEFINED;
    }

    @Override
    public List<String> getSnippets() {
        return snippets;
    }
}
