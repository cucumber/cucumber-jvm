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
                "Could not load UUID generator class for '%s'".formatted(cucumberUuidGenerator), e);
        }
        if (!UuidGenerator.class.isAssignableFrom(uuidGeneratorClass)) {
            throw new IllegalArgumentException("UUID generator class '%s' was not a subclass of '%s'"
                    .formatted(uuidGeneratorClass, UuidGenerator.class));
        }
        return (Class<? extends UuidGenerator>) uuidGeneratorClass;
    }

}
