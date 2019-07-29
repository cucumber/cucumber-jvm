package io.cucumber.core.backend;

import io.cucumber.cucumberexpressions.ParameterType;
import org.apiguardian.api.API;

@API(status = API.Status.EXPERIMENTAL)
public interface ParameterTypeDefinition {

    ParameterType<?> parameterType();

    /**
     * The source line where the parameter type is defined.
     * Example: com/example/app/Cucumber.test():42
     *
     * @param detail true if extra detailed location information should be included.
     * @return The source line of the step definition.
     */
    String getLocation(boolean detail);
}
