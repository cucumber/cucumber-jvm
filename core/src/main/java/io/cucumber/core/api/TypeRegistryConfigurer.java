package io.cucumber.core.api;

import org.apiguardian.api.API;

import java.util.Locale;

/**
 * The type registry configurer allows to configure a new type registry and the locale.
 */
@API(status = API.Status.STABLE)
public interface TypeRegistryConfigurer {
    /**
     * @return The locale to use.
     */
    Locale locale();

    /**
     * Configures the type registry.
     * @param typeRegistry The new type registry.
     */
    void configureTypeRegistry(TypeRegistry typeRegistry);
}
