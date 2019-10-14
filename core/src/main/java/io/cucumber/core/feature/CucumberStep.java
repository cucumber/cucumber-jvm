package io.cucumber.core.feature;

public interface CucumberStep extends io.cucumber.plugin.event.CucumberStep {
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
}
