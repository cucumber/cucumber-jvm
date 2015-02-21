package cucumber.runtime;

import cucumber.api.Scenario;
import gherkin.formatter.model.Tag;

import java.util.Collection;

public interface HookDefinition extends Hook {
    void execute(Scenario scenario) throws Throwable;

    boolean matches(Collection<Tag> tags);
}