package cuke4duke.internal;

public class JunitStepMother extends StepMother {
    public void newWorld() {
        stepDefinitions.clear();
    }

    public void registerSteps(Object object) {
        super.addCucumberMethods(object); 
    }
}