package cuke4duke.internal.jvmclass;

import cuke4duke.StepMother;
import org.picocontainer.MutablePicoContainer;
import org.picocontainer.PicoBuilder;

import java.util.ArrayList;
import java.util.List;

public class PicoFactory implements ObjectFactory {
    private MutablePicoContainer pico;
    private final List<Class<?>> classes = new ArrayList<Class<?>>();
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

    public void addClass(Class<?> clazz) {
        classes.add(clazz);
    }

    public void addStepMother(StepMother instance) {
        instances.add(instance);
    }

    public Object getComponent(Class<?> type) {
        return pico.getComponent(type);
    }

    public List<Class<?>> getClasses() {
        return classes;
    }
}
