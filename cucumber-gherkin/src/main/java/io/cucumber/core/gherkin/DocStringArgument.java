package io.cucumber.core.gherkin;

import org.jspecify.annotations.Nullable;

public interface DocStringArgument extends Argument, io.cucumber.plugin.event.DocStringArgument {

    @Override
    String getContent();

    @Override
    @Deprecated
    @Nullable
    String getContentType();

    @Override
    @Nullable
    String getMediaType();

    @Override
    int getLine();

}
