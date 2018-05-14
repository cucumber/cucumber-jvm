package cucumber.runtime.java.openejb;

import cucumber.runtime.CucumberException;
import cucumber.api.java.ObjectFactory;
import org.apache.openejb.OpenEjbContainer;

import javax.ejb.embeddable.EJBContainer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;

public class OpenEJBObjectFactory implements ObjectFactory {
    private final List<String> classes = new ArrayList<String>();
    private final ThreadLocal<Map<Class<?>, Object>> instances = new ThreadLocal<Map<Class<?>, Object>>(){
        protected Map<Class<?>, Object> initialValue() {
            return new HashMap<Class<?>, Object>();
        }
    };
    private final ThreadLocal<EJBContainer> container = new ThreadLocal<EJBContainer>();

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
        container.set(EJBContainer.createEJBContainer(properties));
    }

    @Override
    public void stop() {
        container.get().close();
        instances.get().clear();
    }

    @Override
    public boolean addClass(Class<?> clazz) {
        classes.add(clazz.getName());
        return true;
    }

    @Override
    public <T> T getInstance(Class<T> type) {
        if (instances.get().containsKey(type)) {
            return type.cast(instances.get().get(type));
        }

        T object;
        try {
            object = type.newInstance();
            container.get().getContext().bind("inject", object);
        } catch (Exception e) {
            throw new CucumberException("can't create " + type.getName(), e);
        }
        instances.get().put(type, object);
        return object;
    }
}

