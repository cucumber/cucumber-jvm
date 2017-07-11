package cucumber.runtime;

import cucumber.api.Configuration;
import io.cucumber.cucumberexpressions.ParameterTypeRegistry;

import java.util.Locale;

public class DefaultConfiguration implements Configuration {
    @Override
    public ParameterTypeRegistry createParameterTypeRegistry() {
        return new ParameterTypeRegistry(Locale.ENGLISH);
    }
}
