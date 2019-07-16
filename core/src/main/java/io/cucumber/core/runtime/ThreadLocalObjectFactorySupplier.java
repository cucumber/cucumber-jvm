package io.cucumber.core.runtime;


import io.cucumber.core.backend.ObjectFactory;
import io.cucumber.core.runner.Options;

import java.util.function.Supplier;

import static io.cucumber.core.runtime.ObjectFactoryLoader.loadObjectFactory;

public class ThreadLocalObjectFactorySupplier implements ObjectFactorySupplier {

    private final Options options;
    private final ThreadLocal<ObjectFactory> runners = ThreadLocal.withInitial(objectFactorySupplier());

    private Supplier<ObjectFactory> objectFactorySupplier() {
        return () -> loadObjectFactory(options.getObjectFactoryClass());
    }

    public ThreadLocalObjectFactorySupplier(Options options) {
        this.options = options;
    }

    @Override
    public ObjectFactory get() {
        return runners.get();
    }
}
