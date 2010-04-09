package cuke4duke.internal.language;

import cuke4duke.Scenario;

public interface Hook {
    public String[] tag_expressions();
    void invoke(String location, Scenario scenario) throws Throwable;
}
