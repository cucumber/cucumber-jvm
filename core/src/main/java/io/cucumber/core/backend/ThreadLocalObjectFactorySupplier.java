package io.cucumber.core.backend;


import io.cucumber.core.options.Env;

import static io.cucumber.core.backend.ObjectFactoryLoader.loadObjectFactory;

public class ThreadLocalObjectFactorySupplier implements ObjectFactorySupplier {

    private final ThreadLocal<ObjectFactory> runners = ThreadLocal.withInitial(
        () -> loadObjectFactory(Env.INSTANCE.get(ObjectFactory.class.getName()))
    );

    @Override
    public ObjectFactory get() {
        return runners.get();
    }
}
