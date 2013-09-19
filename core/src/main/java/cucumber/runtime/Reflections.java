package cucumber.runtime;

import java.lang.reflect.InvocationTargetException;
import java.util.Collection;
import java.util.HashSet;

public class Reflections {
    private final ClassFinder classFinder;

    public static final String NO_INSTANCES_MSG = "Couldn't find a single implementation of ";
    public static final String TOO_MANY_INSTANCES_MSG = "Expected only one instance, but found too many";

    public Reflections(ClassFinder classFinder) {
        this.classFinder = classFinder;
    }

    public <T> T instantiateExactlyOneSubclass(Class<T> parentType, String packageName, Class[] constructorParams, Object[] constructorArgs) {
        Collection<? extends T> instances = instantiateSubclasses(parentType, packageName, constructorParams, constructorArgs);
        if (instances.size() == 1) {
            return instances.iterator().next();
        } else if (instances.size() == 0) {
            throw new CucumberException(NO_INSTANCES_MSG + parentType);
        } else {
            throw new CucumberException(TOO_MANY_INSTANCES_MSG + ": " + instances);
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
        try {
            return clazz.getConstructor(constructorParams).newInstance(constructorArgs);
        } catch (InstantiationException e) {
            throw new CucumberException(e);
        } catch (IllegalAccessException e) {
            throw new CucumberException(e);
        } catch (InvocationTargetException e) {
            throw new CucumberException(e);
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
