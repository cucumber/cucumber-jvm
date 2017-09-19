package cucumber.runtime.java.picocontainer;

import cucumber.api.java.ObjectFactory;
import cucumber.runtime.Utils;
import org.picocontainer.ComponentAdapter;
import org.picocontainer.MutablePicoContainer;
import org.picocontainer.PicoBuilder;

import java.lang.reflect.Constructor;
import java.util.HashSet;
import java.util.Set;

public class PicoFactory implements ObjectFactory {
    private MutablePicoContainer pico;
    private final Set<Class<?>> classes = new HashSet<Class<?>>();
    private final Set<ComponentAdapter<?>> adapters = new HashSet<ComponentAdapter<?>>();

    public void start() {
        pico = new PicoBuilder()
            .withCaching()
            .withLifecycle()
            .build();
        for (Class<?> clazz : classes) {
            pico.addComponent(clazz);
        }
        for (ComponentAdapter<?> adapter : adapters) {
            pico.addAdapter(adapter);
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

    /**
     * Allows subclasses to configure {@link org.picocontainer.PicoContainer} with custom adapters.
     * <p>
     * For example, subclasses can use this method to configure custom factory methods
     * by passing a {@link org.picocontainer.injectors.FactoryInjector}.
     *
     * @since 2.0.0
     */
    public void addAdapter(ComponentAdapter<?> componentAdapter) {
        adapters.add(componentAdapter);
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
