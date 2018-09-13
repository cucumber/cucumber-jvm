package io.cucumber.core.api;

import java.util.Locale;

public interface TypeRegistryConfigurer {

    Locale locale();

    void configureTypeRegistry(TypeRegistry typeRegistry);
}
