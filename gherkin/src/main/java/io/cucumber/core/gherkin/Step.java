package io.cucumber.core.gherkin;

public interface Step extends io.cucumber.plugin.event.Step {

    StepType getType();

    String getPreviousGivenWhenThenKeyword();

    String getId();

    @Override
    Argument getArgument();

}
