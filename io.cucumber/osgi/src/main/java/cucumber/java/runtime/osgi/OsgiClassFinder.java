package cucumber.java.runtime.osgi;

import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Enumeration;

import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cucumber.runtime.ClassFinder;

public class OsgiClassFinder implements ClassFinder {

    private static final Logger LOGGER = LoggerFactory.getLogger(OsgiClassFinder.class);

    private final BundleContext bundleContext;

    public OsgiClassFinder(BundleContext bundleContext) {
        this.bundleContext = bundleContext;
    }

    @Override
    public <T> Collection<Class<? extends T>> getDescendants(Class<T> parentType, String packageName) {
        if (LOGGER.isDebugEnabled())
            LOGGER.debug("Looking for sub classes of " + parentType.getName() + " in '" + packageName + "' package");

        final String searchPath = packageName.replace('.', '/');

        final ArrayList<Class<? extends T>> result = new ArrayList<Class<? extends T>>();;
        for (Bundle bundle : bundleContext.getBundles()) {
            try {
                result.addAll(findClassesInBundle(bundle, searchPath, parentType));
            } catch (Exception e) {
                LOGGER.error("Failed to inspect bundle " + bundle.getSymbolicName() + ": " + e.getMessage(), e);
            }
        }
        result.trimToSize();
        return result;
    }

    @Override
    public <T> Class<? extends T> loadClass(String className) throws ClassNotFoundException {
        for (Bundle bundle : bundleContext.getBundles()) {
            return (Class<? extends T>) bundle.loadClass(className);
        }
        throw new ClassNotFoundException("Couldn't load class from bundles: " + className);
    }

    private <T> Collection<Class<? extends T>> findClassesInBundle(Bundle bundle, String searchPath, Class<T> parentType) {
        final Collection<Class<? extends T>> result = new ArrayList<Class<? extends T>>();
        final Enumeration<URL> resources = bundle.findEntries(searchPath, "*.class", true);
        if (resources == null)
            return Collections.emptyList();
        for (URL url : Collections.list(resources)) {
            final String className = pathToClassName(url.getPath());
            try {
                final Class<? extends T> castClass = loadClassFromBundle(parentType, bundle, className);
                if (castClass != null)
                    result.add(castClass);
            } catch (Exception e) {
                LOGGER.error("Failed to load class " + className, e);
            }
        }
        return result;
    }

    private <T> Class<? extends T> loadClassFromBundle(Class<T> parentType, Bundle bundle, String className) throws ClassNotFoundException {
        final Class<?> clazz = bundle.loadClass(className);
        if (clazz != null && !parentType.equals(clazz) && parentType.isAssignableFrom(clazz)) {
            return clazz.asSubclass(parentType);
        }
        return null;
    }

    private static String pathToClassName(final String path) {
        return path.substring(1, path.length() - 6).replace('/', '.');
    }
}
