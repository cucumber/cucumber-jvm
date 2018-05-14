package cucumber.runtime.java.picocontainer;

import cucumber.api.java.ObjectFactory;
import cucumber.runtime.Utils;
import org.picocontainer.MutablePicoContainer;
import org.picocontainer.PicoBuilder;

import java.lang.reflect.Constructor;
import java.util.HashSet;
import java.util.Set;

public class PicoFactory implements ObjectFactory {
    private final ThreadLocal<MutablePicoContainer> pico = new ThreadLocal<MutablePicoContainer>();
    private final Set<Class<?>> classes = new HashSet<Class<?>>();

    public void start() {
        pico.set(new PicoBuilder()
            .withCaching()
            .withLifecycle()
            .build());
        for (Class<?> clazz : classes) {
            pico.get().addComponent(clazz);
        }
        pico.get().start();
    }

    public void stop() {
        pico.get().stop();
        pico.get().dispose();
    }

    public boolean addClass(Class<?> clazz) {
        if (Utils.isInstantiable(clazz) && classes.add(clazz)) {
            addConstructorDependencies(clazz);
        }
        return true;
    }

    public <T> T getInstance(Class<T> type) {
        return pico.get().getComponent(type);
    }

    private void addConstructorDependencies(Class<?> clazz) {
        for (Constructor constructor : clazz.getConstructors()) {
            for (Class paramClazz : constructor.getParameterTypes()) {
                addClass(paramClazz);
            }
        }
    }
}
