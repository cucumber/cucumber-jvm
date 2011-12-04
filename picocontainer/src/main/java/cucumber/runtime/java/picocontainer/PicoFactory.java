package cucumber.runtime.java.picocontainer;

import cucumber.runtime.java.ObjectFactory;
import org.picocontainer.MutablePicoContainer;
import org.picocontainer.PicoBuilder;

import java.lang.reflect.Modifier;
import java.util.HashSet;
import java.util.Set;

public class PicoFactory implements ObjectFactory {
    private MutablePicoContainer pico;
    private final Set<Class<?>> classes = new HashSet<Class<?>>();

    public void createInstances() {
        pico = new PicoBuilder().withCaching().build();
        for (Class<?> clazz : classes) {
            if (isInstantiable(clazz)) {
                pico.addComponent(clazz);
            }
        }
        pico.start();
    }

    private boolean isInstantiable(Class<?> clazz) {
        if (Modifier.isAbstract(clazz.getModifiers())) {
            return false;
        }
        return true;
    }

    public void disposeInstances() {
        pico.stop();
        pico.dispose();
    }

    public void addClass(Class<?> clazz) {
        classes.add(clazz);
    }

    public <T> T getInstance(Class<T> type) {
        return pico.getComponent(type);
    }
}
