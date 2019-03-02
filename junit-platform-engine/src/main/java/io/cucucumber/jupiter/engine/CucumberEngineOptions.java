package io.cucucumber.jupiter.engine;

import cucumber.api.SnippetType;
import cucumber.runtime.Shellwords;
import io.cucumber.core.model.GluePath;
import io.cucumber.core.options.PluginOptions;
import io.cucumber.core.options.RunnerOptions;
import org.junit.platform.engine.ConfigurationParameters;

import java.net.URI;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import static io.cucucumber.jupiter.engine.Constants.ANSI_COLORS_DISABLED_PROPERTY_NAME;
import static io.cucucumber.jupiter.engine.Constants.DRY_RUN_ENABLED_PROPERTY_NAME;
import static io.cucucumber.jupiter.engine.Constants.GLUE_PROPERTY_NAME;
import static io.cucucumber.jupiter.engine.Constants.PLUGIN_PROPERTY_NAME;
import static io.cucucumber.jupiter.engine.Constants.SNIPPET_TYPE_PROPERTY_NAME;
import static io.cucucumber.jupiter.engine.Constants.STRICT_ENABLED_PROPERTY_NAME;
import static java.util.Collections.emptyList;


class CucumberEngineOptions implements PluginOptions, RunnerOptions {

    private final ConfigurationParameters configurationParameters;

    CucumberEngineOptions(ConfigurationParameters configurationParameters) {
        this.configurationParameters = configurationParameters;
    }

    @Override
    public List<String> getPluginNames() {
        return configurationParameters
            .get(PLUGIN_PROPERTY_NAME, Shellwords::parse)
            .orElse(emptyList());
    }

    @Override
    public boolean isStrict() {
        return configurationParameters.getBoolean(STRICT_ENABLED_PROPERTY_NAME).orElse(false);
    }

    @Override
    public boolean isMonochrome() {
        return configurationParameters.getBoolean(ANSI_COLORS_DISABLED_PROPERTY_NAME).orElse(false);
    }

    @Override
    public List<URI> getGlue() {
        return configurationParameters
            .get(GLUE_PROPERTY_NAME, Shellwords::parse)
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
            .get(SNIPPET_TYPE_PROPERTY_NAME, SnippetType::fromString)
            .orElse(SnippetType.UNDERSCORE);
    }

}
