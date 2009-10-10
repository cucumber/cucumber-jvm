package cuke4duke;

import cuke4duke.StepMother;

public class Steps {
    private final StepMother stepMother;

    public Steps(StepMother stepMother) {
        this.stepMother = stepMother;
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
