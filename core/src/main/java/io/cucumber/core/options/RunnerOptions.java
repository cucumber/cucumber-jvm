package io.cucumber.core.options;

import io.cucumber.core.api.options.SnippetType;

import java.util.List;

public interface RunnerOptions {
    List<String> getGlue();

    boolean isDryRun();

    SnippetType getSnippetType();
}
