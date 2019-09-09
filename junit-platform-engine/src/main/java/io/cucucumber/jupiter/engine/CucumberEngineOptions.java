package io.cucucumber.jupiter.engine;

import io.cucumber.core.backend.ObjectFactory;
import io.cucumber.core.feature.GluePath;
import io.cucumber.core.plugin.Options;
import io.cucumber.core.snippets.SnippetType;
import org.junit.platform.engine.ConfigurationParameters;

import java.net.URI;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import static io.cucucumber.jupiter.engine.Constants.ANSI_COLORS_DISABLED_PROPERTY_NAME;
import static io.cucucumber.jupiter.engine.Constants.DRY_RUN_ENABLED_PROPERTY_NAME;
import static io.cucucumber.jupiter.engine.Constants.GLUE_PROPERTY_NAME;
import static io.cucucumber.jupiter.engine.Constants.SNIPPET_TYPE_PROPERTY_NAME;
import static io.cucucumber.jupiter.engine.Constants.STRICT_ENABLED_PROPERTY_NAME;


class CucumberEngineOptions implements Options, io.cucumber.core.runner.Options, io.cucumber.core.backend.Options {

    private final ConfigurationParameters configurationParameters;

    CucumberEngineOptions(ConfigurationParameters configurationParameters) {
        this.configurationParameters = configurationParameters;
    }

    @Override
    public boolean isStrict() {
        return configurationParameters.getBoolean(STRICT_ENABLED_PROPERTY_NAME).orElse(false);
    }

    @Override
    public Iterable<Plugin> plugins() {
        return null;
    }

    @Override
    public boolean isMonochrome() {
        return configurationParameters.getBoolean(ANSI_COLORS_DISABLED_PROPERTY_NAME).orElse(false);
    }

    @Override
    public List<URI> getGlue() {
        return configurationParameters
            .get(GLUE_PROPERTY_NAME, s -> Arrays.asList(s.split(",")))
            .orElse(Collections.singletonList("classpath:"))
            .stream()
            .map(GluePath::parse)
            .collect(Collectors.toList());
    }

    @Override
    public boolean isDryRun() {
        return configurationParameters.getBoolean(DRY_RUN_ENABLED_PROPERTY_NAME).orElse(false);
    }

    @Override
    public SnippetType getSnippetType() {
        return configurationParameters
            .get(SNIPPET_TYPE_PROPERTY_NAME, SnippetType::valueOf)
            .orElse(SnippetType.UNDERSCORE);
    }

    @Override
    public Class<? extends ObjectFactory> getObjectFactoryClass() {
        return null;
    }

}
