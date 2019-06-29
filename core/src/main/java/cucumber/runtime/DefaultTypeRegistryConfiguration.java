package cucumber.runtime;

import cucumber.api.TypeRegistryConfigurer;
import cucumber.api.TypeRegistry;

import java.util.Locale;

public class DefaultTypeRegistryConfiguration implements TypeRegistryConfigurer, io.cucumber.core.api.TypeRegistryConfigurer {

    @Override
    public Locale locale() {
        return Locale.ENGLISH;
    }

    @Override
    public void configureTypeRegistry(io.cucumber.core.api.TypeRegistry typeRegistry) {
        //noop
    }

    @Override
    public void configureTypeRegistry(TypeRegistry typeRegistry) {
        //noop
    }

}
