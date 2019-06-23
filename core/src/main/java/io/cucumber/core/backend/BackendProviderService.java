package io.cucumber.core.backend;

import io.cucumber.core.io.ResourceLoader;

public interface BackendProviderService {

    Backend create(Lookup lookup, Container container, ResourceLoader resourceLoader);

}
