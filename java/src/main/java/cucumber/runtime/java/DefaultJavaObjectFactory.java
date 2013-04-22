package cucumber.runtime.java;

import cucumber.runtime.CucumberException;

import java.lang.reflect.Constructor;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

class DefaultJavaObjectFactory implements ObjectFactory {
    private final Set<Class<?>> classes = new HashSet<Class<?>>();
    private final Map<Class<?>, Object> instances = new HashMap<Class<?>, Object>();

    public void start() {
        // No-op
    }

    public void stop() {
        instances.clear();
    }

    public void addClass(Class<?> clazz) {
        classes.add(clazz);
    }

    public <T> T getInstance(Class<T> type) {
        T instance = type.cast(instances.get(type));
        if (instance == null) {
            instance = cacheNewInstance(type);
        }
        return instance;
    }

    private <T> T cacheNewInstance(Class<T> type) {
        try {
            Constructor<T> constructor = type.getConstructor();
            T instance = constructor.newInstance();
            instances.put(type, instance);
            return instance;
        } catch (NoSuchMethodException e) {
            throw new CucumberException(String.format("%s doesn't have an empty constructor. If you need DI, put cucumber-picocontainer on the classpath", type), e);
        } catch (Exception e) {
            throw new CucumberException(String.format("Failed to instantiate %s", type), e);
        }
    }
}
