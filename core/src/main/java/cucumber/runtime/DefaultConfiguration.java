package cucumber.runtime;

import cucumber.api.Configuration;
import cucumber.api.TypeRegistry;

import java.util.Locale;

public class DefaultConfiguration implements Configuration {

    @Override
    public Locale locale() {
        return Locale.ENGLISH;
    }

    @Override
    public void configureTypeRegistry(TypeRegistry typeRegistry) {
        //noop
    }

}
