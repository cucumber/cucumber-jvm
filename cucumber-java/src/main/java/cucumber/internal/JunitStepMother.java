package cucumber.internal;

import org.picocontainer.DefaultPicoContainer;
import org.picocontainer.MutablePicoContainer;

public class JunitStepMother extends StepMother {
    public void newWorld() {
        stepDefinitions.clear();
    }

    public void registerSteps(Object object) {
        super.addStepDefinitions(object); 
    }
}