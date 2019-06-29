package io.cucumber.core.backend;

import io.cucumber.core.io.ResourceLoader;
import org.apiguardian.api.API;

@API(status = API.Status.STABLE)
public interface BackendProviderService {

    Backend create(Lookup lookup, Container container, ResourceLoader resourceLoader);

}
