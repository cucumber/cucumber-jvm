package io.cucumber.core.api;

import java.util.Locale;

/**
 * The type registry configurer allows to configure a new type registry and the locale.
 */
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
