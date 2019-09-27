package io.cucumber.java8;

import io.cucumber.core.backend.ParameterTypeDefinition;
import io.cucumber.cucumberexpressions.ParameterType;
import java.util.Collections;
import net.jodah.typetools.TypeResolver;
import net.jodah.typetools.TypeResolver.Unknown;

import static net.jodah.typetools.TypeResolver.resolveRawArguments;

class Java8ParameterTypeDefinition extends AbstractGlueDefinition implements ParameterTypeDefinition {

    private final ParameterType parameterType;

    @Override
    public ParameterType<?> parameterType() {
        return parameterType;
    }

    <T extends ParameterDefinitionBody> Java8ParameterTypeDefinition(String name, String regex, Class<T> bodyClass, T body) {
        super(body, new Exception().getStackTrace()[3]);
        Class<?>[] typeArguments = resolveRawArguments(bodyClass, body.getClass());
        Class<?> returnType = typeArguments[0];
        this.parameterType = new ParameterType(name, Collections.singletonList(regex), returnType, this::execute);
    }

    private Object execute(String[] parameterContent) throws Throwable {
        return Invoker.invoke(this, body, method, parameterContent);
    }
}
