package io.cucumber.core.backend;

import io.cucumber.core.options.Env;

import static io.cucumber.core.backend.ObjectFactoryLoader.loadObjectFactory;

public class SingletonObjectFactorySupplier implements ObjectFactorySupplier {

    private ObjectFactory objectFactory;

    @Override
    public ObjectFactory get() {
        if(objectFactory == null){
            objectFactory = loadObjectFactory(Env.INSTANCE.get(ObjectFactory.class.getName()));
        }
        return objectFactory;
    }
}
