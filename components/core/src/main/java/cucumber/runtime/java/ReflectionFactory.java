package cucumber.runtime.java;

import com.sun.xml.internal.bind.v2.util.QNameMap;
import cuke4duke.StepMother;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class ReflectionFactory implements ObjectFactory {
    Set<Class<?>> classes = new HashSet<Class<?>>();
    private Map<Class<?>, Object> instancesByClass = new HashMap<Class<?>, Object>();

    public void createObjects() {
        instancesByClass.clear();
        for (Class<?> clazz : classes) {
            try {
                instancesByClass.put(clazz, clazz.newInstance());
            } catch (InstantiationException e) {
                throw new RuntimeException(e);
            } catch (IllegalAccessException e) {
                throw new RuntimeException(e);
            }
        }
    }

    public void disposeObjects() {
    }

    public boolean canHandle(Class<?> clazz) {
        return true;
    }

    public void addClass(Class<?> clazz) {
        classes.add(clazz);
    }

    public void addStepMother(StepMother mother) {
    }

    public <T> T getComponent(Class<T> type) {
        return (T) instancesByClass.get(type);
    }

    public Set<Class<?>> getClasses() {
        return classes;
    }
}
