package io.cucumber.java8;

import io.cucumber.core.backend.ParameterInfo;
import io.cucumber.core.backend.TypeResolver;

import java.lang.reflect.Type;

final class Java8ParameterInfo implements ParameterInfo {

    private final LambdaTypeResolver typeResolver;

    Java8ParameterInfo(LambdaTypeResolver typeResolver) {
        this.typeResolver = typeResolver;
    }

    public Type getType() {
        return typeResolver.getType();
    }

    @Override
    public boolean isTransposed() {
        return false;
    }

    @Override
    public TypeResolver getTypeResolver() {
        return typeResolver;
    }

}
