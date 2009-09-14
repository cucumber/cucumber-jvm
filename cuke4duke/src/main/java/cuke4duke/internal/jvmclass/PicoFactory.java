package cuke4duke.internal.jvmclass;

import java.util.ArrayList;
import java.util.List;

import org.picocontainer.MutablePicoContainer;
import org.picocontainer.PicoBuilder;

public class PicoFactory implements ObjectFactory {
    private MutablePicoContainer pico;
    private final List<Class<?>> classes = new ArrayList<Class<?>>();

    public void createObjects() {
        pico = new PicoBuilder().withCaching().build();
        for (Class<?> clazz : classes) {
            pico.addComponent(clazz);
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

    public Object getComponent(Class<?> type) {
        return pico.getComponent(type);
    }
}
