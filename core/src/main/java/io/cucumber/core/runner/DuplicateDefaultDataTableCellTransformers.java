package io.cucumber.core.runner;

import io.cucumber.core.backend.DefaultDataTableCellTransformerDefinition;
import io.cucumber.core.backend.Located;
import io.cucumber.core.exception.CucumberException;

import java.util.List;

import static java.util.stream.Collectors.joining;

class DuplicateDefaultDataTableCellTransformers extends CucumberException {

    DuplicateDefaultDataTableCellTransformers(List<DefaultDataTableCellTransformerDefinition> definitions) {
        super(createMessage(definitions));
    }

    private static String createMessage(List<DefaultDataTableCellTransformerDefinition> definitions) {
        return "There may not be more then one default table cell transformers. Found:" + definitions.stream()
                .map(Located::getLocation)
                .collect(joining("\n - ", "\n - ", "\n"));
    }

}
