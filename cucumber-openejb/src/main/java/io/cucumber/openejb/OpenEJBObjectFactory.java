package io.cucumber.openejb;

import io.cucumber.core.backend.CucumberBackendException;
import io.cucumber.core.backend.ObjectFactory;
import org.apache.openejb.OpenEjbContainer;
import org.apiguardian.api.API;
import org.jspecify.annotations.Nullable;

import javax.ejb.embeddable.EJBContainer;
import javax.naming.Context;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import static java.util.Objects.requireNonNull;

@API(status = API.Status.STABLE)
public final class OpenEJBObjectFactory implements ObjectFactory {

    private final List<String> classes = new ArrayList<String>();
    private final Map<Class<?>, Object> instances = new HashMap<Class<?>, Object>();
    private @Nullable EJBContainer container;

    @Override
    public void start() {
        final StringBuilder callers = new StringBuilder();
        for (Iterator<String> it = classes.iterator(); it.hasNext();) {
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
        if (container != null) {
            container.close();
        }
        instances.clear();
    }

    @Override
    public boolean addClass(Class<?> clazz) {
        classes.add(clazz.getName());
        return true;
    }

    @Override
    public <T> T getInstance(Class<T> type) {
        if (instances.containsKey(type)) {
            return type.cast(instances.get(type));
        }

        T object;
        try {
            object = type.getDeclaredConstructor().newInstance();
            Context context = requireNonNull(container).getContext();
            context.bind("inject", object);
        } catch (Exception e) {
            throw new CucumberBackendException("can't create " + type.getName(), e);
        }
        instances.put(type, object);
        return object;
    }

}
