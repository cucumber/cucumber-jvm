package cucumber.api.guice;

import io.cucumber.guice.ScenarioScope;
import io.cucumber.guice.impl.SequentialScenarioScope;

/**
 * Provides a convenient <code>ScenarioScope</code> instance for use when declaring bindings
 * in implementations of <code>com.google.inject.Module</code>.
 */
public class CucumberScopes {
    public static final ScenarioScope SCENARIO = new SequentialScenarioScope();
}
