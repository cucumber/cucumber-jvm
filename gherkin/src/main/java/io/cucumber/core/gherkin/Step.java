package io.cucumber.core.gherkin;

public interface Step extends io.cucumber.plugin.event.Step {
    @Override
    int getStepLine();

    @Override
    Argument getArgument();

    @Override
    String getKeyWord();

    StepType getStepType();

    String getPreviousGivenWhenThenKeyWord();

    @Override
    String getText();

    String getId();
}
