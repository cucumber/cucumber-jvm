package cucumber.api.guice;

import com.google.inject.Module;
import cucumber.runtime.java.guice.ScenarioScope;
import cucumber.runtime.java.guice.ScenarioScoped;
import cucumber.runtime.java.guice.impl.SequentialScenarioScope;

/**
 * Creates an instance of {@link ScenarioScope} for use when declaring bindings
 * in implementations of {@link Module}.
 * <p>
 * Note that when binding objects to the scenario scope it is recommended to bind
 * them to the {@link ScenarioScoped} annotation instead. E.g:
 *
 * <code>bind(ScenarioScopedObject.class).in(ScenarioScoped.class);</code>
 */
public class CucumberScopes {
    /**
     * A convenient instance of {@link ScenarioScope}. Should only be used
     * in combination with {@link CucumberModules#SCENARIO}.
     * <p>
     * Note that using this in combination with parallel execution results in
     * undefined behaviour.
     *
     * @deprecated please use {@link #createScenarioScope()} instead
     */
    @Deprecated
    public static final ScenarioScope SCENARIO = createScenarioScope();

    /**
     * Creates a new instance of a ScenarioScope.
     *
     * @return a new instance of a ScenarioScope.
     */
    public static ScenarioScope createScenarioScope() {
        return new SequentialScenarioScope();
    }

}
