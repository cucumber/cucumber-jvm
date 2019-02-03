package io.cucumber.core.options;

import cucumber.api.SnippetType;

import java.net.URI;
import java.util.List;

public interface RunnerOptions {
    List<URI> getGlue();

    boolean isDryRun();

    SnippetType getSnippetType();
}
