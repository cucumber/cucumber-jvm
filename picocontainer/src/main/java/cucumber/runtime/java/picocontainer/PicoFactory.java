package cucumber.runtime.java.picocontainer;

import cucumber.runtime.java.ObjectFactory;
import org.picocontainer.MutablePicoContainer;
import org.picocontainer.PicoBuilder;

import java.lang.reflect.Constructor;
import java.util.HashSet;
import java.util.Set;

public class PicoFactory implements ObjectFactory {
    private MutablePicoContainer pico;
    private final Set<Class<?>> classes = new HashSet<Class<?>>();

    public void start() {
        pico = new PicoBuilder().withCaching().build();
        for (Class<?> clazz : classes) {
            pico.addComponent(clazz);
        }
        pico.start();
    }

    public void stop() {
        pico.stop();
        pico.dispose();
    }

    public void addClass(Class<?> clazz) {
        if (classes.add(clazz)) {
            addConstructorDependencies(clazz);
        }
    }

    public <T> T getInstance(Class<T> type) {
        return pico.getComponent(type);
    }

    @Override
    public Object getContainer() {
        return pico;
    }

    @Override
    public String getContainerName() {
        return "container";
    }

    private void addConstructorDependencies(Class<?> clazz) {
        for (Constructor constructor : clazz.getConstructors()) {
            for (Class paramClazz : constructor.getParameterTypes()) {
                addClass(paramClazz);
            }
        }
    }
}
