package io.cucumber.core.runtime;

import io.cucumber.core.backend.ObjectFactory;
import io.cucumber.core.backend.Options;
import io.cucumber.core.exception.CucumberException;

import java.lang.reflect.Constructor;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.ServiceLoader;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.util.Objects.requireNonNull;

public final class ObjectFactoryServiceLoader {

    private final Options options;

    public ObjectFactoryServiceLoader(Options options) {
        this.options = requireNonNull(options);
    }

    /**
     * Loads an instance of {@link ObjectFactory} using the
     * {@link ServiceLoader} mechanism.
     * <p>
     * Will load an instance of the class provided by
     * {@link Options#getObjectFactoryClass()}. If
     * {@link Options#getObjectFactoryClass()} does not provide a class and
     * there is exactly one {@code ObjectFactory} instance available that
     * instance will be used.
     * <p>
     * Otherwise {@link DefaultJavaObjectFactory} with no dependency injection
     * capabilities will be used.
     *
     * @return an instance of {@link ObjectFactory}
     */
    ObjectFactory loadObjectFactory() {
        Class<? extends ObjectFactory> objectFactoryClass = this.options.getObjectFactoryClass();

        final ServiceLoader<ObjectFactory> loader = ServiceLoader.load(ObjectFactory.class);
        if (objectFactoryClass == null) {
            return loadSingleObjectFactoryOrDefault(loader);

        }

        return loadSelectedObjectFactory(loader, objectFactoryClass);
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
            ObjectFactory extraObjectFactory = objectFactories.next();
            throw new CucumberException(getMultipleObjectFactoryLogMessage(objectFactory, extraObjectFactory));
        }
        return objectFactory;
    }

    private static ObjectFactory loadSelectedObjectFactory(
            ServiceLoader<ObjectFactory> loader, Class<? extends ObjectFactory> objectFactoryClass
    ) {
        for (ObjectFactory objectFactory : loader) {
            if (objectFactoryClass.equals(objectFactory.getClass())) {
                return objectFactory;
            }
        }

        throw new CucumberException("" +
                "Could not find object factory " + objectFactoryClass.getName() + ".\n" +
                "Cucumber uses SPI to discover object factory implementations.\n" +
                "Has the class been registered with SPI and is it available on the classpath?");
    }

    private static String getMultipleObjectFactoryLogMessage(ObjectFactory... objectFactories) {
        String factoryNames = Stream.of(objectFactories)
                .map(Object::getClass)
                .map(Class::getName)
                .collect(Collectors.joining(", "));

        return "More than one Cucumber ObjectFactory was found in the classpath\n" +
                "\n" +
                "Found: " + factoryNames + "\n" +
                "\n" +
                "You may have included, for instance, cucumber-spring AND cucumber-guice as part of\n" +
                "your dependencies. When this happens, Cucumber can't decide which to use.\n" +
                "In order to enjoy dependency injection features, either remove the unnecessary dependencies" +
                "from your classpath or use the `cucumber.object-factory` property or `@CucumberOptions(objectFactory=...)` to select one.\n";
    }

    /**
     * Creates glue instances. Does not provide Dependency Injection.
     * <p>
     * All glue classes must have a public no-argument constructor.
     */
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
                throw new CucumberException(String.format(
                    "%s doesn't have an empty constructor. If you need dependency injection, put cucumber-picocontainer on the classpath",
                    type), e);
            } catch (Exception e) {
                throw new CucumberException(String.format("Failed to instantiate %s", type), e);
            }
        }

    }

}
