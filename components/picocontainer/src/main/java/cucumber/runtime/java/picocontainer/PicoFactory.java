package cucumber.runtime.java.picocontainer;

import cucumber.runtime.java.ObjectFactory;
import cuke4duke.StepMother;
import org.picocontainer.MutablePicoContainer;
import org.picocontainer.PicoBuilder;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class PicoFactory implements ObjectFactory {
    private MutablePicoContainer pico;
    private final Set<Class<?>> classes = new HashSet<Class<?>>();
    private final List<Object> instances = new ArrayList<Object>();

    public void createObjects() {
        pico = new PicoBuilder().withCaching().build();
        for (Class<?> clazz : classes) {
            pico.addComponent(clazz);
        }
        for (Object instance : instances) {
            pico.addComponent(instance);
        }
        pico.start();
    }

    public void disposeObjects() {
        pico.stop();
        pico.dispose();
    }

    public boolean canHandle(Class<?> clazz) {
        return true;
    }

    public void addClass(Class<?> clazz) {
        classes.add(clazz);
    }

    public void addStepMother(StepMother instance) {
        instances.add(instance);
    }

    public <T> T getComponent(Class<T> type) {
        return pico.getComponent(type);
    }

    public Set<Class<?>> getClasses() {
        return classes;
    }
}
