package io.cucumber.picocontainer;

import io.cucumber.core.backend.Backend;
import io.cucumber.core.backend.BackendProviderService;
import io.cucumber.core.backend.Container;
import io.cucumber.core.backend.Lookup;
import io.cucumber.core.backend.Options;

import java.util.function.Supplier;

public final class PicoBackendProviderService implements BackendProviderService {

    @Override
    public Backend create(Lookup lookup, Container container, Supplier<ClassLoader> classLoader, Options options) {
        return new PicoBackend(container, classLoader);
    }

}
