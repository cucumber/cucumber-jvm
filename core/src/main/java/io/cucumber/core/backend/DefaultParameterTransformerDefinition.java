package io.cucumber.core.backend;

import io.cucumber.cucumberexpressions.ParameterByTypeTransformer;
import org.apiguardian.api.API;

@API(status = API.Status.STABLE)
public interface DefaultParameterTransformerDefinition extends Located {

    ParameterByTypeTransformer parameterByTypeTransformer();

}
