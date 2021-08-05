package io.cucumber.testng;

import org.apiguardian.api.API;

/**
 * Provides cucumber with properties from {@code testng.xml}.
 *
 * @see io.cucumber.core.options.Constants
 */
@API(status = API.Status.EXPERIMENTAL, since = "6.11")
@FunctionalInterface
public interface CucumberPropertiesProvider {

    /**
     * Returns a configuration property for the given key, or null if there is
     * no such property.
     *
     * @param  key the property name
     * @return     the property value or null
     */
    String get(String key);
}
