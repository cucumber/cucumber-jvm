package io.cucumber.core.backend;

import java.lang.reflect.Type;

public interface ParameterInfo {

    Type getType();

    boolean isTransposed();

    TypeResolver getTypeResolver();
}
