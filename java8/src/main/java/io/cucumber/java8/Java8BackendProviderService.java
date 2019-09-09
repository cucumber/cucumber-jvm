package io.cucumber.java8;

import io.cucumber.core.backend.Backend;
import io.cucumber.core.backend.BackendProviderService;
import io.cucumber.core.backend.Container;
import io.cucumber.core.backend.Lookup;
import io.cucumber.core.io.ResourceLoader;
import io.cucumber.core.stepexpression.TypeRegistry;

public final class Java8BackendProviderService implements BackendProviderService {

    @Override
    public Backend create(Lookup lookup, Container container, ResourceLoader resourceLoader) {
        return new Java8Backend(lookup, container, resourceLoader);
    }
}
