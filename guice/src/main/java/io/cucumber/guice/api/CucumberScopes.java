package io.cucumber.guice.api;

/**
 * Provides a convenient <code>ScenarioScope</code> instance for use when declaring bindings
 * in implementations of <code>com.google.inject.Module</code>.
 */
public class CucumberScopes {
    public static final ScenarioScope SCENARIO = new SequentialScenarioScope();
}
