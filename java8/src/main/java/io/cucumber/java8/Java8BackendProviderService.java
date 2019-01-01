package io.cucumber.java8;

import io.cucumber.core.backend.Backend;
import io.cucumber.core.backend.BackendProviderService;
import io.cucumber.core.backend.ObjectFactory;
import io.cucumber.core.io.ResourceLoader;
import io.cucumber.core.stepexpression.TypeRegistry;
import io.cucumber.java8.Java8Backend;

public class Java8BackendProviderService implements BackendProviderService {

    @Override
    public Backend create(ObjectFactory objectFactory, ResourceLoader resourceLoader, TypeRegistry typeRegistry) {
        return new Java8Backend(objectFactory, resourceLoader, typeRegistry);
    }
}
