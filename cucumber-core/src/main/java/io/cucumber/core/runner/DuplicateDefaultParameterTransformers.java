package io.cucumber.core.runner;

import io.cucumber.core.backend.DefaultParameterTransformerDefinition;
import io.cucumber.core.exception.CucumberException;

import java.util.List;

import static java.util.Collections.unmodifiableList;
import static java.util.Objects.requireNonNull;
import static java.util.stream.Collectors.joining;

public final class DuplicateDefaultParameterTransformers extends CucumberException {

    private final List<DefaultParameterTransformerDefinition> definitions;

    public DuplicateDefaultParameterTransformers(List<DefaultParameterTransformerDefinition> definitions) {
        super(createMessage(definitions));
        this.definitions = unmodifiableList(requireNonNull(definitions));
    }

    public List<DefaultParameterTransformerDefinition> getDefinitions() {
        return definitions;
    }

    private static String createMessage(List<DefaultParameterTransformerDefinition> definitions) {
        return "There may not be more then one default parameter transformer. Found:" + definitions.stream()
                .map(d -> d.getLocation())
                .collect(joining("\n - ", "\n - ", "\n"));
    }

}
