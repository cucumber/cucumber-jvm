package cucumber.runtime;

import gherkin.formatter.model.Tag;

import java.util.Collection;

public interface HookDefinition {
    void execute(ScenarioResult scenarioResult) throws Throwable;

    boolean matches(Collection<Tag> tags);

    int getOrder();
}
