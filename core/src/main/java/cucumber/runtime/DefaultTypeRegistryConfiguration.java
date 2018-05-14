package cucumber.runtime;

import cucumber.api.TypeRegistryConfigurer;
import cucumber.api.TypeRegistry;

import java.util.Locale;

public class DefaultTypeRegistryConfiguration implements TypeRegistryConfigurer {

    @Override
    public Locale locale() {
        return Locale.ENGLISH;
    }

    @Override
    public void configureTypeRegistry(TypeRegistry typeRegistry) {
        //noop
    }

}
