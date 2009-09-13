package cuke4duke.internal.language;

import java.util.ArrayList;
import java.util.List;

public abstract class ProgrammingLanguage {
    private List<StepDefinition> stepDefinitions;

    public List<StepDefinition> step_definitions_for(String file) throws Throwable {
        stepDefinitions = new ArrayList<StepDefinition>();
        load(file);
        return stepDefinitions;
    }

    public abstract void begin_scenario();
    public abstract void end_scenario();

    protected abstract void load(String file) throws Throwable;

    public void addStepDefinition(StepDefinition stepDefinition) {
        stepDefinitions.add(stepDefinition);
    }
}
