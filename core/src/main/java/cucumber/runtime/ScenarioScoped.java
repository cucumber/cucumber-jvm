package cucumber.runtime;

public interface ScenarioScoped {
    /**
     * Dispose references to Runtime world to allow garbage collection to run.
     */
    void disposeScenarioScope();
}
