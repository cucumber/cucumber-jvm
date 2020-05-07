package io.cucumber.java.annotation;

import io.cucumber.core.api.TypeRegistry;
import io.cucumber.core.api.TypeRegistryConfigurer;
import io.cucumber.cucumberexpressions.CaptureGroupTransformer;
import io.cucumber.cucumberexpressions.ParameterType;

import java.time.LocalDate;

public class TypeRegistryConfiguration implements TypeRegistryConfigurer {

    private final CaptureGroupTransformer<LocalDate> localDateParameterType = (String[] args) -> LocalDate.of(
        Integer.parseInt(args[0]),
        Integer.parseInt(args[1]),
        Integer.parseInt(args[2]));

    @Override
    public void configureTypeRegistry(TypeRegistry typeRegistry) {
        typeRegistry.defineParameterType(new ParameterType<>(
            "parameterTypeRegistryIso8601Date",
            "([0-9]{4})/([0-9]{2})/([0-9]{2})",
            LocalDate.class,
            localDateParameterType));
    }

}
