package io.cucumber.core.runtime;

import io.cucumber.core.backend.ObjectFactory;
import io.cucumber.core.exception.CucumberException;

import java.lang.reflect.Constructor;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
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
     * @param objectFactoryClass optional object factory to use
     * @return an instance of {@link ObjectFactory}
     */
    static ObjectFactory loadObjectFactory(Class<? extends ObjectFactory> objectFactoryClass) {
        final ServiceLoader<ObjectFactory> loader = ServiceLoader.load(ObjectFactory.class);
        if (objectFactoryClass == null) {
            return loadSingleObjectFactoryOrDefault(loader);

        }

        return loadSelectedObjectFactory(loader, objectFactoryClass);
    }

    private static ObjectFactory loadSelectedObjectFactory(ServiceLoader<ObjectFactory> loader, Class<? extends ObjectFactory> objectFactoryClass) {
        for (ObjectFactory objectFactory : loader) {
            if (objectFactoryClass.equals(objectFactory.getClass())) {
                return objectFactory;
            }
        }

        throw new CucumberException("Could not find object factory " + objectFactoryClass);
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

    static class DefaultJavaObjectFactory implements ObjectFactory {
        private final Map<Class<?>, Object> instances = new HashMap<>();

        public void start() {
            // No-op
        }

        public void stop() {
            instances.clear();
        }

        public boolean addClass(Class<?> clazz) {
            return true;
        }

        public <T> T getInstance(Class<T> type) {
            T instance = type.cast(instances.get(type));
            if (instance == null) {
                instance = cacheNewInstance(type);
            }
            return instance;
        }

        private <T> T cacheNewInstance(Class<T> type) {
            try {
                Constructor<T> constructor = type.getConstructor();
                T instance = constructor.newInstance();
                instances.put(type, instance);
                return instance;
            } catch (NoSuchMethodException e) {
                throw new CucumberException(String.format("%s doesn't have an empty constructor. If you need DI, put cucumber-picocontainer on the classpath", type), e);
            } catch (Exception e) {
                throw new CucumberException(String.format("Failed to instantiate %s", type), e);
            }
        }
    }
}
