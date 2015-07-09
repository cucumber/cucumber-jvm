package cucumber.java.runtime.osgi;

import java.lang.reflect.Constructor;
import java.util.HashMap;
import java.util.Map;

import cucumber.api.java.ObjectFactory;
import cucumber.runtime.CucumberException;

public abstract class OsgiObjectFactoryBase implements ObjectFactory {
    private final Map<Class<?>, Object> instances = new HashMap<Class<?>, Object>();

    @Override
    public void start() {
        // No-op
    }

    @Override
    public void stop() {
        instances.clear();
    }

    @Override
    public boolean addClass(Class<?> glueClass) {
        return true;
    }

    @Override
    public <T> T getInstance(Class<T> glueClass) {
        T instance = glueClass.cast(instances.get(glueClass));
        if (instance == null) {
            instance = cacheNewInstance(glueClass);
            prepareGlueInstance(instance);
        }
        return instance;
    }

    protected abstract void prepareGlueInstance(Object instance);

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
