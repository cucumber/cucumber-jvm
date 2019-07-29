package io.cucumber.core.options;

import io.cucumber.core.backend.ObjectFactory;
import io.cucumber.core.exception.CucumberException;
import io.cucumber.core.io.MultiLoader;
import io.cucumber.core.io.ResourceLoader;
import io.cucumber.core.feature.RerunLoader;

import java.util.List;
import java.util.Map;

import static io.cucumber.core.options.Constants.CUCUMBER_OBJECT_FACTORY_PROPERTY_NAME;
import static io.cucumber.core.options.Constants.CUCUMBER_OPTIONS_PROPERTY_NAME;

public final class CucumberPropertiesParser {

    private final ResourceLoader resourceLoader;

    public CucumberPropertiesParser(ResourceLoader resourceLoader) {
        this.resourceLoader = resourceLoader;
    }

    public CucumberPropertiesParser() {
        this(new MultiLoader(CucumberPropertiesParser.class.getClassLoader()));
    }

    public RuntimeOptionsBuilder parse(Map<String, String> properties) {
        final RuntimeOptionsBuilder builder;
        String cucumberOptions = properties.get(CUCUMBER_OPTIONS_PROPERTY_NAME);
        if (cucumberOptions != null) {
            RerunLoader rerunLoader = new RerunLoader(resourceLoader);
            RuntimeOptionsParser parser = new RuntimeOptionsParser(rerunLoader);
            List<String> args = ShellWords.parse(cucumberOptions);
            builder = parser.parse(args);
        } else {
            builder = new RuntimeOptionsBuilder();
        }

        String cucumberObjectFactory = properties.get(CUCUMBER_OBJECT_FACTORY_PROPERTY_NAME);
        if (cucumberObjectFactory != null) {
            Class<? extends ObjectFactory> objectFactoryClass = parseObjectFactory(cucumberObjectFactory);
            builder.setObjectFactoryClass(objectFactoryClass);
        }

        return builder;
    }

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
