package io.cucumber.core.backend;

/**
 * Minimal facade for Dependency Injection containers
 */
public interface ObjectFactory extends Container, Lookup {

    /**
     * Instantiate glue code <b>before</b> scenario execution. Called once per scenario.
     */
    void start();

    /**
     * Dispose glue code <b>after</b> scenario execution. Called once per scenario.
     */
    void stop();

}
