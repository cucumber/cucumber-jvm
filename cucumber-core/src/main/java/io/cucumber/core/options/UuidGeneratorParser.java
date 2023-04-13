package io.cucumber.core.options;

import io.cucumber.core.eventbus.UuidGenerator;

public final class UuidGeneratorParser {

    private UuidGeneratorParser() {

    }

    @SuppressWarnings("unchecked")
    public static Class<? extends UuidGenerator> parseUuidGenerator(String cucumberUuidGenerator) {
        Class<?> uuidGeneratorClass;
        try {
            uuidGeneratorClass = Class.forName(cucumberUuidGenerator);
        } catch (ClassNotFoundException e) {
            throw new IllegalArgumentException(
                String.format("Could not load UUID generator class for '%s'", cucumberUuidGenerator), e);
        }
        if (!UuidGenerator.class.isAssignableFrom(uuidGeneratorClass)) {
            throw new IllegalArgumentException(String.format("UUID generator class '%s' was not a subclass of '%s'",
                uuidGeneratorClass, UuidGenerator.class));
        }
        return (Class<? extends UuidGenerator>) uuidGeneratorClass;
    }

}
