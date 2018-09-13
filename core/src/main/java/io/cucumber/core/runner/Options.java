package io.cucumber.core.runner;

import io.cucumber.core.api.options.SnippetType;

import java.util.List;

public interface Options {
    List<String> getGlue();

    boolean isDryRun();

    SnippetType getSnippetType();
}
