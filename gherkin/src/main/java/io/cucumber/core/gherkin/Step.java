package io.cucumber.core.gherkin;

public interface Step extends io.cucumber.plugin.event.Step {
    @Override
    int getLine();

    @Override
    Argument getArgument();

    @Override
    String getKeyWord();

    StepType getType();

    String getPreviousGivenWhenThenKeyWord();

    @Override
    String getText();

    String getId();
}
