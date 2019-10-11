package io.cucumber.core.api;

import org.apiguardian.api.API;

import java.util.Locale;

/**
 * The type registry configurer allows to configure a new type registry and the locale.
 * @deprecated Please use annotation based configuration.
 * See {@link io.cucumber.examples.java.ShoppingSteps} and {@link io.cucumber.examples.java8.ShoppingSteps} for examples
 */
@API(status = API.Status.DEPRECATED)
@Deprecated
public interface TypeRegistryConfigurer {
    /**
     * @return The locale to use, or null when language from feature file should be used.
     */
    default Locale locale() {
        return null;
    }

    /**
     * Configures the type registry.
     *
     * @param typeRegistry The new type registry.
     */
    void configureTypeRegistry(TypeRegistry typeRegistry);
}
