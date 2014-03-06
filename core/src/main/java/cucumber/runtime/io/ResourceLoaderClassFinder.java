package cucumber.runtime.io;

import cucumber.runtime.ClassFinder;

import java.io.File;
import java.util.Collection;
import java.util.HashSet;

public class ResourceLoaderClassFinder implements ClassFinder {
    private final ResourceLoader resourceLoader;
    private final ClassLoader classLoader;

    public ResourceLoaderClassFinder(ResourceLoader resourceLoader, ClassLoader classLoader) {
        this.resourceLoader = resourceLoader;
        this.classLoader = classLoader;
    }

    @Override
    public <T> Collection<Class<? extends T>> getDescendants(Class<T> parentType, String packageName) {
        Collection<Class<? extends T>> result = new HashSet<Class<? extends T>>();
        String packagePath = "classpath:" + packageName.replace('.', '/').replace(File.separatorChar, '/');
        for (Resource classResource : resourceLoader.resources(packagePath, ".class")) {
            String className = classResource.getClassName(".class");
            Class<?> clazz = loadClass(className, classLoader);
            if (clazz != null && !parentType.equals(clazz) && parentType.isAssignableFrom(clazz)) {
                result.add(clazz.asSubclass(parentType));
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
}
