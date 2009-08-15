package cuke4duke.internal.language;

public interface StepMother {
    void register_step_definition(StepDefinition stepDefinition);
    void register_hook(String phase, Hook hook);    
}
