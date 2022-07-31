package io.cucumber.core.runner;

import io.cucumber.core.backend.DefaultParameterTransformerDefinition;
import io.cucumber.core.exception.CucumberException;

import java.util.List;

import static java.util.stream.Collectors.joining;

class DuplicateDefaultParameterTransformers extends CucumberException {

    DuplicateDefaultParameterTransformers(List<DefaultParameterTransformerDefinition> definitions) {
        super(createMessage(definitions));
    }

    private static String createMessage(List<DefaultParameterTransformerDefinition> definitions) {
        return "There may not be more then one default parameter transformer. Found:" + definitions.stream()
                .map(d -> d.getLocation())
                .collect(joining("\n - ", "\n - ", "\n"));
    }

}
