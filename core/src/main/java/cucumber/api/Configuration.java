package cucumber.api;

import io.cucumber.cucumberexpressions.ParameterTypeRegistry;

public interface Configuration {
    ParameterTypeRegistry createParameterTypeRegistry();
}
