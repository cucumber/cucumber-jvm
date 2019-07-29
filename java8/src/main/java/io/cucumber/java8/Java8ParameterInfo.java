package io.cucumber.java8;

import io.cucumber.core.backend.ParameterInfo;
import io.cucumber.core.backend.TypeResolver;

import java.lang.reflect.Type;

final class Java8ParameterInfo implements ParameterInfo {
    private final Type type;
    private final TypeResolver typeResolver;

    Java8ParameterInfo(Type type, TypeResolver typeResolver) {
        this.type = type;
        this.typeResolver = typeResolver;
    }

    public Type getType() {
        return type;
    }

    @Override
    public boolean isTransposed() {
        return false;
    }

    @Override
    public TypeResolver getTypeResolver() {
        return typeResolver;
    }

    @Override
    public String toString() {
        return type.toString();
    }
}
