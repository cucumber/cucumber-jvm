package cucumber.runtime.java.picocontainer;

import cucumber.runtime.java.ObjectFactory;
import org.picocontainer.MutablePicoContainer;
import org.picocontainer.PicoBuilder;

import java.util.HashSet;
import java.util.Set;

public class PicoFactory implements ObjectFactory {
    private MutablePicoContainer pico;
    private final Set<Class<?>> classes = new HashSet<Class<?>>();

    public void createInstances() {
        pico = new PicoBuilder().withCaching().build();
        for (Class<?> clazz : classes) {
            pico.addComponent(clazz);
        }
        pico.start();
    }

    public void disposeInstances() {
        pico.stop();
        pico.dispose();
    }

    public boolean canHandle(Class<?> clazz) {
        return true;
    }

    public void addClass(Class<?> clazz) {
        classes.add(clazz);
    }

    public <T> T getInstance(Class<T> type) {
        return pico.getComponent(type);
    }
}
