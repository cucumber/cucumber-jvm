package io.cucumber.java8;

import io.cucumber.core.backend.DefaultParameterTransformerDefinition;
import io.cucumber.cucumberexpressions.ParameterByTypeTransformer;

final class Java8DefaultParameterTransformerDefinition extends AbstractGlueDefinition
        implements DefaultParameterTransformerDefinition {

    Java8DefaultParameterTransformerDefinition(DefaultParameterTransformerBody body) {
        super(body, new Exception().getStackTrace()[3]);
    }

    @SuppressWarnings("Convert2MethodRef")
    @Override
    public ParameterByTypeTransformer parameterByTypeTransformer() {
        return (fromValue, toValueType) -> invokeMethod(fromValue, toValueType);
    }

}
