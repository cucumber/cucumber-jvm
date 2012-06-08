package cucumber.runtime.java.openejb;

import cucumber.runtime.CucumberException;
import cucumber.runtime.Utils;
import cucumber.runtime.java.ObjectFactory;
import org.apache.openejb.OpenEjbContainer;

import javax.ejb.embeddable.EJBContainer;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;

public class OpenEJBObjectFactory implements ObjectFactory {
    static {
        try {
            configureLog4J();
        } catch (Throwable t) {
            throw new CucumberException(t);
        }
    }

    private final List<String> classes = new ArrayList<String>();
    private final Map<Class<?>, Object> instances = new HashMap<Class<?>, Object>();
    private EJBContainer container;

    @Override
    public void start() {
        final StringBuilder callers = new StringBuilder();
        for (Iterator<String> it = classes.iterator(); it.hasNext(); ) {
            callers.append(it.next());
            if (it.hasNext()) {
                callers.append(",");
            }
        }

        Properties properties = new Properties();
        properties.setProperty(OpenEjbContainer.Provider.OPENEJB_ADDITIONNAL_CALLERS_KEY, callers.toString());
        container = EJBContainer.createEJBContainer(properties);
    }

    @Override
    public void stop() {
        container.close();
        instances.clear();
    }

    @Override
    public void addClass(Class<?> clazz) {
        classes.add(clazz.getName());
    }

    @Override
    public <T> T getInstance(Class<T> type) {
        if (instances.containsKey(type)) {
            return (T) instances.get(type);
        }

        T object;
        try {
            object = type.newInstance();
            container.getContext().bind("inject", object);
        } catch (Exception e) {
            throw new CucumberException("can't create " + type.getName(), e);
        }
        instances.put(type, object);
        return object;
    }

    private static void configureLog4J() throws Throwable {
        final ClassLoader cl = Thread.currentThread().getContextClassLoader();
        if (System.getProperty("log4j.configuration") == null && System.getProperty("log4j.configurationClass") == null
                && cl.getResource("log4j.xml") == null && cl.getResource("log4j.properties") == null) {
            Properties log4jProp = new Properties();
            log4jProp.setProperty("log4j.rootLogger", "info, stdout");
            log4jProp.setProperty("log4j.appender.stdout", "org.apache.log4j.ConsoleAppender");
            try {
                cl.loadClass("org.apache.openejb.logging.SimpleJULLikeLayout");
                log4jProp.setProperty("log4j.appender.stdout.layout", "org.apache.openejb.logging.SimpleJULLikeLayout");
            } catch (Exception e) {
                log4jProp.setProperty("log4j.appender.stdout.layout", "org.apache.log4j.SimpleLayout");
            }

            Method configure = cl.loadClass("org.apache.log4j.PropertyConfigurator").getDeclaredMethod("configure", Properties.class);
            Utils.invoke(null, configure, log4jProp);
        }
    }
}

