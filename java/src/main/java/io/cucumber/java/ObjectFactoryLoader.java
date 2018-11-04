package io.cucumber.java;

import io.cucumber.core.exception.CucumberException;
import io.cucumber.java.api.ObjectFactory;

import java.util.Iterator;
import java.util.ServiceLoader;

class ObjectFactoryLoader {
    private ObjectFactoryLoader() {
    }

    /**
     * Loads an instance of {@link ObjectFactory} using the {@link ServiceLoader}.
     * When <code>objectFactoryClassName</code> is provided that object factory
     * will be used if present.
     * <p>
     * If <code>objectFactoryClassName</code> is not provided and there exactly one
     * instance present that instance will be used.
     * <p>
     * Otherwise a default object factory with no Dependency Injection capabilities
     * will be used.
     *
     * @param objectFactoryClassName optional object factory to use
     * @return an instance of {@link ObjectFactory}
     */
    static ObjectFactory loadObjectFactory(String objectFactoryClassName) {
        final ServiceLoader<ObjectFactory> loader = ServiceLoader.load(ObjectFactory.class);
        if (objectFactoryClassName == null) {
            return loadSingleObjectFactoryOrDefault(loader);

        }

        return loadSelectedObjectFactory(loader, objectFactoryClassName);
    }

    private static ObjectFactory loadSelectedObjectFactory(ServiceLoader<ObjectFactory> loader, String objectFactoryClassName) {
        for (ObjectFactory objectFactory : loader) {
            if (objectFactoryClassName.equals(objectFactory.getClass().getName())) {
                return objectFactory;
            }
        }

        throw new CucumberException("Could not find object factory " + objectFactoryClassName);
    }

    private static ObjectFactory loadSingleObjectFactoryOrDefault(ServiceLoader<ObjectFactory> loader) {
        final Iterator<ObjectFactory> objectFactories = loader.iterator();

        ObjectFactory objectFactory;
        if (objectFactories.hasNext()) {
            objectFactory = objectFactories.next();
        } else {
            objectFactory = new DefaultJavaObjectFactory();
        }

        if (objectFactories.hasNext()) {
            System.out.println(getMultipleObjectFactoryLogMessage());
            objectFactory = new DefaultJavaObjectFactory();
        }
        return objectFactory;
    }

    private static String getMultipleObjectFactoryLogMessage() {
        return "More than one Cucumber ObjectFactory was found in the classpath\n" +
            "\n" +
            "You probably may have included, for instance, cucumber-spring AND cucumber-guice as part of\n" +
            "your dependencies. When this happens, Cucumber falls back to instantiating the\n" +
            "DefaultJavaObjectFactory implementation which doesn't provide IoC.\n" +
            "In order to enjoy IoC features, please remove the unnecessary dependencies from your classpath.\n";
    }
}
