package io.cucumber.guice;

import io.cucumber.core.backend.Backend;
import io.cucumber.core.backend.BackendProviderService;
import io.cucumber.core.backend.Container;
import io.cucumber.core.backend.Lookup;

import java.util.function.Supplier;

public final class GuiceBackendProviderService implements BackendProviderService {

    @Override
    public Backend create(Lookup lookup, Container container, Supplier<ClassLoader> classLoaderSupplier) {
        return new GuiceBackend(container, classLoaderSupplier);
    }

}
