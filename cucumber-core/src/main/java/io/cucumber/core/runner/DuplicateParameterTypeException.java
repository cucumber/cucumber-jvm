package io.cucumber.core.runner;

import io.cucumber.core.backend.ParameterTypeDefinition;
import io.cucumber.core.exception.CucumberException;
import io.cucumber.cucumberexpressions.DuplicateTypeNameException;

import static java.util.Objects.requireNonNull;

final class DuplicateParameterTypeException extends CucumberException {

    DuplicateParameterTypeException(ParameterTypeDefinition a, ParameterTypeDefinition b) {
        super(createMessage(a, b));
    }

    DuplicateParameterTypeException(ParameterTypeDefinition definition, DuplicateTypeNameException cause) {
        super(createBuiltInConflictMessage(definition), cause);
    }

    private static String createMessage(ParameterTypeDefinition a, ParameterTypeDefinition b) {
        requireNonNull(a);
        requireNonNull(b);

        String name = b.parameterType().getName();
        return "Duplicate parameter type '%s' in %s and %s".formatted(
            name,
            a.getLocation(),
            b.getLocation());
    }

    private static String createBuiltInConflictMessage(ParameterTypeDefinition definition) {
        requireNonNull(definition);

        String name = definition.parameterType().getName();
        return "Duplicate parameter type '%s' in %s (conflicts with a built-in parameter type)"
                .formatted(name, definition.getLocation());
    }

}
