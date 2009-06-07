package cuke4duke.internal;

import org.picocontainer.DefaultPicoContainer;
import org.picocontainer.MutablePicoContainer;

public class PicoContainerStepMother extends StepMother {
    public void newWorld() {
        stepDefinitions.clear();
        MutablePicoContainer pico = new DefaultPicoContainer();
        for (Class<?> clazz : classes) {
            pico.addComponent(clazz);
        }

        for (Object object : pico.getComponents()) {
            addCucumberMethods(object);
        }
    }
}
