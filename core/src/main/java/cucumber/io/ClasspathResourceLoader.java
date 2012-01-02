package cucumber.io;

import cucumber.runtime.CucumberException;
import cucumber.runtime.Utils;

import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.util.Collection;
import java.util.HashSet;

public class ClasspathResourceLoader implements ResourceLoader {
    @Override
    public Iterable<Resource> resources(String path, String suffix) {
        return new ClasspathIterable(cl(), path, suffix);
    }

    public Collection<Class<? extends Annotation>> getAnnotations(String packagePath) {
        return getDescendants(Annotation.class, packagePath);
    }

    public <T> Collection<Class<? extends T>> getDescendants(Class<T> parentType, String packagePath) {
        Collection<Class<? extends T>> result = new HashSet<Class<? extends T>>();
        for (Resource classResource : resources(packagePath, ".class")) {
            String className = className(classResource.getPath());
            Class<?> clazz = loadClass(className);
            if (clazz != null && !parentType.equals(clazz) && parentType.isAssignableFrom(clazz)) {
                result.add(clazz.asSubclass(parentType));
            }
        }
        return result;
    }

    public <T> T instantiateExactlyOneSubclass(Class<T> parentType, String packagePath, Class[] constructorParams, Object[] constructorArgs) {
        Collection<? extends T> instances = instantiateSubclasses(parentType, packagePath, constructorParams, constructorArgs);
        if (instances.size() == 1) {
            return instances.iterator().next();
        } else if (instances.size() == 0) {
            throw new CucumberException("Couldn't find a single implementation of " + parentType);
        } else {
            throw new CucumberException("Expected only one instance, but found too many: " + instances);
        }
    }

    public <T> Collection<? extends T> instantiateSubclasses(Class<T> parentType, String packagePath, Class[] constructorParams, Object[] constructorArgs) {
        Collection<T> result = new HashSet<T>();
        for (Class<? extends T> clazz : getDescendants(parentType, packagePath)) {
            if (Utils.isInstantiable(clazz)) {
                result.add(newInstance(constructorParams, constructorArgs, clazz));
            }
        }
        return result;
    }

    private Class<?> loadClass(String className) {
        try {
            return cl().loadClass(className);
        } catch (ClassNotFoundException ignore) {
            return null;
        } catch (NoClassDefFoundError ignore) {
            return null;
        }
    }

    private <T> T newInstance(Class[] constructorParams, Object[] constructorArgs, Class<? extends T> clazz) {
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

    private String className(String pathToClass) {
        return pathToClass.substring(0, pathToClass.length() - 6).replace("/", ".");
    }

    private ClassLoader cl() {
        return Thread.currentThread().getContextClassLoader();
    }
}
