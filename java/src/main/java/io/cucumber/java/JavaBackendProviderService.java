package io.cucumber.java;

import io.cucumber.core.backend.Backend;
import io.cucumber.core.backend.BackendProviderService;
import io.cucumber.core.backend.Container;
import io.cucumber.core.backend.Lookup;
import io.cucumber.core.io.ResourceLoader;
import io.cucumber.core.stepexpression.TypeRegistry;

public final class JavaBackendProviderService implements BackendProviderService {

    @Override
    public Backend create(Lookup lookup, Container container, ResourceLoader resourceLoader) {
        return new JavaBackend(lookup, container, resourceLoader);
    }
}
