package io.cucumber.core.runner;

public interface ScenarioScoped {
    /**
     * Dispose references to Runtime world to allow garbage collection to run.
     */
    void disposeScenarioScope();
}
