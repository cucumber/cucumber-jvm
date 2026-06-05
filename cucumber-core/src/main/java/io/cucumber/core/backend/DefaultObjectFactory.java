package io.cucumber.core.backend;

import io.cucumber.core.exception.CucumberException;
import org.apiguardian.api.API;
import org.jspecify.annotations.Nullable;

import java.lang.reflect.Constructor;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * Default factory to instantiate glue classes. Loaded via SPI.
 * <p>
 * This object factory instantiates glue classes by using their public
 * no-argument constructor. As such it does not provide any dependency
 * injection.
 * <p>
 * Note: This class is intentionally an explicit part of the public api. It
 * allows the default object factory to be used even when another object factory
 * implementation is present through the
 * {@value io.cucumber.core.options.Constants#OBJECT_FACTORY_PROPERTY_NAME}
 * property or equivalent configuration options.
 *
 * @see ObjectFactory
 */
@API(status = API.Status.STABLE, since = "7.1.0")
public final class DefaultObjectFactory implements ObjectFactory {

    private final Map<Class<?>, Object> instances = new HashMap<>();

    @Override
    public void start() {
        // No-op
    }

    @Override
    public void stop() {
        instances.clear();
    }

    @Override
    public boolean addClass(Class<?> clazz) {
        return true;
    }

    @Override
    public <T> T getInstance(Class<T> type) {
        Object instance = instances.get(type);
        if (instance != null) {
            return type.cast(instance);
        }
        return cacheNewInstance(type);
    }

    private <T> T cacheNewInstance(Class<T> type) {
        Constructor<T> constructor = getNonPrivateZeroArgumentConstructor(type);
        if (constructor == null) {
            throw createNoAccessibleZeroArgumentConstructor(type, null);
        }
        try {
            T instance = makeAccessible(constructor).newInstance();
            instances.put(type, instance);
            return instance;
        } catch (IllegalAccessException e) {
            throw createNoAccessibleZeroArgumentConstructor(type, e);
        } catch (Exception e) {
            throw new CucumberException("Failed to instantiate %s".formatted(type), e);
        }
    }

    private static <T> CucumberException createNoAccessibleZeroArgumentConstructor(
            Class<T> type, @Nullable Throwable cause
    ) {
        return new CucumberException("""
                %s does not have an single accessible zero-argument constructor.

                To use dependency injection add an other ObjectFactory implementation such as:
                 * cucumber-picocontainer
                 * cucumber-spring
                 * cucumber-jakarta-cdi
                 * ...etc
                """.formatted(type), cause);
    }

    @SuppressWarnings("unchecked")
    public static <T> @Nullable Constructor<T> getNonPrivateZeroArgumentConstructor(Class<T> type) {
        var constructors = Arrays.stream(type.getDeclaredConstructors())//
                .filter(constructor -> !constructor.isSynthetic())//
                .filter(constructor -> !Modifier.isPrivate(constructor.getModifiers()))
                .filter(constructor -> constructor.getParameterCount() == 0)
                .toList();

        if (constructors.size() != 1) {
            return null;
        }
        return (Constructor<T>) constructors.get(0);
    }

    @SuppressWarnings("deprecation")
    public static <T> Constructor<T> makeAccessible(Constructor<T> constructor) {
        if ((!Modifier.isPublic(constructor.getModifiers())
                || !Modifier.isPublic(constructor.getDeclaringClass().getModifiers())) && !constructor.isAccessible()) {
            constructor.setAccessible(true);
        }
        return constructor;
    }

}
