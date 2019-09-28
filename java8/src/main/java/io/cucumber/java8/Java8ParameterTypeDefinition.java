package io.cucumber.java8;

import java.util.Collections;

import io.cucumber.core.backend.ParameterTypeDefinition;
import io.cucumber.cucumberexpressions.ParameterType;

import static net.jodah.typetools.TypeResolver.resolveRawArguments;

class Java8ParameterTypeDefinition extends AbstractGlueDefinition implements ParameterTypeDefinition {

    private final ParameterType parameterType;

    @Override
    public ParameterType<?> parameterType() {
        return parameterType;
    }

    <T extends ParameterDefinitionBody> Java8ParameterTypeDefinition(String name, String regex, Class<T> bodyClass, T body) {
        super(body, new Exception().getStackTrace()[3]);
        Class<?> returnType = resolveRawArguments(bodyClass, body.getClass())[0];
        this.parameterType = new ParameterType(name, Collections.singletonList(regex), returnType, this::execute);
    }

    private Object execute(String[] parameterContent) throws Throwable {
        return Invoker.invoke(this, body, method, parameterContent);
    }
}
