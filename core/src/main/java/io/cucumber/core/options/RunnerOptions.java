package io.cucumber.core.options;

import cucumber.api.SnippetType;

import java.util.List;

public interface RunnerOptions {
    List<String> getGlue();

    boolean isDryRun();

    SnippetType getSnippetType();
}
