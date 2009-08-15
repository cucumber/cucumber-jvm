package cuke4duke.internal.language;

public abstract class ProgrammingLanguage {
    private final StepMother stepMother;

    public ProgrammingLanguage(StepMother stepMother) {
        this.stepMother = stepMother;
    }

    public abstract void load_step_def_file(String step_def_file) throws Exception;
    public abstract void begin_scenario();
    public abstract void end_scenario();

    public StepMother step_mother() {
        return stepMother;
    }
}
