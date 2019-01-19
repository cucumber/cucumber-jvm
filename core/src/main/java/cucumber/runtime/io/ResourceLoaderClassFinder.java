package cucumber.runtime.io;

import cucumber.runtime.ClassFinder;

import java.net.URI;
import java.util.Collection;
import java.util.HashSet;

public class ResourceLoaderClassFinder implements ClassFinder {
    private static final String CLASS_SUFFIX = ".class";
    private static final String CLASSPATH_PREFIX = "classpath:";
    private static final char DOT = '.';
    private static final char PACKAGE_PATH_SEPARATOR = '/';
    private final ResourceLoader resourceLoader;
    private final ClassLoader classLoader;

    public ResourceLoaderClassFinder(ResourceLoader resourceLoader, ClassLoader classLoader) {
        this.resourceLoader = resourceLoader;
        this.classLoader = classLoader;
    }

    @Override
    public <T> Collection<Class<? extends T>> getDescendants(Class<T> parentType, String packageName) {
        Collection<Class<? extends T>> result = new HashSet<>();
        for (Resource classResource : resourceLoader.resources(packagePath(packageName), CLASS_SUFFIX)) {
            String className = getClassName(classResource.getPath().getSchemeSpecificPart());

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

    private URI packagePath(String packageName) {
        return URI.create(CLASSPATH_PREFIX + packageName.replace(DOT, PACKAGE_PATH_SEPARATOR));
    }

    private String getClassName(String path) {
        return path.substring(0, path.length() - CLASS_SUFFIX.length()).replace(PACKAGE_PATH_SEPARATOR, DOT);
    }

    public <T> Class<? extends T> loadClass(String className) throws ClassNotFoundException {
        return (Class<? extends T>) classLoader.loadClass(className);
    }
}
