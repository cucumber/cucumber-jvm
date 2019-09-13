package io.cucumber.core.backend;

import org.apiguardian.api.API;

@API(status = API.Status.STABLE)
public interface HookDefinition {
    /**
     * The source line where the step definition is defined.
     * Example: com/example/app/Cucumber.test():42
     *
     * @return The source line where the step definition is defined.
     */
    String getLocation();

    void execute(Scenario scenario) throws Throwable;

    String getTagExpression();

    int getOrder();
}
