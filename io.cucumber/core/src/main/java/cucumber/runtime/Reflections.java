package cucumber.runtime;

import java.lang.reflect.Constructor;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;

public class Reflections {
    private final ClassFinder classFinder;

    public Reflections(ClassFinder classFinder) {
        this.classFinder = classFinder;
    }

    public <T> T instantiateExactlyOneSubclass(Class<T> parentType, String packageName, Class[] constructorParams, Object[] constructorArgs) {
        Collection<? extends T> instances = instantiateSubclasses(parentType, packageName, constructorParams, constructorArgs);
        if (instances.size() == 1) {
            return instances.iterator().next();
        } else if (instances.isEmpty()) {
            throw new NoInstancesException(parentType);
        } else {
            throw new TooManyInstancesException(instances);
        }
    }

    public <T> Collection<? extends T> instantiateSubclasses(Class<T> parentType, String packageName, Class[] constructorParams, Object[] constructorArgs) {
        Collection<T> result = new HashSet<T>();
        for (Class<? extends T> clazz : classFinder.getDescendants(parentType, packageName)) {
            if (Utils.isInstantiable(clazz) && hasConstructor(clazz, constructorParams)) {
                result.add(newInstance(constructorParams, constructorArgs, clazz));
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
