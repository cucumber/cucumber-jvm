package cucumber.runtime;

import cucumber.StepDefinition;
import cucumber.runtime.java.ObjectFactory;
import cucumber.runtime.java.pico.PicoFactory;

import java.util.Collections;
import java.util.List;

public class SimpleBackend implements Backend {
    private final List<StepDefinition> stepDefinitions;
    private final ObjectFactory objectFactory;

    public SimpleBackend(List<StepDefinition> stepDefinitions, ObjectFactory objectFactory) {
        this.stepDefinitions = stepDefinitions;
        this.objectFactory = objectFactory;
    }

    public SimpleBackend() {
        this(Collections.<StepDefinition>emptyList(), new PicoFactory());
    }

    public List<StepDefinition> getStepDefinitions() {
        return stepDefinitions;
    }

    public void newScenario() {
        objectFactory.createObjects();
    }
}
