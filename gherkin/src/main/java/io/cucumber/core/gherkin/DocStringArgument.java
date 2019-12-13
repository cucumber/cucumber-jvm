package io.cucumber.core.gherkin;

public interface DocStringArgument extends Argument, io.cucumber.plugin.event.DocStringArgument {
    String getContent();

    String getContentType();

    int getLine();
}
