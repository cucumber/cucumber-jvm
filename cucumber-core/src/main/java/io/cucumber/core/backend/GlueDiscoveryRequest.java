package io.cucumber.core.backend;

import java.util.List;

public interface GlueDiscoveryRequest {

    <T extends GlueDiscoverySelector> List<T> getSelectorsByType(Class<T> selectorType);

    ConfigurationParameters getConfigurationParameters();

}
