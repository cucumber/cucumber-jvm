package io.cucumber.core.runner;

import io.cucumber.core.api.options.SnippetType;

import java.net.URI;
import java.util.List;

public interface Options {
    List<URI> getGlue();

    boolean isDryRun();

    SnippetType getSnippetType();
}
