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
            throw new IllegalArgumentException(
                String.format("Could not load object factory class for '%s'", cucumberObjectFactory), e);
        }
        if (!ObjectFactory.class.isAssignableFrom(objectFactoryClass)) {
            throw new IllegalArgumentException(String.format("Object factory class '%s' was not a subclass of '%s'",
                objectFactoryClass, ObjectFactory.class));
        }
        return (Class<? extends ObjectFactory>) objectFactoryClass;
    }

}
