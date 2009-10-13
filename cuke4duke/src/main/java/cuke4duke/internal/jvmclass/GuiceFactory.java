package cuke4duke.internal.jvmclass;

import java.util.HashMap;
import java.util.Map;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Module;

/**
 * @author Henning Jensen
 */
public class GuiceFactory implements ObjectFactory {

    private Injector injector;
    private Map<Class<?>, Object> instanceMap = new HashMap<Class<?>, Object>();

    public GuiceFactory() {
        String moduleClassName = System.getProperty("cuke4duke.guiceModule", null);
        try {
            Module module = (Module) Class.forName(moduleClassName).newInstance();
            injector = Guice.createInjector(module);
        } catch (InstantiationException e) {
            System.err.println("Could not instantiate Guice module. " + e);
        } catch (IllegalAccessException e) {
            System.err.println("Could not access Guice module class. " + e);
        } catch (ClassNotFoundException e) {
            System.err.println("Could not find Guice module. " + e);
        }
    }

    public void addClass(Class<?> clazz) {
        instanceMap.put(clazz, injector.getInstance(clazz));
    }

    public void addInstance(Object instance) {
    }

    public void createObjects() {
    }

    public void disposeObjects() {
    }

    public Object getComponent(Class<?> type) {
        return instanceMap.get(type);
    }

}
