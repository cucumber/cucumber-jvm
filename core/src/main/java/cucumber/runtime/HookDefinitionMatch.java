package cucumber.runtime;

import cucumber.api.Scenario;

import java.util.Collections;
import java.util.List;

public class HookDefinitionMatch implements DefinitionMatch {
    private final HookDefinition hookDefinition;

    public HookDefinitionMatch(HookDefinition hookDefinition) {
        this.hookDefinition = hookDefinition;
    }

    @Override
    public Object runStep(String language, Scenario scenario) throws Throwable {
        return hookDefinition.execute(scenario);
    }

    @Override
    public Object dryRunStep(String language, Scenario scenario) throws Throwable {
        // Do nothing
    	return null;
    }

    @Override
    public Match getMatch() {
        return new Match(Collections.<Argument>emptyList(), hookDefinition.getLocation(false));
    }

    @Override
    public String getPattern() {
        throw new UnsupportedOperationException();
    }

    @Override
    public String getCodeLocation() {
        return hookDefinition.getLocation(false);
    }

    @Override
    public List<Argument> getArguments() {
        return Collections.<Argument>emptyList();
    }

    @Override
    public List<String> getSnippets() {
        throw new UnsupportedOperationException();
    }
}
