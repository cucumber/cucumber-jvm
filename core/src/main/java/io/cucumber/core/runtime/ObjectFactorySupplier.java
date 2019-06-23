package io.cucumber.core.runtime;

import io.cucumber.core.backend.ObjectFactory;

public interface ObjectFactorySupplier {

    ObjectFactory get();

}
