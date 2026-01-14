package io.cucumber.core.options;

import io.cucumber.core.backend.ObjectFactory;

public final class ObjectFactoryParser {

    private ObjectFactoryParser() {

    }

    @SuppressWarnings("unchecked")
    public static Class<? extends ObjectFactory> parseObjectFactory(String cucumberObjectFactory) {
        Class<?> objectFactoryClass;
        try {
            objectFactoryClass = Class.forName(cucumberObjectFactory);
        } catch (ClassNotFoundException e) {
            throw new IllegalArgumentException("Could not load object factory class for '%s'".formatted(cucumberObjectFactory), e);
        }
        if (!ObjectFactory.class.isAssignableFrom(objectFactoryClass)) {
            throw new IllegalArgumentException("Object factory class '%s' was not a subclass of '%s'".formatted(objectFactoryClass, ObjectFactory.class));
        }
        return (Class<? extends ObjectFactory>) objectFactoryClass;
    }

}
