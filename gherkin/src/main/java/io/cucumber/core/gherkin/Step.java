package io.cucumber.core.gherkin;

public interface Step extends io.cucumber.plugin.event.Step {

    StepType getType();

    String getPreviousGivenWhenThenKeyword();

    @Override
    String getText();

    String getId();

    @Override
    Argument getArgument();
}
