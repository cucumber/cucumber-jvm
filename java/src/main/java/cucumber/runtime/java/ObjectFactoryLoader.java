package cucumber.runtime.java;

import cucumber.runtime.ClassFinder;
import cucumber.runtime.CucumberException;
import cucumber.runtime.NoInstancesException;
import cucumber.runtime.Reflections;
import cucumber.runtime.TooManyInstancesException;
import io.cucumber.core.backend.ObjectFactory;
import io.cucumber.core.logging.Logger;
import io.cucumber.core.logging.LoggerFactory;

import java.net.URI;
import java.util.List;

import static java.util.Arrays.asList;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.Objects;
import java.util.ServiceLoader;

public final class ObjectFactoryLoader {

    private static final Logger LOG = LoggerFactory.getLogger(ObjectFactoryLoader.class);

    private ObjectFactoryLoader() {
    }

    /**
     * Loads an instance of {@link io.cucumber.core.backend.ObjectFactory}.
     * The class name of the {@link io.cucumber.core.backend.ObjectFactory} can be specified.
     * Also a class name specifying an old deprecated {@link cucumber.api.java.ObjectFactory} can be specified.
     * 
     * When a class name for {@link io.cucumber.core.backend.ObjectFactory} is specified, it will be instantiated and returned.
     * When a class name for {@link cucumber.api.java.ObjectFactory} is specified instead of a regular objectFactoryClassName,
     * it will be instantiated, wrapped in an adapter (turning it into a {@link io.cucumber.core.backend.ObjectFactory}) and returned.
     * 
     * When no class names are specified, an instance of {@link io.cucumber.core.backend.ObjectFactory} is searched for in the services
     * and instantiated using a ServiceLoader. When the ServiceLoader cannot find an ObjectFactory, a default ObjectFactory implementation
     * is searched for and instantiated in the Cucumber runtime.
     *
     * @param classFinder                      where to load classes from
     * @param objectFactoryClassName           specific class name of the {@link io.cucumber.core.backend.ObjectFactory} implementation. May be null.
     * @param deprecatedObjectFactoryClassName specific class name of the deprecated {@link cucumber.api.java.ObjectFactory} implementation. May be null.
     * 
     * @return an instance of {@link io.cucumber.core.backend.ObjectFactory}
     */
    public static ObjectFactory loadObjectFactory(ClassFinder classFinder, String objectFactoryClassName, String deprecatedObjectFactoryClassName) {
        try {
            final Reflections reflections = new Reflections(classFinder);
            if ((objectFactoryClassName == null) && (deprecatedObjectFactoryClassName == null)) {
                return loadSingleObjectFactory(reflections);
            } else if (objectFactoryClassName != null) {
                return loadSelectedObjectFactory(reflections, classFinder, objectFactoryClassName);
            } else {
                return loadSelectedDeprecatedObjectFactory(reflections, classFinder, deprecatedObjectFactoryClassName);
            }
        } catch (TooManyInstancesException e) {
            LOG.warn(e.getMessage());
            LOG.warn(getMultipleObjectFactoryLogMessage());
            return new DefaultJavaObjectFactory();
        } catch (NoInstancesException e) {
            return new DefaultJavaObjectFactory();
        } catch (ClassNotFoundException e) {
            throw new CucumberException("Couldn't instantiate custom ObjectFactory", e);
        }
    }

    private static ObjectFactory loadSingleObjectFactory(final Reflections reflections) {
        Iterator<ObjectFactory> availableObjectFactoriesIt = ServiceLoader.load(ObjectFactory.class).iterator();
        if (availableObjectFactoriesIt.hasNext()) {
            final List<ObjectFactory> instances = new ArrayList<>();
            do {
                instances.add(availableObjectFactoriesIt.next());
            } while (availableObjectFactoriesIt.hasNext());
            if (instances.size() > 1) {
                throw new TooManyInstancesException(instances);
            }
            return instances.get(0);
        } else {
            return loadDefaultRuntimeObjectFactory(reflections);
        }
    }

    private static ObjectFactory loadSelectedObjectFactory(final Reflections reflections, final ClassFinder classFinder, final String objectFactoryClassName) throws ClassNotFoundException {
        ObjectFactory objectFactory = reflections.newInstance(new Class[0], new Object[0], classFinder.<ObjectFactory>loadClass(objectFactoryClassName));
        LOG.info("Using ObjectFactory " + objectFactory.getClass().getSimpleName());
        return objectFactory;
    }

    private static ObjectFactory loadDefaultRuntimeObjectFactory(final Reflections reflections) {
        final List<URI> packages = asList(URI.create("classpath:cucumber/runtime"));
        ObjectFactory objectFactory = reflections.instantiateExactlyOneSubclass(ObjectFactory.class, packages, new Class[0], new Object[0], null);
        if (objectFactory != null) {
            LOG.info("Default ObjectFactory " + objectFactory.getClass().getSimpleName() + " loaded by reflection.");
        }
        return objectFactory;
    }

    private static String getMultipleObjectFactoryLogMessage() {
        StringBuilder sb = new StringBuilder();
        sb.append("More than one Cucumber ObjectFactory was found in the classpath\n\n");
        sb.append("You probably may have included, for instance, cucumber-spring AND cucumber-guice as part of\n");
        sb.append("your dependencies. When this happens, Cucumber falls back to instantiating the\n");
        sb.append("DefaultJavaObjectFactory implementation which doesn't provide IoC.\n");
        sb.append("In order to enjoy IoC features, please remove the unnecessary dependencies from your classpath.\n");
        return sb.toString();
    }

    @Deprecated
    private static ObjectFactory loadSelectedDeprecatedObjectFactory(final Reflections reflections, final ClassFinder classFinder, final String objectFactoryClassName) throws ClassNotFoundException {
        final Iterator<? extends cucumber.api.java.ObjectFactory> availableObjectFactoriesIt = ServiceLoader.load(classFinder.<cucumber.api.java.ObjectFactory>loadClass(objectFactoryClassName)).iterator();
        if (availableObjectFactoriesIt.hasNext()) {
            return new ObjectFactoryAdapter(availableObjectFactoriesIt.next());
        } else {
            LOG.warn("Deprecated cucumber.api.java.ObjectFactory " + objectFactoryClassName + " loaded by reflection.");
            return new ObjectFactoryAdapter(reflections.newInstance(new Class[0], new Object[0], classFinder.<cucumber.api.java.ObjectFactory>loadClass(objectFactoryClassName)));
        }
    }

    @Deprecated
    private static class ObjectFactoryAdapter implements ObjectFactory {

        private final cucumber.api.java.ObjectFactory delegate;

        public ObjectFactoryAdapter(final cucumber.api.java.ObjectFactory delegate) {
            this.delegate = Objects.requireNonNull(delegate);
        }

        @Override
        public void start() {
            delegate.start();
        }

        @Override
        public void stop() {
            delegate.stop();
        }

        @Override
        public boolean addClass(Class<?> arg0) {
            return delegate.addClass(arg0);
        }

        @Override
        public <T> T getInstance(Class<T> arg0) {
            return delegate.getInstance(arg0);
        }

    }

}
