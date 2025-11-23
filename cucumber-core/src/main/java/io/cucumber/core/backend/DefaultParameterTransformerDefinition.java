package io.cucumber.core.backend;

import io.cucumber.cucumberexpressions.ParameterByTypeTransformer;
import org.apiguardian.api.API;

import java.util.Locale;

@API(status = API.Status.STABLE)
public interface DefaultParameterTransformerDefinition extends Located {

    ParameterByTypeTransformer parameterByTypeTransformer();

    default ParameterByTypeTransformer parameterByTypeTransformer(Locale locale) {
        return this.parameterByTypeTransformer();
    }

}
