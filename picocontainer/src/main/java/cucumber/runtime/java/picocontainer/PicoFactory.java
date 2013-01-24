package cucumber.runtime.java.picocontainer;

import java.io.IOException;
import java.lang.reflect.Constructor;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.picocontainer.MutablePicoContainer;
import org.picocontainer.PicoBuilder;

import cucumber.runtime.java.ObjectFactory;
import cucumber.runtime.java.picocontainer.configuration.PicoConfigurer;
import cucumber.runtime.java.picocontainer.configuration.PicoMapper;

public class PicoFactory implements ObjectFactory, PicoMapper {
    private MutablePicoContainer pico;
    private final Set<Class<?>> classes = new HashSet<Class<?>>();
    private final Map<Class<?>, Class<?>> componentKeyToImplementationClassMapping = new HashMap<Class<?>, Class<?>>();
    private final PicoConfigurer customPicoConfigurer;

    public PicoFactory() throws IOException {
        this(new CustomPicoConfigurerFactory(
                new PropertyLoader("/cucumber-picocontainer.properties")));
    }
    
    PicoFactory(CustomPicoConfigurerFactory configurerFactory) throws IOException {
        customPicoConfigurer = configurerFactory.getConfigurer();
    }

    public void start() {
        customPicoConfigurer.configure(this);
        pico = new PicoBuilder().withCaching().build();
        for (Class<?> clazz : classes) {
            pico.addComponent(clazz);
        }
        for (Entry<Class<?>, Class<?>> mapping : componentKeyToImplementationClassMapping.entrySet()) {
            pico.addComponent(mapping.getKey(), mapping.getValue());
        }
        pico.start();
    }

    public void stop() {
        pico.stop();
        pico.dispose();
    }

    public void addClass(Class<?> clazz) {
        if (!clazz.isInterface()) {
            if (classes.add(clazz)) {
                addConstructorDependencies(clazz);
            }
        }
    }

    public void addClass(Class<?> componentKey, Class<?> implementation) {
        componentKeyToImplementationClassMapping.put(componentKey, implementation);
        addConstructorDependencies(implementation);
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
