package io.cucumber.java8;

import io.cucumber.core.backend.Backend;
import io.cucumber.core.backend.BackendProviderService;
import io.cucumber.core.backend.Container;
import io.cucumber.core.io.ResourceLoader;
import io.cucumber.core.stepexpression.TypeRegistry;

public final class Java8BackendProviderService implements BackendProviderService {

    @Override
    public Backend create(Container container, ResourceLoader resourceLoader, TypeRegistry typeRegistry) {
        return new Java8Backend(container, resourceLoader, typeRegistry);
    }
}
