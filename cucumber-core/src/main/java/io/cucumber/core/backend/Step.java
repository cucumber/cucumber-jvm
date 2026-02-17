package io.cucumber.core.backend;

public interface Step {

    String getKeyword();

    String getText();

    int getLine();
}
