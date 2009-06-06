package cucumber.internal;

public class JunitStepMother extends StepMother {
    public void newWorld() {
        stepDefinitions.clear();
    }

    public void registerSteps(Object object) {
        super.addStepDefinitions(object); 
    }
}