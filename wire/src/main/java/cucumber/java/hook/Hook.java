package cucumber.java.hook;

import cucumber.api.Scenario;
import cucumber.runtime.HookDefinition;
import gherkin.formatter.model.Tag;

import java.util.Collection;
import java.util.HashSet;

public abstract class Hook {
    private HookDefinition hookDefinition;

    public Hook(HookDefinition hookDefinition) {
        this.hookDefinition = hookDefinition;
    }

    public void invokeHook(Scenario scenario) throws Throwable {
        if (tagsMatch(scenario)) {
            hookDefinition.execute(scenario);
        } else {
            skipHook();
        }
    }

    void skipHook() throws Throwable {
    }

    protected boolean tagsMatch(Scenario scenario) {
        if (scenario == null) return true;
        Collection<String> tagNames = scenario.getSourceTagNames();
        if (tagNames == null) return true;

        Collection<Tag> tags = new HashSet<Tag>();
        for (String tagName : tagNames) {
            tags.add(new Tag(tagName, null));
        }

        return hookDefinition.matches(tags);
    }
}
