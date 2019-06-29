package io.cucumber.core.backend;

import org.apiguardian.api.API;

/**
 * Minimal facade for Dependency Injection containers
 */
@API(status = API.Status.STABLE)
public interface ObjectFactory {

    /**
     * Instantiate glue code <b>before</b> scenario execution. Called once per
     * scenario.
     */
    void start();

    /**
     * Dispose glue code <b>after</b> scenario execution. Called once per
     * scenario.
     */
    void stop();

    /**
     * Collects glue classes in the classpath. Called once on init.
     *
     * @param glueClass Glue class containing cucumber.api annotations (Before,
     * Given, When, ...)
     * @return true if stepdefs and hooks in this class should be used, false if
     * they should be ignored.
     */
    boolean addClass(Class<?> glueClass);

    /**
     * Provides the glue instances used to execute the current scenario. The
     * instance can be prepared in {@link #start()}.
     *
     * @param glueClass type of instance to be created.
     * @param <T> type of Glue class
     * @return new Glue instance of type T
     */
    <T> T getInstance(Class<T> glueClass);

}
