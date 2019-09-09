package io.cucumber.core.backend;

import io.cucumber.cucumberexpressions.ParameterByTypeTransformer;
import org.apiguardian.api.API;

@API(status = API.Status.STABLE)
public interface DefaultParameterTransformerDefinition {

    ParameterByTypeTransformer parameterByTypeTransformer();

    /**
     * The source line where the default parameter transformer is defined.
     * Example: com/example/app/Cucumber.test():42
     *
     * @param detail true if extra detailed location information should be included.
     * @return The source line of the step definition.
     */
    String getLocation(boolean detail);
}
