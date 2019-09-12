package io.cucumber.core.backend;

import org.apiguardian.api.API;

import java.lang.reflect.Type;

@API(status = API.Status.STABLE)
public interface ParameterInfo {

    Type getType();

    boolean isTransposed();

    TypeResolver getTypeResolver();
}
