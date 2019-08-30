package io.cucumber.core.reflection;

import io.cucumber.core.io.ClassFinder;
import io.cucumber.core.exception.CucumberException;

import java.lang.reflect.Constructor;
import java.net.URI;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;

public final class Reflections {
    private final ClassFinder classFinder;

    public Reflections(ClassFinder classFinder) {
        this.classFinder = classFinder;
    }

    public static boolean isInstantiable(Class<?> clazz) {
        boolean isNonStaticInnerClass = !Modifier.isStatic(clazz.getModifiers()) && clazz.getEnclosingClass() != null;
        return Modifier.isPublic(clazz.getModifiers()) && !Modifier.isAbstract(clazz.getModifiers()) && !isNonStaticInnerClass;
    }

    public <T> T instantiateExactlyOneSubclass(Class<T> parentType, List<URI> packageNames, Class[] constructorParams, Object[] constructorArgs, T fallback) {
        Collection<? extends T> instances = instantiateSubclasses(parentType, packageNames, constructorParams, constructorArgs);
        if (instances.size() == 1) {
            return instances.iterator().next();
        } else if (instances.isEmpty()) {
            if(fallback != null) {
                return fallback;
            }
            throw new NoInstancesException(parentType);
        } else {
            throw new TooManyInstancesException(instances);
        }
    }

    private <T> Collection<? extends T> instantiateSubclasses(Class<T> parentType, List<URI> packageNames, Class[] constructorParams, Object[] constructorArgs) {
        Collection<T> result = new HashSet<>();
        for (URI packageName : packageNames) {
            for (Class<? extends T> clazz : classFinder.getDescendants(parentType, packageName)) {
                if (isInstantiable(clazz)) {
                    result.add(newInstance(constructorParams, constructorArgs, clazz));
                }
            }
        }
        return result;
    }

    private <T> T newInstance(Class[] constructorParams, Object[] constructorArgs, Class<? extends T> clazz) {
        Constructor<? extends T> constructor;
        try {
            constructor = clazz.getConstructor(constructorParams);
            try {
                return constructor.newInstance(constructorArgs);
            } catch (Exception e) {
                String message = String.format("Failed to instantiate %s with %s", constructor.toGenericString(), Arrays.asList(constructorArgs));
                throw new CucumberException(message, e);
            }
        } catch (NoSuchMethodException e) {
            throw new CucumberException(e);
        }
    }


}
