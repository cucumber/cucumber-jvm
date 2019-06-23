package io.cucumber.core.runtime;

import io.cucumber.core.stepexpression.TypeRegistry;

public interface TypeRegistrySupplier {
    TypeRegistry get();
}
