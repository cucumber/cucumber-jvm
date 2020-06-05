package io.cucumber.core.runner;

import io.cucumber.plugin.event.Argument;

import java.util.List;

public class TestDefinitionArgument {

    public static List<Argument> createArguments(List<io.cucumber.core.stepexpression.Argument> match) {
        return DefinitionArgument.createArguments(match);
    }

}
