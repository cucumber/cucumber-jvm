package io.cucumber.java8;

import io.cucumber.core.backend.ParameterTypeDefinition;
import io.cucumber.cucumberexpressions.ParameterType;

import java.util.Arrays;
import java.util.Collections;
import net.jodah.typetools.TypeResolver;
import net.jodah.typetools.TypeResolver.Unknown;

import static net.jodah.typetools.TypeResolver.resolveRawArguments;

class Java8ParameterTypeDefinition<R> extends AbstractGlueDefinition implements ParameterTypeDefinition {

    private final ParameterType<R> parameterType;

    @Override
    public ParameterType<?> parameterType() {
        return parameterType;
    }

    <T extends ParameterDefinitionBody<R>> Java8ParameterTypeDefinition(String name, String regex, T body) {
        super(body, new Exception().getStackTrace()[3]);
        Class<R> returnType = (Class<R>) resolveRawArguments(ParameterDefinitionBody.class, body.getClass())[0];
        this.parameterType = new ParameterType<R>(name, Collections.singletonList(regex), returnType, this::execute);
    }

    private R execute(String[] parameterContent) throws Throwable {
        return (R) Invoker.invoke(this, body, method, parameterContent);
    }
}
