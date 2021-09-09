package io.cucumber.core.gherkin;

public interface DocStringArgument extends Argument, io.cucumber.plugin.event.DocStringArgument {

    @Override
    String getContent();

    @Override
    String getContentType();

    @Override
    String getMediaType();

    @Override
    int getLine();

}
