package io.cucumber.core.runtime;

import io.cucumber.core.backend.ObjectFactory;
import io.cucumber.core.options.CucumberProperties;

import java.util.Map;

import static io.cucumber.core.backend.ObjectFactoryLoader.loadObjectFactory;

public class SingletonObjectFactorySupplier implements ObjectFactorySupplier {

    private ObjectFactory objectFactory;

    @Override
    public ObjectFactory get() {
        if(objectFactory == null){
            Map<String, String> properties = CucumberProperties.create();
            objectFactory = loadObjectFactory(properties.get(ObjectFactory.class.getName()));
        }
        return objectFactory;
    }
}
