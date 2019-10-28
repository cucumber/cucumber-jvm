package io.cucumber.java8;

import io.cucumber.core.backend.DefaultParameterTransformerDefinition;
import io.cucumber.cucumberexpressions.ParameterByTypeTransformer;

import java.lang.reflect.Type;

class Java8DefaultParameterTypeDefinition extends AbstractGlueDefinition implements DefaultParameterTransformerDefinition {

    Java8DefaultParameterTypeDefinition(DefaultParameterTransformerBody body) {
        super(body, new Exception().getStackTrace()[3]);
    }

    @Override
    public ParameterByTypeTransformer parameterByTypeTransformer() {
        return this::execute;
    }

    private Object execute(String fromValue, Type toValue) {
        return Invoker.invoke(this, body, method, fromValue, toValue);
    }
}
