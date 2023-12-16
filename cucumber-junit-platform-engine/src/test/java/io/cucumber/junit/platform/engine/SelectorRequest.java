package io.cucumber.junit.platform.engine;

import org.junit.platform.engine.ConfigurationParameters;
import org.junit.platform.engine.DiscoveryFilter;
import org.junit.platform.engine.DiscoverySelector;
import org.junit.platform.engine.EngineDiscoveryRequest;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

class SelectorRequest implements EngineDiscoveryRequest {

    private final Set<DiscoverySelector> resources = new HashSet<>();
    private final ConfigurationParameters parameters;

    SelectorRequest(ConfigurationParameters parameters, DiscoverySelector... selectors) {
        this(parameters, Arrays.asList(selectors));
    }

    SelectorRequest(DiscoverySelector... selectors) {
        this(new EmptyConfigurationParameters(), Arrays.asList(selectors));
    }

    SelectorRequest(List<DiscoverySelector> selectors) {
        this(new EmptyConfigurationParameters(), selectors);
    }

    SelectorRequest(ConfigurationParameters parameters, List<DiscoverySelector> selectors) {
        this.parameters = parameters;
        resources.addAll(selectors);
    }

    @Override
    public <T extends DiscoverySelector> List<T> getSelectorsByType(Class<T> selectorType) {
        return resources.stream()
                .filter(selectorType::isInstance)
                .map(selectorType::cast)
                .collect(Collectors.toList());
    }

    @Override
    public <T extends DiscoveryFilter<?>> List<T> getFiltersByType(Class<T> filterType) {
        return Collections.emptyList();
    }

    @Override
    public ConfigurationParameters getConfigurationParameters() {
        return parameters;
    }

}
