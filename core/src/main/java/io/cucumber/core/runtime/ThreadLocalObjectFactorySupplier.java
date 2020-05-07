package io.cucumber.core.runtime;

import io.cucumber.core.backend.ObjectFactory;

import static java.lang.ThreadLocal.withInitial;
import static java.util.Objects.requireNonNull;

public final class ThreadLocalObjectFactorySupplier implements ObjectFactorySupplier {

    private final ThreadLocal<ObjectFactory> runners;

    public ThreadLocalObjectFactorySupplier(ObjectFactoryServiceLoader objectFactoryServiceLoader) {
        this.runners = withInitial(requireNonNull(objectFactoryServiceLoader)::loadObjectFactory);
    }

    @Override
    public ObjectFactory get() {
        return runners.get();
    }

}
