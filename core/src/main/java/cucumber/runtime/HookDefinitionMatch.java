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
    public void runStep(String language, Scenario scenario) throws Throwable {
        hookDefinition.execute(scenario);
    }

    @Override
    public void dryRunStep(String language, Scenario scenario) throws Throwable {
        // Do nothing
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
}
