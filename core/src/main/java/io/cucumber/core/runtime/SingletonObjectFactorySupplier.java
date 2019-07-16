package io.cucumber.core.runtime;

import io.cucumber.core.backend.ObjectFactory;
import io.cucumber.core.options.CucumberProperties;
import io.cucumber.core.runner.Options;

import java.util.Map;

import static io.cucumber.core.runtime.ObjectFactoryLoader.loadObjectFactory;

public class SingletonObjectFactorySupplier implements ObjectFactorySupplier {

    private final Options options;
    private ObjectFactory objectFactory;

    public SingletonObjectFactorySupplier(Options options) {
        this.options = options;
    }

    @Override
    public ObjectFactory get() {
        if(objectFactory == null){
            objectFactory = loadObjectFactory(options.getObjectFactoryClass());
        }
        return objectFactory;
    }
}
