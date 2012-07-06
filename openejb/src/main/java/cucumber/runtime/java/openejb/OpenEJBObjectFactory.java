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
}

