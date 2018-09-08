package io.cucumber.core.runner;

import cucumber.api.SnippetType;

import java.util.List;

public interface Options {
    List<String> getGlue();

    boolean isDryRun();

    SnippetType getSnippetType();
}
