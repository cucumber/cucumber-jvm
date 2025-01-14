package io.cucumber.core.runner;

import io.cucumber.core.backend.ObjectFactory;
import io.cucumber.core.eventbus.UuidGenerator;
import io.cucumber.core.snippets.SnippetType;
import io.cucumber.tagexpressions.Expression;

import java.net.URI;
import java.util.List;

public interface Options {

    List<URI> getGlue();

    boolean isDryRun();

    SnippetType getSnippetType();

    Class<? extends ObjectFactory> getObjectFactoryClass();

    Class<? extends UuidGenerator> getUuidGeneratorClass();

    List<Expression> getSkipTagExpressions();

}
