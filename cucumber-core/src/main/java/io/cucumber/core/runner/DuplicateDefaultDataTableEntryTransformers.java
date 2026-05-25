package io.cucumber.core.runner;

import io.cucumber.core.exception.CucumberException;

import java.util.List;

import static java.util.Collections.unmodifiableList;
import static java.util.Objects.requireNonNull;
import static java.util.stream.Collectors.joining;

public final class DuplicateDefaultDataTableEntryTransformers extends CucumberException {

    private final List<CoreDefaultDataTableEntryTransformerDefinition> definitions;

    public DuplicateDefaultDataTableEntryTransformers(
            List<CoreDefaultDataTableEntryTransformerDefinition> definitions
    ) {
        super(createMessage(definitions));
        this.definitions = unmodifiableList(requireNonNull(definitions));
    }

    public List<CoreDefaultDataTableEntryTransformerDefinition> getDefinitions() {
        return definitions;
    }

    private static String createMessage(List<CoreDefaultDataTableEntryTransformerDefinition> definitions) {
        return "There may not be more then one default data table entry. Found:" + definitions.stream()
                .map(CoreDefaultDataTableEntryTransformerDefinition::getLocation)
                .collect(joining("\n - ", "\n - ", "\n"));
    }

}
