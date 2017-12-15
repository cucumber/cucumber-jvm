package cucumber.runtime;

import cucumber.api.Configuration;
import cucumber.api.TypeRegistry;

import java.util.Locale;

public class DefaultConfiguration implements Configuration {
    @Override
    public TypeRegistry createTypeRegistry() {
        return new TypeRegistry(Locale.ENGLISH);
    }
}
