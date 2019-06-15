package cucumber.runtime.java.picocontainer;

import cucumber.runtime.Utils;
import io.cucumber.core.backend.ObjectFactory;
import org.picocontainer.MutablePicoContainer;
import org.picocontainer.PicoBuilder;

import java.lang.reflect.Constructor;
import java.util.HashSet;
import java.util.Set;

public class PicoFactory implements ObjectFactory {
    private MutablePicoContainer pico;
    private final Set<Class<?>> classes = new HashSet<Class<?>>();

    public void start() {
        pico = new PicoBuilder()
            .withCaching()
            .withLifecycle()
            .build();
        for (Class<?> clazz : classes) {
            pico.addComponent(clazz);
        }
        pico.start();
    }

    public void stop() {
        pico.stop();
        pico.dispose();
    }

    public boolean addClass(Class<?> clazz) {
        if (Utils.isInstantiable(clazz) && classes.add(clazz)) {
            addConstructorDependencies(clazz);
        }
        return true;
    }

    public <T> T getInstance(Class<T> type) {
        return pico.getComponent(type);
    }

    private void addConstructorDependencies(Class<?> clazz) {
        for (Constructor constructor : clazz.getConstructors()) {
            for (Class paramClazz : constructor.getParameterTypes()) {
                addClass(paramClazz);
            }
        }
    }
}
