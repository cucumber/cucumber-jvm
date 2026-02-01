package io.cucumber.core.runtime;

import io.cucumber.core.backend.ObjectFactory;
import org.jspecify.annotations.Nullable;

public final class SingletonObjectFactorySupplier implements ObjectFactorySupplier {

    private final ObjectFactoryServiceLoader objectFactoryServiceLoader;
    private @Nullable ObjectFactory objectFactory;

    public SingletonObjectFactorySupplier(ObjectFactoryServiceLoader objectFactoryServiceLoader) {
        this.objectFactoryServiceLoader = objectFactoryServiceLoader;
    }

    @Override
    public ObjectFactory get() {
        if (objectFactory == null) {
            objectFactory = objectFactoryServiceLoader.loadObjectFactory();
        }
        return objectFactory;
    }

}
