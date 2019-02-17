package io.cucumber.core.options;

import io.cucumber.core.api.options.SnippetType;

import java.net.URI;
import java.util.List;

public interface RunnerOptions {
    List<URI> getGlue();

    boolean isDryRun();

    SnippetType getSnippetType();
}
