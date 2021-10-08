package io.cucumber.core.runtime;

import io.cucumber.core.backend.DefaultObjectFactory;
import io.cucumber.core.backend.ObjectFactory;
import io.cucumber.core.backend.Options;
import io.cucumber.core.exception.CucumberException;

import java.util.Iterator;
import java.util.ServiceLoader;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.util.Objects.requireNonNull;

/**
 * Loads an instance of {@link ObjectFactory} using the {@link ServiceLoader}
 * mechanism.
 * <p>
 * Will load an instance of the class provided by
 * {@link Options#getObjectFactoryClass()}. If
 * {@link Options#getObjectFactoryClass()} does not provide a class and there is
 * exactly one {@code ObjectFactory} instance available that instance will be
 * used.
 * <p>
 * Otherwise {@link DefaultObjectFactory} with no dependency injection
 */
public final class ObjectFactoryServiceLoader {

    private final Supplier<ClassLoader> classLoaderSupplier;
    private final Options options;

    public ObjectFactoryServiceLoader(Supplier<ClassLoader> classLoaderSupplier, Options options) {
        this.classLoaderSupplier = requireNonNull(classLoaderSupplier);
        this.options = requireNonNull(options);
    }

    ObjectFactory loadObjectFactory() {
        Class<? extends ObjectFactory> objectFactoryClass = options.getObjectFactoryClass();
        ClassLoader classLoader = classLoaderSupplier.get();
        ServiceLoader<ObjectFactory> loader = ServiceLoader.load(ObjectFactory.class, classLoader);
        if (objectFactoryClass == null) {
            return loadSingleObjectFactoryOrDefault(loader);
        }

        return loadSelectedObjectFactory(loader, objectFactoryClass);
    }

    private static ObjectFactory loadSingleObjectFactoryOrDefault(ServiceLoader<ObjectFactory> loader) {
        Iterator<ObjectFactory> objectFactories = loader.iterator();

        // Find the first non-default object factory,
        // or the default as a side effect.
        ObjectFactory objectFactory = null;
        while (objectFactories.hasNext()) {
            objectFactory = objectFactories.next();
            if (!(objectFactory instanceof DefaultObjectFactory)) {
                break;
            }
        }

        if (objectFactory == null) {
            throw new CucumberException("" +
                    "Could not find any object factory.\n" +
                    "\n" +
                    "Cucumber uses SPI to discover object factory implementations.\n" +
                    "This typically happens when using shaded jars. Make sure\n" +
                    "to merge all SPI definitions in META-INF/services correctly");
        }

        // Check if there are no other non-default object factories
        while (objectFactories.hasNext()) {
            ObjectFactory extraObjectFactory = objectFactories.next();
            if (extraObjectFactory instanceof DefaultObjectFactory) {
                continue;
            }
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
                "\n" +
                "Cucumber uses SPI to discover object factory implementations.\n" +
                "Has the class been registered with SPI and is it available on\n" +
                "the classpath?");
    }

    private static String getMultipleObjectFactoryLogMessage(ObjectFactory... objectFactories) {
        String factoryNames = Stream.of(objectFactories)
                .map(Object::getClass)
                .map(Class::getName)
                .collect(Collectors.joining(", "));

        return "More than one Cucumber ObjectFactory was found on the classpath\n" +
                "\n" +
                "Found: " + factoryNames + "\n" +
                "\n" +
                "You may have included, for instance, cucumber-spring AND cucumber-guice as part\n" +
                "of your dependencies. When this happens, Cucumber can't decide which to use.\n" +
                "In order to enjoy dependency injection features, either remove the unnecessary\n" +
                "dependencies from your classpath or use the `cucumber.object-factory` property\n" +
                "or `@CucumberOptions(objectFactory=...)` to select one.\n";
    }

}
