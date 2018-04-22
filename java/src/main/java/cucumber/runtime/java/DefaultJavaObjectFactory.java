package cucumber.runtime.java;

import cucumber.api.java.ObjectFactory;
import cucumber.runtime.CucumberException;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;

/**
 * This class has package scope so it doesn't get loaded by reflection,
 * thereby colliding with other DI implementations.
 */
class DefaultJavaObjectFactory implements ObjectFactory {
    private final ThreadLocal<Map<Class<?>, Object>> instances = new ThreadLocal<Map<Class<?>, Object>>();

    public void start() {
        if (instances.get() == null) {
            instances.set(new HashMap<Class<?>, Object>());
        }
    }

    public void stop() {
        instances.get().clear();
    }

    public boolean addClass(Class<?> clazz) {
        return true;
    }

    public <T> T getInstance(Class<T> type) {
        T instance = type.cast(instances.get().get(type));
        if (instance == null) {
            instance = cacheNewInstance(type);
        }
        return instance;
    }

    private <T> T cacheNewInstance(Class<T> type) {
        try {
            Constructor<T> constructor = type.getConstructor();
            T instance = constructor.newInstance();
            instances.get().put(type, instance);
            return instance;
        } catch (NoSuchMethodException e) {
            throw new CucumberException(String.format("%s doesn't have an empty constructor. If you need DI, put cucumber-picocontainer on the classpath", type), e);
        } catch (Exception e) {
            throw new CucumberException(String.format("Failed to instantiate %s", type), e);
        }
    }
}
