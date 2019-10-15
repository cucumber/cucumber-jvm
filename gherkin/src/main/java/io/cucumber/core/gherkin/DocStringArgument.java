package io.cucumber.core.gherkin;

import io.cucumber.core.gherkin.Argument;

public interface DocStringArgument extends Argument, io.cucumber.plugin.event.DocStringArgument {
    @Override
    String getContent();

    @Override
    String getContentType();

    @Override
    int getLine();
}
