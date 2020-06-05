package io.cucumber.junit.platform.engine;

import org.junit.platform.engine.ConfigurationParameters;
import org.junit.platform.engine.DiscoveryFilter;
import org.junit.platform.engine.DiscoverySelector;
import org.junit.platform.engine.EngineDiscoveryRequest;

import java.util.Collections;
import java.util.List;

class EmptyEngineDiscoveryRequest implements EngineDiscoveryRequest {

    private final ConfigurationParameters config;

    EmptyEngineDiscoveryRequest(ConfigurationParameters config) {
        this.config = config;
    }

    @Override
    public <T extends DiscoverySelector> List<T> getSelectorsByType(Class<T> selectorType) {
        return Collections.emptyList();
    }

    @Override
    public <T extends DiscoveryFilter<?>> List<T> getFiltersByType(Class<T> filterType) {
        return Collections.emptyList();
    }

    @Override
    public ConfigurationParameters getConfigurationParameters() {
        return config;
    }

}
