package io.cucumber.core.backend;

/**
 * Marks a glue class as being scenario scoped.
 * <p>
 * Instances of scenario scoped glue can not be used between scenarios and will
 * be removed from the glue. This is useful when the glue holds a reference to a
 * scenario scoped object (e.g. a method closure).
 */
public interface ScenarioScoped {

    /**
     * Disposes of the test execution context.
     * <p>
     * Scenario scoped step definition may be used in events. Thus retaining a
     * potential reference to the test execution context. When many tests are
     * used this may result in an over consumption of memory. Disposing of the
     * execution context resolves this problem.
     */
    default void dispose() {

    }

}
