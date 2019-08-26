package cucumber.java.runtime.osgi;

import java.lang.reflect.Field;
import java.util.Collection;

import javax.inject.Inject;

import org.osgi.framework.BundleContext;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceReference;

import cucumber.api.osgi.Filter;
import cucumber.api.osgi.ServiceNotFoundException;
import cucumber.api.osgi.TimeoutException;

public class OsgiObjectFactory extends OsgiObjectFactoryBase {

    private BundleContext bundleContext;

    public OsgiObjectFactory(BundleContext bundleContext) {
        this.bundleContext = bundleContext;
    }

    @Override
    protected void prepareGlueInstance(Object instance) {
        injectFields(instance);
    }

    public void injectFields(Object target) {
        for (   Class<?> targetClass = target.getClass();
                targetClass != Object.class;
                targetClass = targetClass.getSuperclass()) {

            injectDeclaredFields(target, targetClass);
        }
    }

    private void injectDeclaredFields(Object target, Class<?> targetClass) {
        for (Field field : targetClass.getDeclaredFields()) {
            if (field.getAnnotation(Inject.class) != null) {
                injectField(target, targetClass, field);
            }
        }
    }

    private void injectField(Object target, Class<?> targetClass, Field field) {
        Class<?> type = field.getType();
        long timeout = 500;
        String filterString = "";

        final Filter filter = field.getAnnotation(Filter.class);
        if (filter != null) {
            filterString = filter.value();
            if (filter.timeout() != 0)
                timeout = filter.timeout();
        }

        // Retrieve bundle Context just before calling getService to avoid that the bundle restarts
        // in between
        final Object service = (BundleContext.class == type) ?
                bundleContext : getService(type, timeout, filterString);
        setField(target, field, service);
    }

    private void setField(Object target, Field field, Object service) {
        try {
            final boolean accessible = field.isAccessible();

            if (!accessible) field.setAccessible(true);

            try {
                field.set(target, service);
            }
            finally {
                if (!accessible) field.setAccessible(false);
            }
        }
        catch (IllegalAccessException e) {
            throw new IllegalStateException(e);
        }
    }

    private Object getService(Class<?> type, long timeout, String filter) {
        final String serviceName = type.getName() + " " + filter;

        try {
            final long tryUntil = System.currentTimeMillis() + timeout;
            Object service = null;
            search : while (service == null) {
                final Collection<?> serviceReferences = bundleContext.getServiceReferences(type, filter.isEmpty() ? null : filter);

                for (Object serviceReference : serviceReferences) {
                    service = bundleContext.getService((ServiceReference<?>) serviceReference);
                    break search;
                }

                if (tryUntil < System.currentTimeMillis())
                    break;

                Thread.sleep(20);
            }

            if (service == null)
                throw new ServiceNotFoundException(serviceName);
            return service;
        } catch (InvalidSyntaxException e) {
            throw new IllegalArgumentException(e);
        } catch (InterruptedException e) {
            throw new TimeoutException(serviceName);
        }
    }
}
