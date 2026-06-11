package io.cucumber.core.runner;

import io.cucumber.core.backend.GlueDiscoveryRequest;
import io.cucumber.core.backend.ObjectFactory;
import io.cucumber.core.eventbus.UuidGenerator;
import io.cucumber.core.snippets.SnippetType;
import org.jspecify.annotations.Nullable;

public interface Options {

    boolean isDryRun();

    SnippetType getSnippetType();

    @Nullable
    Class<? extends ObjectFactory> getObjectFactoryClass();

    @Nullable
    Class<? extends UuidGenerator> getUuidGeneratorClass();

    GlueDiscoveryRequest getGlueDiscoveryRequest();

}
