package io.cucumber.core.gherkin;

import org.jspecify.annotations.Nullable;

public interface Step extends io.cucumber.plugin.event.Step {

    StepType getType();

    String getPreviousGivenWhenThenKeyword();

    String getId();

    @Override
    @Nullable
    Argument getArgument();

}
