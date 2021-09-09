package io.cucumber.core.runner;

import io.cucumber.core.backend.ObjectFactory;
import io.cucumber.core.snippets.SnippetType;

import java.net.URI;
import java.util.List;

public interface Options {

    List<URI> getGlue();

    boolean isDryRun();

    SnippetType getSnippetType();

    Class<? extends ObjectFactory> getObjectFactoryClass();

}
