package io.cucumber.core.runner;

import io.cucumber.core.backend.ParameterTypeDefinition;
import io.cucumber.core.exception.CucumberException;
import org.jspecify.annotations.Nullable;

import static java.util.Objects.requireNonNull;

final class DuplicateParameterTypeDefinitionException extends CucumberException {

    DuplicateParameterTypeDefinitionException(
            String typeName,
            @Nullable ParameterTypeDefinition existing,
            ParameterTypeDefinition duplicate,
            Throwable cause
    ) {
        super(createMessage(typeName, existing, duplicate), cause);
    }

    private static String createMessage(
            String typeName,
            @Nullable ParameterTypeDefinition existing,
            ParameterTypeDefinition duplicate
    ) {
        requireNonNull(typeName);
        requireNonNull(duplicate);

        if (existing == null) {
            return "There is already a parameter type with name %s. The duplicate is defined in %s".formatted(
                typeName,
                duplicate.getLocation());
        }
        return "Duplicate parameter type with name %s defined in %s and %s".formatted(
            typeName,
            existing.getLocation(),
            duplicate.getLocation());
    }

}
