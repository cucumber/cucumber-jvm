package io.cucumber.guice.api;

import com.google.inject.Module;

/**
 * Provides a convenient <code>com.google.inject.Module</code> instance that contains bindings for
 * <code>ScenarioScoped</code> annotation and for
 * <code>ScenarioScope</code>.
 */
public class CucumberModules {
    public static final Module SCENARIO = new ScenarioModule(CucumberScopes.SCENARIO);
}
