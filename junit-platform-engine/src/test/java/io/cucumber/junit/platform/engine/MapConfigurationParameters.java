package io.cucumber.junit.platform.engine;

import org.junit.platform.engine.ConfigurationParameters;

import java.util.Collections;
import java.util.Map;
import java.util.Optional;

class MapConfigurationParameters implements ConfigurationParameters {

    private final Map<String, String> parameters;

    MapConfigurationParameters(String key, String value) {
        this(Collections.singletonMap(key, value));
    }

    MapConfigurationParameters(Map<String, String> parameters) {
        this.parameters = parameters;
    }

    @Override
    public Optional<String> get(String key) {
        return Optional.ofNullable(parameters.get(key));
    }

    @Override
    public Optional<Boolean> getBoolean(String key) {
        return get(key, Boolean::valueOf);
    }

    @Override
    public int size() {
        return 0;
    }

}
