package cucumber.runtime;

import gherkin.formatter.model.Tag;

import java.util.Collection;

public interface StaticHookDefinition {
    void execute() throws Throwable;

    boolean matches(Collection<Tag> tags);

    int getOrder();
}
