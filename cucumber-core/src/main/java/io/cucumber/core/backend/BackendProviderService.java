package io.cucumber.core.backend;

import org.apiguardian.api.API;

import java.util.function.Supplier;

@API(status = API.Status.STABLE)
public interface BackendProviderService {

    Backend create(Lookup lookup, Container container, Supplier<ClassLoader> classLoader);

}
