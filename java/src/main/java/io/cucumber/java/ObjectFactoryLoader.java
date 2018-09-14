package io.cucumber.java;

import cucumber.api.java.ObjectFactory;
import io.cucumber.core.io.ClassFinder;

import java.util.Iterator;
import java.util.ServiceLoader;

public class ObjectFactoryLoader {

    private static final ServiceLoader<ObjectFactory> LOADER = ServiceLoader.load(ObjectFactory.class);

    private ObjectFactoryLoader() {
    }

    /**
     * Loads an instance of {@link ObjectFactory}. The class name can be explicit, or it can be null.
     * When it's null, the implementation is searched for in the <pre>cucumber.runtime</pre> package.
     *
     * @param classFinder where to load classes from
     * @param objectFactoryClassName specific class name of {@link ObjectFactory} implementation. May be null.
     * @return an instance of {@link ObjectFactory}
     */
    // TODO remove once confirmed classFinder and/or objectFactoryClassName are not need for serviceLoader
    public static ObjectFactory loadObjectFactory(ClassFinder classFinder, String objectFactoryClassName) {
        return loadObjectFactory();
    }

    /**
     * Loads an instance of {@link ObjectFactory} using the {@link ServiceLoader}.
     * The class name can be explicit, or it can be null.
     * When it's null, the implementation is searched for in the <pre>cucumber.runtime</pre> package.
     *
     * @return an instance of {@link ObjectFactory}
     */
    public static ObjectFactory loadObjectFactory() {

        final Iterator<ObjectFactory> objectFactories = LOADER.iterator();

        ObjectFactory objectFactory;
        if (objectFactories.hasNext()) {
            objectFactory = objectFactories.next();

        } else {
            objectFactory = new DefaultJavaObjectFactory();
        }

        if (objectFactories.hasNext()) {
            System.out.println(getMultipleObjectFactoryLogMessage());
            objectFactory = new DefaultJavaObjectFactory();
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

}
