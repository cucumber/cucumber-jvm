package cucumber.runtime.java.guice;

import com.google.inject.Scope;

/**
 * A custom Guice scope that enables classes to be bound in a scope that will last for the lifetime of one Cucumber
 * scenario.
 */
public interface ScenarioScope extends Scope {
    void enterScope();
    void exitScope();
}
