package cucumber.runtime;

import java.util.Collection;

public interface HookDefinition {
    void execute() throws Throwable;

    boolean matches(Collection<String> tags);

    int getOrder();
}
