package io.cucumber.core.runtime;

import io.cucumber.core.api.TypeRegistryConfigurer;

public interface TypeRegistryConfigurerSupplier {

    TypeRegistryConfigurer get();

}
