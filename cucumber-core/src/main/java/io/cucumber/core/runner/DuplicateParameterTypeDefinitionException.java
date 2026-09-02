package io.cucumber.core.runner;

import io.cucumber.core.backend.Located;
import io.cucumber.core.backend.ParameterTypeDefinition;
import io.cucumber.core.exception.CucumberException;

import java.util.List;

import static java.util.stream.Collectors.joining;

final class DuplicateParameterTypeDefinitionException extends CucumberException {

    DuplicateParameterTypeDefinitionException(String name, List<ParameterTypeDefinition> duplicates) {
        super(createMessage(name, duplicates));
    }

    private static String createMessage(String name, List<ParameterTypeDefinition> duplicates) {
        var locations = duplicates.stream().map(Located::getLocation).collect(joining(", "));
        return "Duplicate parameter types with name \"%s\" defined in %s"
                .formatted(name, locations);
    }

}
