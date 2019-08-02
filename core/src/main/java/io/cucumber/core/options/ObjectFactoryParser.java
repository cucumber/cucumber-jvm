package io.cucumber.core.options;

import io.cucumber.core.backend.ObjectFactory;
import io.cucumber.core.exception.CucumberException;

class ObjectFactoryParser {
    @SuppressWarnings("unchecked")
    static Class<? extends ObjectFactory> parseObjectFactory(String cucumberObjectFactory) {
        Class<?> objectFactoryClass;
        try {
            objectFactoryClass = Class.forName(cucumberObjectFactory);
        } catch (ClassNotFoundException e) {
            throw new CucumberException("Could not load object factory class for " + cucumberObjectFactory, e);
        }
        if (!ObjectFactory.class.isAssignableFrom(objectFactoryClass)) {
            throw new CucumberException("Object factory class " + objectFactoryClass + " was not a subclass of " + ObjectFactory.class);
        }
        return (Class<? extends ObjectFactory>) objectFactoryClass;
    }
}
