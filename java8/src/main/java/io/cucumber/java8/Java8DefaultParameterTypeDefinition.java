package io.cucumber.java8;

import io.cucumber.core.backend.DefaultParameterTransformerDefinition;
import io.cucumber.cucumberexpressions.ParameterByTypeTransformer;

class Java8DefaultParameterTypeDefinition extends AbstractGlueDefinition
        implements DefaultParameterTransformerDefinition {

    Java8DefaultParameterTypeDefinition(DefaultParameterTransformerBody body) {
        super(body, new Exception().getStackTrace()[3]);
    }

    @Override
    public ParameterByTypeTransformer parameterByTypeTransformer() {
        return (fromValue, toValue) -> invokeMethod(fromValue, toValue);
    }

}
