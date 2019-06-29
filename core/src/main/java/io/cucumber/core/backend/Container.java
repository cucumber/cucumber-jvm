package io.cucumber.core.backend;

import org.apiguardian.api.API;

@API(status = API.Status.STABLE)
public interface Container {
    /**
     * Collects glue classes in the classpath. Called once on init.
     *
     * @param glueClass Glue class containing cucumber.api annotations (Before, Given, When, ...)
     * @return true if stepdefs and hooks in this class should be used, false if they should be ignored.
     */
    boolean addClass(Class<?> glueClass);
}
