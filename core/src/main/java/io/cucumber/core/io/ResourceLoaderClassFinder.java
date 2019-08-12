package io.cucumber.core.io;

import java.net.URI;
import java.util.Collection;
import java.util.HashSet;

public final class ResourceLoaderClassFinder implements ClassFinder {
    private static final String CLASS_SUFFIX = ".class";
    private static final char DOT = '.';
    private static final char PACKAGE_PATH_SEPARATOR = '/';
    private final ResourceLoader resourceLoader;
    private final ClassLoader classLoader;

    public ResourceLoaderClassFinder(ResourceLoader resourceLoader, ClassLoader classLoader) {
        this.resourceLoader = resourceLoader;
        this.classLoader = classLoader;
    }

    @Override
    public <T> Collection<Class<? extends T>> getDescendants(Class<T> parentType, URI packageName) {
        Collection<Class<? extends T>> result = new HashSet<>();
        for (Resource classResource : resourceLoader.resources(packageName, CLASS_SUFFIX)) {
            String className = getClassName(classResource.getPath());
            try {
                Class<?> clazz = loadClass(className);
                if (clazz != null && !parentType.equals(clazz) && parentType.isAssignableFrom(clazz)) {
                    result.add(clazz.asSubclass(parentType));
                }
            } catch (ClassNotFoundException | NoClassDefFoundError ignore) {
            }
        }
        return result;
    }

    private static String getClassName(URI uri) {
        String resourceName = Classpath.resourceName(uri);
        return resourceName.substring(0, resourceName.length() - CLASS_SUFFIX.length()).replace(PACKAGE_PATH_SEPARATOR, DOT);
    }

    @SuppressWarnings("unchecked")
    public <T> Class<? extends T> loadClass(String className) throws ClassNotFoundException {
        return (Class<? extends T>) classLoader.loadClass(className);
    }
}
