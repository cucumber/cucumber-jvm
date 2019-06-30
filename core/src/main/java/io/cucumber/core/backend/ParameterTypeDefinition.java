package io.cucumber.core.backend;

import io.cucumber.cucumberexpressions.ParameterType;
import org.apiguardian.api.API;

@API(status = API.Status.EXPERIMENTAL)
public interface ParameterTypeDefinition {

    ParameterType<?> parameterType();

}
