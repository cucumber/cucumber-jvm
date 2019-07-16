package cucumber.runtime.java;

import cucumber.api.java.ObjectFactory;
import cucumber.runtime.ClassFinder;
import cucumber.runtime.CucumberException;
import cucumber.runtime.NoInstancesException;
import cucumber.runtime.Reflections;
import cucumber.runtime.TooManyInstancesException;
import io.cucumber.core.logging.Logger;
import io.cucumber.core.logging.LoggerFactory;

import java.net.URI;
import java.util.List;

import static java.util.Arrays.asList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Objects;
import java.util.ServiceLoader;

public final class ObjectFactoryLoader {

    private static final Logger LOG = LoggerFactory.getLogger(ObjectFactoryLoader.class);

    private ObjectFactoryLoader() {
    }

    /**
     * Loads an instance of {@link ObjectFactory}.
     * A given class name will cause an object factory of type {@link io.cucumber.core.backend.ObjectFactory} to be loaded.
     * A given deprecated class name will cause an object factory of type {@link ObjectFactory} to be loaded.
     * When both are null, the implementation is searched for with a service loader or in the <pre>cucumber.runtime</pre> package.
     *
     * @param classFinder                      where to load classes from
     * @param objectFactoryClassName           specific class name of {@link io.cucumber.core.backend.ObjectFactory} implementation. May be null.
     * @param deprecatedObjectFactoryClassName specific class name of {@link ObjectFactory} implementation. May be null.
     * 
     * @return an instance of {@link ObjectFactory}
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

    /**
     * Loads an ObjectFactory of the type {@link io.cucumber.core.backend.ObjectFactory} either via the ServiceLoader or by searching the runtime classpath.
     */
    private static ObjectFactory loadSingleObjectFactory(final Reflections reflections) {
        Iterator<io.cucumber.core.backend.ObjectFactory> serviceLoaderObjectFactories = ServiceLoader.load(io.cucumber.core.backend.ObjectFactory.class).iterator();
        if (serviceLoaderObjectFactories.hasNext()) {
            final Collection<io.cucumber.core.backend.ObjectFactory> instances = new HashSet<>();
            do {
                instances.add(serviceLoaderObjectFactories.next());
            } while (serviceLoaderObjectFactories.hasNext());
            if (instances.size() > 1) {
                throw new TooManyInstancesException(instances);
            }
            io.cucumber.core.backend.ObjectFactory objectFactory = instances.iterator().next();
            LOG.info("Loading ObjectFactory via service loader: " + objectFactory.getClass().getName() );
            return new ObjectFactoryAdapter(objectFactory);
        } else {
            final List<URI> packages = asList(URI.create("classpath:cucumber/runtime"));
            ObjectFactory objectFactory = reflections.instantiateExactlyOneSubclass(ObjectFactory.class, packages, new Class[0], new Object[0], null);
            if(objectFactory != null){
                LOG.warn("Loading deprecated ObjectFactory from runtime via reflection: " + objectFactory.getClass().getName());
            }
            return objectFactory;
        }
    }

    /**
     * Loads an ObjectFactory of the type {@link io.cucumber.core.backend.ObjectFactory} which is defined by its class name
     * either via the ServiceLoader or by trying to instantiate it directly.
     */
    private static ObjectFactory loadSelectedObjectFactory(final Reflections reflections, final ClassFinder classFinder, final String objectFactoryClassName) throws ClassNotFoundException {
        final Iterator<? extends io.cucumber.core.backend.ObjectFactory> serviceLoaderObjectFactories = ServiceLoader.load(classFinder.<io.cucumber.core.backend.ObjectFactory>loadClass(objectFactoryClassName)).iterator();
        if (serviceLoaderObjectFactories.hasNext()) {
            io.cucumber.core.backend.ObjectFactory objectFactory = serviceLoaderObjectFactories.next();
            LOG.info("Loading ObjectFactory via service loader: " + objectFactory.getClass().getName() );
            return new ObjectFactoryAdapter(objectFactory);
        } else {
            LOG.info("Loading ObjectFactory via reflection: " + objectFactoryClassName);
            return new ObjectFactoryAdapter(reflections.newInstance(new Class[0], new Object[0], classFinder.<io.cucumber.core.backend.ObjectFactory>loadClass(objectFactoryClassName)));
        }
    }

    /**
     * Loads an ObjectFactory of the deprecated type {@link cucumber.api.java.ObjectFactory} which is defined by its class name
     * either via the ServiceLoader or by trying to instantiate it directly.
     */
    @Deprecated
    private static ObjectFactory loadSelectedDeprecatedObjectFactory(final Reflections reflections, final ClassFinder classFinder, final String objectFactoryClassName) throws ClassNotFoundException {
        final Iterator<? extends ObjectFactory> serviceLoaderObjectFactories = ServiceLoader.load(classFinder.<ObjectFactory>loadClass(objectFactoryClassName)).iterator();
        if (serviceLoaderObjectFactories.hasNext()) {
            ObjectFactory objectFactory = serviceLoaderObjectFactories.next();
            LOG.warn("Loading deprecated ObjectFactory via service loader: " + objectFactory.getClass().getName() );
            return objectFactory;
        } else {
            LOG.warn("Loading deprecated ObjectFactory via reflection: " + objectFactoryClassName);
            return reflections.newInstance(new Class[0], new Object[0], classFinder.<ObjectFactory>loadClass(objectFactoryClassName));
        }
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
    private static class ObjectFactoryAdapter implements ObjectFactory {

        private final io.cucumber.core.backend.ObjectFactory delegate;

        public ObjectFactoryAdapter(final io.cucumber.core.backend.ObjectFactory delegate) {
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
