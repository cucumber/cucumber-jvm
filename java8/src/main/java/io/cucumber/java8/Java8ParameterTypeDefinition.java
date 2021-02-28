package io.cucumber.java8;

import io.cucumber.core.backend.ParameterTypeDefinition;
import io.cucumber.cucumberexpressions.CaptureGroupTransformer;
import io.cucumber.cucumberexpressions.ParameterType;

import java.util.Collections;

class Java8ParameterTypeDefinition extends AbstractGlueDefinition implements ParameterTypeDefinition {

    private final ParameterType<?> parameterType;

    @SuppressWarnings({ "unchecked", "rawtypes" })
    <T extends ParameterDefinitionBody> Java8ParameterTypeDefinition(
            String name, String regex, Class<T> bodyClass, T body
    ) {
        super(body, new Exception().getStackTrace()[3]);
        Class<?> returnType = resolveRawArguments(bodyClass, body.getClass())[0];
        this.parameterType = new ParameterType(name, Collections.singletonList(regex), returnType,
            (CaptureGroupTransformer) this::invokeMethod);
    }

    @Override
    public ParameterType<?> parameterType() {
        return parameterType;
    }

}
