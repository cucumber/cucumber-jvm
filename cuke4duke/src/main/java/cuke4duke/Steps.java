package cuke4duke;

public class Steps {
    private final StepMother stepMother;

    public Steps(StepMother stepMother) {
        this.stepMother = stepMother;
    }

    /**
     * See {@link cuke4duke.StepMother#ask(String, int)}.
     */
    protected String ask(String question, int timeoutSecs) {
        return stepMother.ask(question, timeoutSecs);        
    }

    /**
     * See {@link cuke4duke.StepMother#announce(String)}.
     */
    protected void announce(String message) {
        stepMother.announce(message);
    }

    /**
     * See {@link cuke4duke.StepMother#embed(String, String)}.
     */
    protected void embed(String file, String mimeType) {
        stepMother.embed(file, mimeType);        
    }

    public void Given(String step) {
        stepMother.invoke(step);
    }

    public void Given(String step, Table table) {
        stepMother.invoke(step, table);
    }

    public void Given(String step, String multilineString) {
        stepMother.invoke(step, multilineString);
    }

    public void When(String step) {
        Given(step);
    }

    public void When(String step, Table table) {
        Given(step, table);
    }

    public void When(String step, String multilineString) {
        Given(step, multilineString);
    }

    public void Then(String step) {
        Given(step);
    }

    public void Then(String step, Table table) {
        Given(step, table);
    }

    public void Then(String step, String multilineString) {
        Given(step, multilineString);
    }
}
