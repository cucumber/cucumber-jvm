package cucumber.api;

import java.util.Locale;

/**
 * @deprecated use {@link io.cucumber.core.api.TypeRegistryConfigurer} instead.
 */
@Deprecated
public interface TypeRegistryConfigurer {

    Locale locale();

    void configureTypeRegistry(TypeRegistry typeRegistry);
}
