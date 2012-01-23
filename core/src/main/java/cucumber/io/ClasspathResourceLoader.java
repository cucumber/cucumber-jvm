package cucumber.io;

import cucumber.runtime.CucumberException;
import cucumber.runtime.Utils;

import java.io.File;
import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.util.Collection;
import java.util.HashSet;

public class ClasspathResourceLoader implements ResourceLoader {
    private final ClassLoader classLoader;

    public ClasspathResourceLoader(ClassLoader classLoader) {
        this.classLoader = classLoader;
    }

    @Override
    public Iterable<Resource> resources(String path, String suffix) {
        return new ClasspathIterable(classLoader, path, suffix);
    }

    public Collection<Class<? extends Annotation>> getAnnotations(String packageName) {
        return getDescendants(Annotation.class, packageName);
    }

    public <T> Collection<Class<? extends T>> getDescendants(Class<T> parentType, String packageName) {
        String packagePath = packageName.replace('.', '/').replace(File.separatorChar, '/');
        Collection<Class<? extends T>> result = new HashSet<Class<? extends T>>();
        for (Resource classResource : resources(packagePath, ".class")) {
            String className = classResource.getClassName();
            Class<?> clazz = loadClass(className, classLoader);
            if (clazz != null && !parentType.equals(clazz) && parentType.isAssignableFrom(clazz)) {
                result.add(clazz.asSubclass(parentType));
            }
        }
        return result;
    }

    public <T> T instantiateExactlyOneSubclass(Class<T> parentType, String packageName, Class[] constructorParams, Object[] constructorArgs) {
        Collection<? extends T> instances = instantiateSubclasses(parentType, packageName, constructorParams, constructorArgs);
        if (instances.size() == 1) {
            return instances.iterator().next();
        } else if (instances.size() == 0) {
            throw new CucumberException("Couldn't find a single implementation of " + parentType);
        } else {
            throw new CucumberException("Expected only one instance, but found too many: " + instances);
        }
    }

    public <T> Collection<? extends T> instantiateSubclasses(Class<T> parentType, String packageName, Class[] constructorParams, Object[] constructorArgs) {
        Collection<T> result = new HashSet<T>();
        for (Class<? extends T> clazz : getDescendants(parentType, packageName)) {
            if (Utils.isInstantiable(clazz) && Utils.hasConstructor(clazz, constructorParams)) {
                result.add(newInstance(constructorParams, constructorArgs, clazz));
            }
        }
        return result;
    }

    private Class<?> loadClass(String className, ClassLoader classLoader) {
        try {
            return classLoader.loadClass(className);
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
}
