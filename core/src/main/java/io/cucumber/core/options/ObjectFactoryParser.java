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
            throw new CucumberException(String.format("Could not load object factory class for '%s'", cucumberObjectFactory), e);
        }
        if (!ObjectFactory.class.isAssignableFrom(objectFactoryClass)) {
            throw new CucumberException(String.format("Object factory class '%s' was not a subclass of '%s'", objectFactoryClass, ObjectFactory.class));
        }
        return (Class<? extends ObjectFactory>) objectFactoryClass;
    }
}
