package io.cucumber.core.backend;

/**
 * Marks a glue class as being scenario scoped.
 * <p>
 * Instances of scenario scoped glue can not be used between scenarios and will
 * be removed from the glue. This is useful when the glue holds a reference to
 * a scenario scoped object (e.g. a method closure).
 */
public interface ScenarioScoped {

}
