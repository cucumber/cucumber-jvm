package cuke4duke;

public interface StepMother {
    void invoke(String step);
    void invoke(String step, Table table);
    void invoke(String step, String multilineString);
}
