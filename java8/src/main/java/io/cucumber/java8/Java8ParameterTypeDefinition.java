package io.cucumber.java8;

import io.cucumber.core.backend.ParameterTypeDefinition;
import io.cucumber.cucumberexpressions.ParameterType;
import java.util.Collections;
import net.jodah.typetools.TypeResolver;

class Java8ParameterTypeDefinition extends AbstractGlueDefinition implements ParameterTypeDefinition {

    private final ParameterType parameterType;

    @Override
    public ParameterType<?> parameterType() {
        return parameterType;
    }

    Java8ParameterTypeDefinition(Object body, String regex, String name) {
        super(body, new Exception().getStackTrace()[3]);
        Class returnType = TypeResolver.resolveRawArguments(ParameterDefinitionBody.class, body.getClass())[0];
        this.parameterType = new ParameterType(name, Collections.singletonList(regex), returnType, this::execute);
    }

    private Object execute(String parameterContent) throws Throwable {
        return Invoker.invoke(this, body, method, parameterContent);
    }
}
