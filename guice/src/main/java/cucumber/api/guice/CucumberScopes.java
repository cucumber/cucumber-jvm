package cucumber.api.guice;

import cucumber.runtime.java.guice.ScenarioScope;
import cucumber.runtime.java.guice.impl.SequentialScenarioScope;

/**
 * Provides a convenient <code>cucumber.runtime.java.guice.ScenarioScope</code> instance for use when declaring bindings
 * in implementations of <code>com.google.inject.Module</code>.
 */
public class CucumberScopes {
    /**
     * @deprecated please use {@link #createScenario()} instead
     */
    public static final ScenarioScope SCENARIO = new SequentialScenarioScope();

    public static ScenarioScope createScenario() {
        return new SequentialScenarioScope();
    }

}
