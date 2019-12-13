package io.cucumber.core.gherkin;

public interface Step extends io.cucumber.plugin.event.Step {

    int getLine();

    Argument getArgument();

    String getKeyWord();

    StepType getType();

    String getPreviousGivenWhenThenKeyWord();

    String getText();

    String getId();
}
