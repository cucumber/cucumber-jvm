package io.cucumber.core.runner;

import io.cucumber.core.backend.GlueDiscoveryRequest;
import io.cucumber.core.backend.ObjectFactory;
import io.cucumber.core.eventbus.UuidGenerator;
import io.cucumber.core.snippets.SnippetType;
import org.jspecify.annotations.Nullable;

import java.net.URI;
import java.util.Collections;
import java.util.List;
import java.util.Set;

public interface Options {

    List<URI> getGlue();

    boolean isDryRun();

    SnippetType getSnippetType();

    @Nullable
    Class<? extends ObjectFactory> getObjectFactoryClass();

    @Nullable
    Class<? extends UuidGenerator> getUuidGeneratorClass();

    default Set<String> getGlueClasses() {
        return Collections.emptySet();
    }

    GlueDiscoveryRequest getGlueDiscoveryRequest();
}
