package cucumber.fallback.runtime.java;

import cucumber.runtime.CucumberException;
import cucumber.runtime.java.ObjectFactory;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
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
            } catch (IllegalAccessError e) {
                throw new RuntimeException("can't create an instance of " + clazz.getName() + ", class does not have a default constructor", e);
            } catch (Exception e) {
                throw new RuntimeException("can't create an instance of " + clazz.getName(), e);
            }
        }
        injectStepDefinitions();
    }

    public void disposeInstances() {
        instances.clear();
    }

    public void addClass(Class<?> clazz) {
        if (Modifier.isAbstract(clazz.getModifiers())) {
            return;
        }
        classes.add(clazz);
    }

    public <T> T getInstance(Class<T> type) {
        return (T) instances.get(type);
    }

    private void injectStepDefinitions() throws CucumberException {
        for (Object outer : instances.values()) {
            for (Object inner : instances.values()) {
                if (outer.getClass().equals(inner.getClass())) {
                    continue;
                }
                for (Method outerMethod : outer.getClass().getDeclaredMethods()) {
                    if (outerMethod.getName().startsWith("set")) {
                        if (outerMethod.getParameterTypes().length == 1 && outerMethod.getParameterTypes()[0].equals(inner.getClass())) {
                            try {
                                outerMethod.setAccessible(true);
                                outerMethod.invoke(outer, inner);
                            } catch (IllegalAccessException e) {
                                throw new CucumberException(e);
                            } catch (InvocationTargetException e) {
                                throw new CucumberException(e);
                            }
                        }
                    }
                }
            }
        }
    }
}
