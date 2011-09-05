package cucumber.runtime;

/**
 * After Hooks that declare a parameter of this type will receive an instance of this class.
 * This allows an After hook to inspect whether or not a Scenario failed.
 */
public interface ScenarioResult {
    /**
     * @return the <em>most severe</em> status of the Scenario's Steps. One of "passed", "undefined", "pending", "skipped", "failed"
     */
    String getStatus();

    /**
     * @return true if and only if {@link #getStatus()} returns "false"
     */
    boolean isFailed();
}
