package cucumber.runtime;

import java.lang.reflect.Constructor;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;

public class Reflections {
    private final ClassFinder classFinder;

    public Reflections(ClassFinder classFinder) {
        this.classFinder = classFinder;
    }

    public <T> T instantiateExactlyOneSubclass(Class<T> parentType, List<String> packageNames, Class[] constructorParams, Object[] constructorArgs, T fallback) {
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

    public <T> Collection<? extends T> instantiateSubclasses(Class<T> parentType, List<String> packageNames, Class[] constructorParams, Object[] constructorArgs) {
        Collection<T> result = new HashSet<T>();
        for (String packageName : packageNames) {
            for (Class<? extends T> clazz : classFinder.getDescendants(parentType, packageName)) {
                if (Utils.isInstantiable(clazz)) {
                    result.add(newInstance(constructorParams, constructorArgs, clazz));
                }
            }
        }
        return result;
    }

    public <T> T newInstance(Class[] constructorParams, Object[] constructorArgs, Class<? extends T> clazz) {
        Constructor<? extends T> constructor = null;
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

    private boolean hasConstructor(Class<?> clazz, Class[] paramTypes) {
        try {
            clazz.getConstructor(paramTypes);
            return true;
        } catch (NoSuchMethodException e) {
            return false;
        }
    }


}
