package io.cucumber.core.gherkin;

import org.jspecify.annotations.Nullable;

import java.util.List;

public interface Step extends io.cucumber.plugin.event.Step {

    StepType getType();

    String getPreviousGivenWhenThenKeyword();

    String getId();

    @Override
    @Nullable
    Argument getArgument();

    List<Argument> getArguments();
}
