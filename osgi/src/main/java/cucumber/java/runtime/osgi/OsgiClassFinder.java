package cucumber.java.runtime.osgi;

import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Enumeration;

import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.wiring.BundleRevision;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cucumber.runtime.ClassFinder;

public class OsgiClassFinder implements ClassFinder {

    private static final Logger LOGGER = LoggerFactory.getLogger(OsgiClassFinder.class);
    private static final String TARGET_CLASSES = "target/classes/";
	private static final String DOT_CLASS = ".class";
	
    private final BundleContext bundleContext;

    public OsgiClassFinder(BundleContext bundleContext) {
        this.bundleContext = bundleContext;
    }

    @Override
    public <T> Collection<Class<? extends T>> getDescendants(Class<T> parentType, String packageName) {
       
    	final String searchPath = packageName.replace('.', '/');

        final ArrayList<Class<? extends T>> result = new ArrayList<Class<? extends T>>();
        
        for (Bundle bundle : bundleContext.getBundles()) {
            
        	/** 
        	 * Fragments don't have a ClassLoader, so don't try to load classes from them.
        	 * Their classes will be loaded inside their hosts. 
        	 */
        	if ((bundle.adapt(BundleRevision.class).getTypes() & BundleRevision.TYPE_FRAGMENT) == 0) {
        	
	        	try {
	        		if (LOGGER.isDebugEnabled())
	                    LOGGER.debug("Looking for sub classes of " + parentType.getName() + " in '" + packageName + "' package");
	        		
	                result.addAll(findClassesInBundle(bundle, "", searchPath, parentType));
	                
	                if (LOGGER.isDebugEnabled())
	                    LOGGER.debug("Looking for fragment classes of " + parentType.getName() + " in '" + searchPath + "' path");
	                
	                result.addAll(findClassesInBundle(bundle, TARGET_CLASSES, TARGET_CLASSES+searchPath, parentType));
	            } 
	        	catch (Exception e) {
	                LOGGER.error("Failed to inspect bundle " + bundle.getSymbolicName() + ": " + e.getMessage(), e);
	            }
        	}
        }
        
        result.trimToSize();
        
        if (LOGGER.isDebugEnabled()) {
        	for (Class<? extends T> clazz : result) {
        		LOGGER.debug("Found class " + clazz.getName());
        	}
        }
        
        return result;
    }

    @SuppressWarnings("unchecked")
	@Override
    public <T> Class<? extends T> loadClass(String className) throws ClassNotFoundException {
    	Class<? extends T> loadedClass = null;
    	for (Bundle bundle : bundleContext.getBundles()) {
            if ((loadedClass = (Class<? extends T>) bundle.loadClass(className)) != null) {
            	break;
            }
        }
    	if (loadedClass != null) {
    		return loadedClass;
    	}
        throw new ClassNotFoundException("Couldn't load class from bundles: " + className);
    }

    protected <T> Collection<Class<? extends T>> findClassesInBundle(Bundle bundle, String prefix, String searchPath, Class<T> parentType) {
        final Collection<Class<? extends T>> result = new ArrayList<Class<? extends T>>();
        final Enumeration<URL> resources = bundle.findEntries(searchPath, "*.class", true);
        if (resources == null)
            return Collections.emptyList();
        for (URL url : Collections.list(resources)) {
            final String className = pathToClassName(prefix, url.getPath());
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

    protected <T> Class<? extends T> loadClassFromBundle(Class<T> parentType, Bundle bundle, String className) throws ClassNotFoundException {
        final Class<?> clazz = bundle.loadClass(className);
        if (clazz != null && !parentType.equals(clazz) && parentType.isAssignableFrom(clazz)) {
            return clazz.asSubclass(parentType);
        }
        return null;
    }

    protected static String pathToClassName(final String prefix, final String path) {
        return path.substring(prefix.length()+1, path.length() - DOT_CLASS.length()).replace('/', '.');
    }
}
