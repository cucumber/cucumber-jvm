package cucumber.runtime.java;

import cucumber.api.java.ObjectFactory;
import cucumber.runtime.ClassFinder;
import cucumber.runtime.CucumberException;
import cucumber.runtime.NoInstancesException;
import cucumber.runtime.Reflections;
import cucumber.runtime.TooManyInstancesException;

public class ObjectFactoryLoader {
    /**
     * Loads an instance of {@link ObjectFactory}. The class name can be explicit, or it can be null.
     * When it's null, the implementation is searched for in the <pre>cucumber.runtime</pre> packahe.
     *
     * @param classFinder where to load classes from
     * @param objectFactoryClassName specific class name of {@link ObjectFactory} implementation. May be null.
     * @return an instance of {@link ObjectFactory}
     */
    public static ObjectFactory loadObjectFactory(ClassFinder classFinder, String objectFactoryClassName) {
        ObjectFactory objectFactory;
        try {
            Reflections reflections = new Reflections(classFinder);

            if(objectFactoryClassName != null) {
                Class<ObjectFactory> objectFactoryClass = (Class<ObjectFactory>) classFinder.getClassLoader().loadClass(objectFactoryClassName);
                objectFactory = reflections.newInstance(new Class[0], new Object[0], objectFactoryClass);
            } else {
                objectFactory = reflections.instantiateExactlyOneSubclass(ObjectFactory.class, "cucumber.runtime", new Class[0], new Object[0]);
            }
        } catch (TooManyInstancesException e) {
            System.out.println(e.getMessage());
            System.out.println(getMultipleObjectFactoryLogMessage());
            objectFactory = new DefaultJavaObjectFactory();
        } catch (NoInstancesException e) {
            objectFactory = new DefaultJavaObjectFactory();
        } catch (ClassNotFoundException e) {
            throw new CucumberException("Couldn't instantiate custom ObjectFactory", e);
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
