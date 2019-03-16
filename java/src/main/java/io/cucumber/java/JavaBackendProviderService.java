package io.cucumber.java;

import io.cucumber.core.backend.Backend;
import io.cucumber.core.backend.BackendProviderService;
import io.cucumber.core.backend.Container;
import io.cucumber.core.io.ResourceLoader;
import io.cucumber.core.stepexpression.TypeRegistry;

public class JavaBackendProviderService implements BackendProviderService {

    @Override
    public Backend create(Container container, ResourceLoader resourceLoader, TypeRegistry typeRegistry) {
        return new JavaBackend(container, resourceLoader, typeRegistry);
    }
}
