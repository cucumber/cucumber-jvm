package io.cucumber.core.runtime;


import io.cucumber.core.backend.ObjectFactory;
import io.cucumber.core.options.CucumberProperties;

import java.util.Map;

import static io.cucumber.core.backend.ObjectFactoryLoader.loadObjectFactory;

public class ThreadLocalObjectFactorySupplier implements ObjectFactorySupplier {

    private final ThreadLocal<ObjectFactory> runners = ThreadLocal.withInitial(
        () -> {
            Map<String, String> properties = CucumberProperties.create();
            return loadObjectFactory(properties.get(ObjectFactory.class.getName()));
        }
    );

    @Override
    public ObjectFactory get() {
        return runners.get();
    }
}
