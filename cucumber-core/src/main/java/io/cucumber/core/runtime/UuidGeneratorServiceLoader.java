package io.cucumber.core.runtime;

import io.cucumber.core.eventbus.IncrementingUuidGenerator;
import io.cucumber.core.eventbus.Options;
import io.cucumber.core.eventbus.RandomUuidGenerator;
import io.cucumber.core.eventbus.UuidGenerator;
import io.cucumber.core.exception.CucumberException;

import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.ServiceLoader;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.util.Objects.requireNonNull;

/**
 * Loads an instance of {@link UuidGenerator} using the {@link ServiceLoader}
 * mechanism.
 * <p>
 * Will load an instance of the class provided by
 * {@link Options#getUuidGeneratorClass()}. If
 * {@link Options#getUuidGeneratorClass()} does not provide a class, if there is
 * exactly one {@code UuidGenerator} instance available that instance will be
 * used.
 * <p>
 * Otherwise {@link RandomUuidGenerator} with no dependency injection
 */
public final class UuidGeneratorServiceLoader {

    private final Supplier<ClassLoader> classLoaderSupplier;
    private final Options options;

    public UuidGeneratorServiceLoader(Supplier<ClassLoader> classLoaderSupplier, Options options) {
        this.classLoaderSupplier = requireNonNull(classLoaderSupplier);
        this.options = requireNonNull(options);
    }

    UuidGenerator loadUuidGenerator() {
        Class<? extends UuidGenerator> objectFactoryClass = options.getUuidGeneratorClass();
        ClassLoader classLoader = classLoaderSupplier.get();
        ServiceLoader<UuidGenerator> loader = ServiceLoader.load(UuidGenerator.class, classLoader);
        if (objectFactoryClass == null) {
            return loadSingleUuidGeneratorOrDefault(loader);
        }

        return loadSelectedUuidGenerator(loader, objectFactoryClass);
    }

    private static UuidGenerator loadSingleUuidGeneratorOrDefault(ServiceLoader<UuidGenerator> loader) {
        Iterator<UuidGenerator> uuidGenerators = loader.iterator();

        // categorize the UUID generators (random, incrementing or external)
        UuidGenerator randomGenerator = null;
        UuidGenerator incrementingGenerator = null;
        UuidGenerator externalGenerator = null;
        while (uuidGenerators.hasNext()) {
            UuidGenerator uuidGenerator = uuidGenerators.next();
            if (uuidGenerator instanceof RandomUuidGenerator) {
                randomGenerator = uuidGenerator;
            } else if (uuidGenerator instanceof IncrementingUuidGenerator) {
                incrementingGenerator = uuidGenerator;
            } else {
                if (externalGenerator != null) {
                    // we have multiple external generators, which is an error
                    throw new CucumberException(getMultipleUuidGeneratorLogMessage(
                        Arrays.asList(externalGenerator, uuidGenerator)));
                }
                externalGenerator = uuidGenerator;
            }
        }

        // decide which generator to use
        if (externalGenerator != null) {
            // we have a single external generator
            return externalGenerator;
        } else if (randomGenerator != null) {
            // we don't have any external generators, use random if available
            return randomGenerator;
        } else if (incrementingGenerator != null) {
            // we don't have any external generators and no random, use
            // incrementing if available
            return incrementingGenerator;
        } else {
            // we don't have any generators at all, throw an error
            throw new CucumberException("" +
                    "Could not find any UUID generator.\n" +
                    "\n" +
                    "Cucumber uses SPI to discover UUID generator implementations.\n" +
                    "This typically happens when using shaded jars. Make sure\n" +
                    "to merge all SPI definitions in META-INF/services correctly");
        }
    }

    private static UuidGenerator loadSelectedUuidGenerator(
            ServiceLoader<UuidGenerator> loader,
            Class<? extends UuidGenerator> uuidGeneratorClass
    ) {
        for (UuidGenerator uuidGenerator : loader) {
            if (uuidGeneratorClass.equals(uuidGenerator.getClass())) {
                return uuidGenerator;
            }
        }

        throw new CucumberException("" +
                "Could not find UUID generator " + uuidGeneratorClass.getName() + ".\n" +
                "\n" +
                "Cucumber uses SPI to discover UUID generator implementations.\n" +
                "Has the class been registered with SPI and is it available on\n" +
                "the classpath?");
    }

    private static String getMultipleUuidGeneratorLogMessage(List<UuidGenerator> uuidGenerators) {
        String factoryNames = Stream.of(uuidGenerators)
                .map(Object::getClass)
                .map(Class::getName)
                .collect(Collectors.joining(", "));

        return "More than one Cucumber UuidGenerator was found on the classpath\n" +
                "\n" +
                "Found: " + factoryNames + "\n" +
                "\n" +
                "You can either remove the unnecessary SPI dependencies from your classpath\n" +
                "or use the `cucumber.uuid-generator` property\n" +
                "or `@CucumberOptions(uuidGenerator=...)` to select one UUID generator.\n";
    }

}
