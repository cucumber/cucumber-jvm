package io.cucumber.core.runner;

import io.cucumber.core.backend.ObjectFactory;
import io.cucumber.core.eventbus.UuidGenerator;
import io.cucumber.core.snippets.SnippetType;

import java.net.URI;
import java.util.Collections;
import java.util.List;
import java.util.Set;

public interface Options {

    List<URI> getGlue();

    boolean isDryRun();

    SnippetType getSnippetType();

    Class<? extends ObjectFactory> getObjectFactoryClass();

    Class<? extends UuidGenerator> getUuidGeneratorClass();

    default Set<String> getGlueClasses() {
        return Collections.emptySet();
    }
}
