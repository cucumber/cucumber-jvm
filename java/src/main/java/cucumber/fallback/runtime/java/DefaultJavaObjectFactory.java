package cucumber.fallback.runtime.java;

import cucumber.runtime.java.ObjectFactory;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class DefaultJavaObjectFactory implements ObjectFactory {
    private final Set<Class<?>> classes = new HashSet<Class<?>>();
    private final Map<Class<?>, Object> instances = new HashMap<Class<?>, Object>();

    public void createInstances() {
        for (Class<?> clazz : classes) {
            try {
                instances.put(clazz, clazz.newInstance());
            } catch (Exception e) {
                throw new RuntimeException("can't create an instance of " + clazz.getName(), e);
            }
        }
    }

    public void disposeInstances() {
        instances.clear();
    }

    public void addClass(Class<?> clazz) {
        classes.add(clazz);
    }

    public <T> T getInstance(Class<T> type) {
        return (T) instances.get(type);
    }
}
