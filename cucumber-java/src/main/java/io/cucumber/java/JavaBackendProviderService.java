package io.cucumber.java;

import io.cucumber.core.backend.Backend;
import io.cucumber.core.backend.BackendProviderService;
import io.cucumber.core.backend.Container;
import io.cucumber.core.backend.Lookup;
import io.cucumber.core.backend.Options;

import java.util.function.Supplier;

public final class JavaBackendProviderService implements BackendProviderService {

    @Override
    public Backend create(
            Lookup lookup, Container container, Supplier<ClassLoader> classLoaderSupplier, Options options
    ) {
        return new JavaBackend(lookup, container, classLoaderSupplier, options);
    }

}
