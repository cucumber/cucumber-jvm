package io.cucumber.java8;

import io.cucumber.core.backend.Backend;
import io.cucumber.core.backend.BackendProviderService;
import io.cucumber.core.backend.Container;
import io.cucumber.core.backend.Lookup;

import java.util.function.Supplier;

public final class Java8BackendProviderService implements BackendProviderService {

    @Override
    public Backend create(Lookup lookup, Container container, Supplier<ClassLoader> classLoaderProvider) {
        return new Java8Backend(lookup, container, classLoaderProvider);
    }

}
